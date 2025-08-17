package com.organizer.grocery.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record FailedOrderRequestDto(
        Long orderId,
        LocalDateTime initializationTime,
        OrderRequestDto orderRequestDto
) implements Serializable { }
