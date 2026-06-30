package com.revy.example.admin.command.impl;

import com.revy.example.admin.command.AdminCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = false)
public class AdminCommandImpl implements AdminCommand {
}
