package com.revy.example.admin.api.admin.usecase.impl;

import com.revy.example.admin.api.admin.payload.AdminCreatePayload;
import com.revy.example.admin.api.admin.payload.AdminSearchPayload;
import com.revy.example.admin.api.admin.payload.AdminUpdatePayload;
import com.revy.example.admin.api.admin.usecase.AdminUseCase;
import com.revy.example.admin.command.AdminCommand;
import com.revy.example.admin.reader.AdminReader;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminUseCaseImpl implements AdminUseCase {

    private final AdminReader adminReader;
    private final AdminCommand adminCommand;
    private final PasswordEncoder passwordEncoder;


    private Admin getAdmin(Long id) {
        return adminReader.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
