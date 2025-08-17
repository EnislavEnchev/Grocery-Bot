package com.organizer.grocery.service;

import com.organizer.grocery.dto.OrderItemRequestDto;

import java.util.List;

public interface OrderItemsService {
    List<OrderItemRequestDto> getProductsByOrderId(Long orderId);
}
