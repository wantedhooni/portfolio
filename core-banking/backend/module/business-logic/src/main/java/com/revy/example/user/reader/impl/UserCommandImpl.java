package com.revy.example.user.reader.impl;

import com.revy.example.user.reader.UserCommand;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UserCommandImpl implements UserCommand {
}
