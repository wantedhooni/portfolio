package com.revy.example.order.reader;

import com.revy.example.order.reader.dto.OrderResult;
import com.revy.example.order.reader.dto.OrderSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderReader {

    Optional<OrderResult> findById(Long id);

    Page<OrderResult> search(Pageable pageable, OrderSearchCondition condition);
}
