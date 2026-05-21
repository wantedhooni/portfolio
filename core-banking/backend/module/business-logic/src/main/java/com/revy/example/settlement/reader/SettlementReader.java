package com.revy.example.settlement.reader;

import com.revy.example.settlement.reader.dto.SettlementResult;
import com.revy.example.settlement.reader.dto.SettlementSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SettlementReader {

    Optional<SettlementResult> findById(Long id);

    Page<SettlementResult> search(Pageable pageable, SettlementSearchCondition condition);
}
