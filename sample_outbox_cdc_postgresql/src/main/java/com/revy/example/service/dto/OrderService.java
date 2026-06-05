package com.revy.example.service.dto;

import com.revy.example.domain.PurchaseOrder;
import com.revy.example.domain.PurchaseOrderRepository;
import com.revy.example.domain.outbox.OutboxEvent;
import com.revy.example.service.OrderCreatedPayload;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class OrderService {

    private final PurchaseOrderRepository orderRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public OrderService(
            PurchaseOrderRepository orderRepository,
            EntityManager entityManager,
            ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        PurchaseOrder order = new PurchaseOrder(
                request.customerId(),
                request.productName(),
                request.quantity()
        );

        PurchaseOrder savedOrder = orderRepository.save(order);

        String payload = toJson(
                new OrderCreatedPayload(
                        savedOrder.getId(),
                        savedOrder.getCustomerId(),
                        savedOrder.getProductName(),
                        savedOrder.getQuantity()
                )
        );

        entityManager.persist(
            OutboxEvent.orderCreated(savedOrder.getId(), payload)
        );

        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    private String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    public record CreateOrderRequest(
            Long customerId,
            String productName,
            Integer quantity
    ) {
    }

    public record OrderResponse(
            Long id,
            Long customerId,
            String productName,
            Integer quantity,
            String status
    ) {
        public static OrderResponse from(PurchaseOrder order) {
            return new OrderResponse(
                    order.getId(),
                    order.getCustomerId(),
                    order.getProductName(),
                    order.getQuantity(),
                    order.getStatus().name()
            );
        }
    }
}