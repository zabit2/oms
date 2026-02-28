package com.project.oms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank String productId,
        @NotBlank String productName,
        @Positive int quantity,
        @NotNull @Positive BigDecimal unitPrice
) {
}

