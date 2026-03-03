package com.project.oms.service;

import com.project.oms.domain.Order;
import com.project.oms.domain.OrderItem;
import com.project.oms.domain.OrderStatus;
import com.project.oms.domain.Payment;
import com.project.oms.exception.InvalidOrderStatusException;
import com.project.oms.exception.InvalidPaymentValueException;
import com.project.oms.exception.OrderNotFoundException;
import com.project.oms.repository.OrderRepository;
import com.project.oms.repository.PaymentRepository;
import com.project.oms.web.dto.CreateOrderRequest;
import com.project.oms.web.dto.OrderItemRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository) {

        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new InvalidOrderStatusException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.productId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(itemRequest.unitPrice());

            BigDecimal itemTotal = itemRequest.unitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity()));
            item.setTotalPrice(itemTotal);

            total = total.add(itemTotal);
            order.addItem(item);
        }

        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(Optional<OrderStatus> status) {
        return status.map(orderRepository::findByStatus)
                .orElseGet(orderRepository::findAll);
    }

    public Order updateStatus(Long id, OrderStatus newStatus) {
        Order order = getOrder(id);

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException(
                    "Cannot change status of an order that is " + order.getStatus());
        }

        if (newStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("Use cancel endpoint to cancel an order");
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order updatePayment(Long id, BigDecimal paymentValue) {
        Order order = getOrder(id);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException(
                    "Cannot pay for an order that is cancelled or delivered");
        }

        if (paymentValue.intValue() < 0) {
            throw new InvalidPaymentValueException("Invalid payment value");
        }
        BigDecimal updatedPaymentValue =  order.getTotalPayment().add(paymentValue);
        order.setTotalPayment(updatedPaymentValue);
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStatusException(
                    "Only orders in PENDING status can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public int autoPromotePendingToProcessing() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            List<Payment> paymentsList = paymentRepository.findByOrderId(order.getId());
            BigDecimal finalPaymentValue = new BigDecimal(0);
            for (Payment payment : paymentsList) {
                finalPaymentValue = finalPaymentValue.add(payment.getPaymentValue());
            }
            if (order.getTotalAmount().equals(finalPaymentValue)) {
                order.setStatus(OrderStatus.PROCESSING);
            }
        }
        orderRepository.saveAll(pendingOrders);
        return pendingOrders.size();
    }
}

