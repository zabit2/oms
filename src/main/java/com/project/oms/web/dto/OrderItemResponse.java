package com.project.oms.web.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}

