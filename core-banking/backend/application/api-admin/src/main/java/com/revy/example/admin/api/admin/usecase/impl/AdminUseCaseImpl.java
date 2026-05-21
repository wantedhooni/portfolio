package com.revy.example.admin.api.admin.usecase.impl;

import com.revy.example.admin.AdminReader;
import com.revy.example.admin.api.admin.payload.AdminPayload;
import com.revy.example.admin.api.admin.usecase.AdminUseCase;
import com.revy.example.admin.dto.AdminResult;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUseCaseImpl implements AdminUseCase {

    private final AdminReader adminReader;

    @Override
    public AdminPayload.ModelResponse getAdmin(Long id) {
        AdminResult admin = adminReader.findByIdWithRoles(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return map(admin);
    }

    @Override
    public PageImpl<AdminPayload.ModelResponse> search(Pageable pageable, AdminPayload.SearchRequest searchRequest) {
        Page<AdminResult> result = adminReader.search(pageable, searchRequest.name());
        return new PageImpl<>(mapList(result.getContent()), pageable, result.getTotalElements());
    }

    private List<AdminPayload.ModelResponse> mapList(List<AdminResult> content) {
        return content.stream().map(this::map).toList();
    }

    private AdminPayload.ModelResponse map(AdminResult admin) {
        List<AdminPayload.RoleSummary> roles = admin.roles().stream()
                .map(r -> new AdminPayload.RoleSummary(r.id(), r.name(), r.permissions()))
                .toList();
        return new AdminPayload.ModelResponse(admin.id(), admin.email(), admin.name(), roles);
    }
}
