package com.revy.example.account.command.impl;

import com.revy.example.account.command.AccountCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AccountCommandImpl implements AccountCommand {
}
