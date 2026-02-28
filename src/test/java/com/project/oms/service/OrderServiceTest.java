package com.project.oms.service;

import com.project.oms.domain.Order;
import com.project.oms.domain.OrderStatus;
import com.project.oms.exception.InvalidOrderStatusException;
import com.project.oms.repository.OrderRepository;
import com.project.oms.web.dto.CreateOrderRequest;
import com.project.oms.web.dto.OrderItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest sampleRequest;

    @BeforeEach
    void setUp() {
        OrderItemRequest item = new OrderItemRequest(
                "P1",
                "Product 1",
                2,
                BigDecimal.valueOf(10)
        );
        sampleRequest = new CreateOrderRequest("customer-1", List.of(item));
    }

    @Test
    void createOrder_shouldPersistWithCalculatedTotal() {
        given(orderRepository.save(any(Order.class)))
                .willAnswer(invocation -> invocation.getArgument(0, Order.class));

        Order created = orderService.createOrder(sampleRequest);

        assertThat(created.getCustomerId()).isEqualTo("customer-1");
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(created.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(created.getItems()).hasSize(1);
    }

    @Test
    void createOrder_withoutItems_shouldFail() {
        CreateOrderRequest invalid = new CreateOrderRequest("c1", Collections.emptyList());

        assertThatThrownBy(() -> orderService.createOrder(invalid))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void cancelOrder_onlyPendingAllowed() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        Order canceled = orderService.cancelOrder(1L);

        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_nonPendingShouldFail() {
        Order order = new Order();
        order.setStatus(OrderStatus.PROCESSING);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    void autoPromotePendingToProcessing_updatesAllPendingOrders() {
        Order o1 = new Order();
        o1.setStatus(OrderStatus.PENDING);
        Order o2 = new Order();
        o2.setStatus(OrderStatus.PENDING);
        given(orderRepository.findByStatus(OrderStatus.PENDING))
                .willReturn(List.of(o1, o2));

        int updated = orderService.autoPromotePendingToProcessing();

        assertThat(updated).isEqualTo(2);
        ArgumentCaptor<List<Order>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(Order::getStatus)
                .containsOnly(OrderStatus.PROCESSING);
    }
}

