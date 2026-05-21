package com.revy.example.settlement.command;

import com.revy.example.settlement.command.dto.CreateSettlementCommand;

public interface SettlementCommand {

    Long create(CreateSettlementCommand command);

    void settle(Long id);

    void fail(Long id, String reason);

    void cancel(Long id);
}
