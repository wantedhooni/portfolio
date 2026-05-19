package com.revy.example.admin.api.admin.usecase.impl;

import com.revy.example.admin.api.admin.payload.AdminPayload;
import com.revy.example.admin.api.admin.usecase.AdminUseCase;
import com.revy.example.admin.AdminCommand;
import com.revy.example.admin.AdminReader;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.Admin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUseCaseImpl implements AdminUseCase {

    private final AdminReader adminReader;
    private final AdminCommand adminCommand;
    private final PasswordEncoder passwordEncoder;


    @Override
    public AdminPayload.ModelResponse getAdmin(Long id) {
        var result = this.findById(id);
        return map(result);
    }

    @Override
    public PageImpl<AdminPayload.ModelResponse> search(Pageable pageable, AdminPayload.SearchRequest searchRequest) {
        Page<Admin> result = adminReader.search(pageable, searchRequest.name());
        return new PageImpl<AdminPayload.ModelResponse>(map(result.getContent()), pageable, result.getTotalElements());
    }

    private List<AdminPayload.ModelResponse> map(List<Admin> content) {
        return content.stream().map(this::map).toList();
    }

    private AdminPayload.ModelResponse map(Admin admin) {
        return new AdminPayload.ModelResponse(admin.getId(), admin.getEmail(), admin.getName());
    }


    private Admin findById(Long id) {
        return adminReader.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

}
