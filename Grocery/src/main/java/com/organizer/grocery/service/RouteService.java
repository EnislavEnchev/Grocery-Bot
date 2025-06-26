package com.organizer.grocery.service;

import com.organizer.grocery.model.Order;
import com.organizer.grocery.dto.RouteDto;

public interface RouteService {
    void createAndSaveRouteForOrder(Order order);
    RouteDto getRouteByOrderId(Long orderId);
}
