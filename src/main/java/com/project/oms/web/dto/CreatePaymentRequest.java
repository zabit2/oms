package com.project.oms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record CreatePaymentRequest(
        @NotBlank String orderId,
        BigDecimal paymentValue
) {
}

