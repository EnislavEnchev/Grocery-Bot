package com.organizer.grocery.service;

import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.dto.OrderResponseDto;

public interface OrderService {
    OrderResponseDto placeOrder(OrderRequestDto orderRequest) throws Exception;
    OrderResponseDto getOrderStatus(Long orderId);
    void deleteOrder(Long orderId);
}
