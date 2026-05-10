package com.revy.example.account.reader.impl;

import com.revy.example.account.reader.AccountReader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class AccountReaderImpl implements AccountReader {
}
