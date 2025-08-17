package com.organizer.grocery.aws;

import com.organizer.grocery.algo.RouteStrategy;
import com.organizer.grocery.dto.OrderItemRequestDto;
import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.model.*;
import com.organizer.grocery.repository.OrderRepository;
import com.organizer.grocery.repository.ProductRepository;
import com.organizer.grocery.service.RouteService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderRetry {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RouteService routeService;
    private final Map<String, RouteStrategy> routeStrategies;


    public OrderRetry(OrderRepository orderRepository, ProductRepository productRepository, RouteService routeService,
                                      Map<String, RouteStrategy> routeStrategies) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.routeService = routeService;
        this.routeStrategies = routeStrategies;
    }

    @Transactional
    public boolean retryFailedOrder(OrderRequestDto orderRequest, Long orderId) throws Exception {
        RouteStrategy routeStrategy = routeStrategies.get("ChristofidesStrategy");
        List<List<Product>> productsToUpdate = new ArrayList<>();
        Map<String, Integer> productNameToQuantity = new HashMap<>();
        for (OrderItemRequestDto itemRequest : orderRequest.items()) {
            List<Product> products = productRepository.findAllByNameForUpdate(itemRequest.productName());
            if (products.isEmpty()) {
                return false;
            }
            int totalQuantity = products.stream().mapToInt(Product::getQuantity).sum();
            if (totalQuantity < itemRequest.quantity()) {
                return false;
            }
            productNameToQuantity.put(itemRequest.productName(), itemRequest.quantity());
            productsToUpdate.add(products);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        order.setStatus(OrderStatus.SUCCESSFUL);
        order.setMessage("Order successfully retried");
        Order savedOrder = orderRepository.save(order);
        List<Product> chosenProducts = routeStrategy.getOptimalRoute(productsToUpdate, productNameToQuantity);
        routeService.saveRouteForOrder(savedOrder, productNameToQuantity, chosenProducts);
        return true;
    }
}
