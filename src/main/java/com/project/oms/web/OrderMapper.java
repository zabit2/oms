package com.project.oms.web;

import com.project.oms.domain.Order;
import com.project.oms.domain.OrderItem;
import com.project.oms.web.dto.OrderItemResponse;
import com.project.oms.web.dto.OrderResponse;

import java.util.List;

final class OrderMapper {

    private OrderMapper() {
    }

    static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}

