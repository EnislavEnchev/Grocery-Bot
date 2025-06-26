package com.organizer.grocery.dto;

import java.util.List;

public record OrderRequestDto(
        List<OrderItemRequestDto> items
) {}