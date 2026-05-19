package com.revy.example.admin.api.admin.usecase;

import com.revy.example.admin.api.admin.payload.AdminPayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface AdminUseCase {
    AdminPayload.ModelResponse getAdmin(Long id);

    PageImpl<AdminPayload.ModelResponse> search(Pageable pageable, AdminPayload.SearchRequest searchRequest);
}
