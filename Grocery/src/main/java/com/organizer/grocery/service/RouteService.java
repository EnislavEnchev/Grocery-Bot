package com.organizer.grocery.service;

import com.organizer.grocery.model.Coordinate;
import com.organizer.grocery.model.Order;
import com.organizer.grocery.dto.RouteDto;
import com.organizer.grocery.model.Product;

import java.util.List;
import java.util.Map;

public interface RouteService {
    void saveRouteForOrder(Order order, Map<String, Integer> productNameToQuantity, List<Product> chosenProducts) throws Exception;
    RouteDto getRouteByOrderId(Long orderId);
}
