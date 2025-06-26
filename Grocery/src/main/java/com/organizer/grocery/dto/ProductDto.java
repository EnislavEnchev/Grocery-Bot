package com.organizer.grocery.dto;

import com.organizer.grocery.model.Coordinate;
import java.math.BigDecimal;

public record ProductDto(
        Long id,
        String name,
        BigDecimal price,
        int quantity,
        Coordinate location
) {}