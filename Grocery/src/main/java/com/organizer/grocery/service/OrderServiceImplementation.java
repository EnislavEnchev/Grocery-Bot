package com.organizer.grocery.service;


import com.organizer.grocery.algo.HelperFunctions;
import com.organizer.grocery.algo.RouteStrategy;
import com.organizer.grocery.dto.FailedOrderRequestDto;
import com.organizer.grocery.dto.OrderItemRequestDto;
import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.dto.OrderResponseDto;
import com.organizer.grocery.aws.SqsQueueManager;
import com.organizer.grocery.model.*;
import com.organizer.grocery.repository.OrderRepository;
import com.organizer.grocery.repository.ProductRepository;
import com.organizer.grocery.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RouteService routeService;
    private final UserRepository userRepository;
    private final Map<String, RouteStrategy> routeStrategies;
    private final CacheService cacheService;
    private final SqsQueueManager SqsQueueManager;


    public OrderServiceImplementation(UserRepository userRepository, OrderRepository orderRepository, ProductRepository productRepository, RouteService routeService,
                                      Map<String, RouteStrategy> routeStrategies, SqsQueueManager SqsQueueManager, CacheService cacheService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.routeService = routeService;
        this.routeStrategies = routeStrategies;
        this.SqsQueueManager = SqsQueueManager;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto orderRequest) throws Exception {
        RouteStrategy routeStrategy = routeStrategies.get("ChristofidesStrategy");
        List<String> missingItems = new ArrayList<>();
        List<List<Product>> productsToUpdate = new ArrayList<>();
        Map<String, Integer> productNameToQuantity = new HashMap<>();
        Order order = new Order();
        for (OrderItemRequestDto itemRequest : orderRequest.items()) {
            List<Product> products = productRepository.findAllByNameForUpdate(itemRequest.productName());
            if (products.isEmpty()) {
                missingItems.add(String.format("%s:0", itemRequest.productName()));
                continue;
            }
            int totalQuantity = products.stream().mapToInt(Product::getQuantity).sum();
            if (totalQuantity < itemRequest.quantity()) {
                missingItems.add(
                        String.format("%s:%d",
                                products.get(0).getName(), totalQuantity)
                );
            }
            OrderItem orderItem = new OrderItem(null, order, itemRequest.productName(), itemRequest.quantity());
            order.getOrderItems().add(orderItem);
            productNameToQuantity.put(itemRequest.productName(), itemRequest.quantity());
            productsToUpdate.add(products);
        }

        User user = userRepository.findByEmail(HelperFunctions.getUsernameFromContext())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String username = user.getFullName();
        if (!missingItems.isEmpty()) {
            order.setStatus(OrderStatus.PENDING);
            String missingItemsString = String.join(";", missingItems);
            String errorMessage = "Not enough stock to fulfill the order";
            order.setMessage(missingItemsString);
            orderRepository.save(order);
            System.out.println("Order count after saving: " + order.getOrderItems().size());
            FailedOrderRequestDto failedRequest = new FailedOrderRequestDto(order.getId(), LocalDateTime.now(), orderRequest);
            SqsQueueManager.sendOrder(failedRequest);
            return new OrderResponseDto(order.getId(), OrderStatus.FAILED, order.getOrderTimestamp(), errorMessage, username);
        }

        order.setStatus(OrderStatus.SUCCESSFUL);
        Order savedOrder = orderRepository.save(order);
        //Execute Lambda function here?
        List<Product> chosenProducts = routeStrategy.getOptimalRoute(productsToUpdate, productNameToQuantity);
        routeService.saveRouteForOrder(savedOrder, productNameToQuantity, chosenProducts);
        return new OrderResponseDto(savedOrder.getId(), savedOrder.getStatus(), savedOrder.getOrderTimestamp(),
                "Order ready! Please collect it at the desk.", username);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponseDto getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        User user = userRepository.findByEmail(HelperFunctions.getUsernameFromContext())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String username = user.getFullName();
        String message = order.getStatus() == OrderStatus.SUCCESSFUL? "Order was fulfilled successfully." : order.getMessage();
        return new OrderResponseDto(order.getId(), order.getStatus(), order.getOrderTimestamp(), message, username);
    }

    @Override
    @Transactional
    @CacheEvict(value = "orders", key = "#orderId")
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }

}
