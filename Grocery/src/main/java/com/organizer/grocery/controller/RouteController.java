package com.organizer.grocery.controller;

import com.organizer.grocery.dto.RouteDto;
import com.organizer.grocery.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for retrieving calculated picking routes.
 */
@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<RouteDto> getRouteForOrder(@RequestParam Long orderId) {
        RouteDto routeDTO = routeService.getRouteByOrderId(orderId);
        return ResponseEntity.ok(routeDTO);
    }
}
