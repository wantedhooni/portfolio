package com.revy.example.admin.api.rbac.usecase;

import com.revy.example.admin.api.rbac.payload.RolePayload;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface RoleUseCase {

    RolePayload.ModelResponse getRole(Long id);

    PageImpl<RolePayload.ModelResponse> search(Pageable pageable, RolePayload.SearchRequest request);

    Long createRole(RolePayload.CreateRequest request);

    void updateRole(Long id, RolePayload.UpdateRequest request);

    void deleteRole(Long id);

    void assignRole(Long adminId, Long roleId);

    void removeRole(Long adminId, Long roleId);
}
