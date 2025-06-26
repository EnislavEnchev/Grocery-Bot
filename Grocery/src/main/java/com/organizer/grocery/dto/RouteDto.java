package com.organizer.grocery.dto;

import com.organizer.grocery.model.OrderStatus;
import com.organizer.grocery.model.Coordinate;

import java.util.List;

public record RouteDto(
        Long orderId,
        OrderStatus status,
        List<Coordinate> visitedLocations
) {}
