package com.project.oms.web;

import com.project.oms.domain.Payment;
import com.project.oms.service.PaymentService;
import com.project.oms.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Payment created = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPayment(id);
        return ResponseEntity.ok(PaymentMapper.toResponse(payment));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @RequestParam(name = "orderId", required = false) Long orderId
    ) {
        List<Payment> payments = paymentService.listPayments(Optional.ofNullable(orderId));
        List<PaymentResponse> responses = payments.stream()
                .map(PaymentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }
}

