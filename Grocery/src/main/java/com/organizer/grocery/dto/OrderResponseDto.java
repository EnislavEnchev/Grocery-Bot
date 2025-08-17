package com.organizer.grocery.dto;

import com.organizer.grocery.model.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public record OrderResponseDto(
        Long orderId,
        OrderStatus status,
        LocalDateTime orderTimestamp,
        String message,
        String userFullName
) implements Serializable {}
