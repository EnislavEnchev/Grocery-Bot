package com.organizer.grocery.controller;

import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.dto.OrderResponseDto;
import com.organizer.grocery.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto orderRequest) {
        OrderResponseDto response = orderService.placeOrder(orderRequest);
        if (response.status() == com.organizer.grocery.model.OrderStatus.FAIL) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderStatus(id);
        return ResponseEntity.ok(response);
    }
}
