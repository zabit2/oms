package com.project.oms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotEmpty List<OrderItemRequest> items
) {
}

