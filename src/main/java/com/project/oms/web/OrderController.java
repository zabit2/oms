package com.project.oms.web;

import com.project.oms.domain.Order;
import com.project.oms.domain.OrderStatus;
import com.project.oms.service.OrderService;
import com.project.oms.web.dto.CreateOrderRequest;
import com.project.oms.web.dto.OrderResponse;
import com.project.oms.web.dto.UpdateOrderPaymentRequest;
import com.project.oms.web.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(OrderMapper.toResponse(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(name = "status", required = false) OrderStatus status
    ) {
        List<Order> orders = orderService.listOrders(Optional.ofNullable(status));
        List<OrderResponse> responses = orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        Order updated = orderService.updateStatus(id, request.status());
        return ResponseEntity.ok(OrderMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<OrderResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderPaymentRequest request
    ) {
        Order updated = orderService.updatePayment(id, request.payment());
        return ResponseEntity.ok(OrderMapper.toResponse(updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        Order canceled = orderService.cancelOrder(id);
        return ResponseEntity.ok(OrderMapper.toResponse(canceled));
    }
}

