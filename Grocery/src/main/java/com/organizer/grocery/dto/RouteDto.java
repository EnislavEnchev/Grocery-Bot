package com.organizer.grocery.dto;

import com.organizer.grocery.model.OrderStatus;
import com.organizer.grocery.model.PickedProduct;

import java.io.Serializable;
import java.util.List;

public record RouteDto(
        Long orderId,
        OrderStatus status,
        List<PickedProduct> visitedLocations
) implements Serializable {}
