package com.project.oms.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        String orderId,
        BigDecimal paymentAmount,
        OffsetDateTime createdAt
) {
}

