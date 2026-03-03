package com.project.oms.service;

import com.project.oms.domain.Payment;
import com.project.oms.exception.InvalidOrderStatusException;
import com.project.oms.exception.OrderNotFoundException;
import com.project.oms.repository.PaymentRepository;
import com.project.oms.web.dto.CreatePaymentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        if (request == null || request.paymentValue().intValue() < 0) {
            throw new InvalidOrderStatusException("Invalid payment value");
        }
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setPaymentAmount(request.paymentValue());
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Payment> listPayments(Optional<Long> orderId) {
        return orderId.map(paymentRepository::findByOrderId)
                .orElseGet(paymentRepository::findAll);
    }
}

