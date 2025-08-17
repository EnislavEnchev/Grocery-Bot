package com.organizer.grocery.controller;

import com.organizer.grocery.dto.OrderItemRequestDto;
import com.organizer.grocery.dto.OrderRequestDto;
import com.organizer.grocery.dto.OrderResponseDto;
import com.organizer.grocery.model.OrderItem;
import com.organizer.grocery.service.OrderService;
import com.organizer.grocery.service.OrderItemsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemsService orderItemsService;

    public OrderController(OrderService orderService, OrderItemsService orderItemsService) {
        this.orderService = orderService;
        this.orderItemsService = orderItemsService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto orderRequest) throws Exception {
        OrderResponseDto response = orderService.placeOrder(orderRequest);
        if (response.status() == com.organizer.grocery.model.OrderStatus.FAILED) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderStatus(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<OrderItemRequestDto>> getProductsByOrderId(@PathVariable Long id) {
        List<OrderItemRequestDto> products = orderItemsService.getProductsByOrderId(id);
        System.out.println("Products for order ID " + id + ": " + products.get(0).toString());
        return ResponseEntity.ok(products);
    }
}
