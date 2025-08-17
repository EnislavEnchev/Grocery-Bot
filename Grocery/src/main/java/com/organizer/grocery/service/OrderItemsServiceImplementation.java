package com.organizer.grocery.service;

import com.organizer.grocery.model.OrderItem;
import com.organizer.grocery.dto.OrderItemRequestDto;
import com.organizer.grocery.repository.OrderItemRepository;

import jakarta.persistence.criteria.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemsServiceImplementation implements OrderItemsService {
    private final OrderItemRepository orderItemRepository;

    public OrderItemsServiceImplementation(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemRequestDto> getProductsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(item -> new OrderItemRequestDto(
                        item.getProductName(),
                        item.getQuantity()
                ))
                .toList();
    }
}
