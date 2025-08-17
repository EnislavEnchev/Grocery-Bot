package com.organizer.grocery.dto;

import java.io.Serializable;

public record OrderItemRequestDto(
        String productName,
        int quantity
) implements Serializable {}
