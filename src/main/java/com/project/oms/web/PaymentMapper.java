package com.project.oms.web;

import com.project.oms.domain.Order;
import com.project.oms.domain.OrderItem;
import com.project.oms.domain.Payment;
import com.project.oms.web.dto.OrderItemResponse;
import com.project.oms.web.dto.OrderResponse;
import com.project.oms.web.dto.PaymentResponse;

import java.util.List;

final class PaymentMapper {

    private PaymentMapper() {
    }

    static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getPaymentValue(),
                payment.getCreatedAt()
        );
    }
}

