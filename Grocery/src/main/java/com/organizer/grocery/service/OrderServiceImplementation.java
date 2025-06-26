package com.organizer.grocery.service;


import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.dto.OrderResponseDto;
import com.organizer.grocery.model.Order;
import com.organizer.grocery.model.OrderItem;
import com.organizer.grocery.model.OrderStatus;
import com.organizer.grocery.repository.OrderRepository;
import com.organizer.grocery.repository.ProductRepository;
import com.organizer.grocery.model.Product;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RouteService routeService;

    public OrderServiceImplementation(OrderRepository orderRepository, ProductRepository productRepository, RouteService routeService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.routeService = routeService;
    }

    @Override
    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto orderRequest) {
        List<String> missingItems = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();
        for (var itemRequest : orderRequest.items()) {
            Product product = productRepository.findByName(itemRequest.productName())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with name: " + itemRequest.productName()));

            if (product.getQuantity() < itemRequest.quantity()) {
                missingItems.add(
                        String.format("%s: requested %d, available %d",
                                product.getName(), itemRequest.quantity(), product.getQuantity())
                );
            }
            productsToUpdate.add(product);
        }

        if (!missingItems.isEmpty()) {
            Order failedOrder = new Order();
            failedOrder.setStatus(OrderStatus.FAIL);
            orderRepository.save(failedOrder);
            String errorMessage = "Not enough stock to fulfill the order. Missing items: " + String.join("; ", missingItems);
            return new OrderResponseDto(failedOrder.getId(), OrderStatus.FAIL, failedOrder.getOrderTimestamp(), errorMessage);
        }

        Order order = new Order();

        for (int i = 0; i < orderRequest.items().size(); i++) {
            var itemRequest = orderRequest.items().get(i);
            Product product = productsToUpdate.get(i);

            OrderItem orderItem = new OrderItem(null, order, product, itemRequest.quantity());
            order.getOrderItems().add(orderItem);

            product.setQuantity(product.getQuantity() - itemRequest.quantity());
            productRepository.save(product);
        }

        Order savedOrder = orderRepository.save(order);
        routeService.createAndSaveRouteForOrder(savedOrder);
        savedOrder.setStatus(OrderStatus.SUCCESS);
        return new OrderResponseDto(savedOrder.getId(), savedOrder.getStatus(), savedOrder.getOrderTimestamp(), "Order ready! Please collect it at the desk.");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        String message = order.getStatus() == OrderStatus.SUCCESS? "Order was fulfilled successfully." : "Order could not be fulfilled.";
        return new OrderResponseDto(order.getId(), order.getStatus(), order.getOrderTimestamp(), message);
    }
}
