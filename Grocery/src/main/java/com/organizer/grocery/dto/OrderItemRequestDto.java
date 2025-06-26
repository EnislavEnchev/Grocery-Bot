package com.organizer.grocery.dto;

public record OrderItemRequestDto(
        String productName,
        int quantity
) {}
