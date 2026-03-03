package com.project.oms.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateOrderPaymentRequest(
        @NotNull BigDecimal payment
) {
}

