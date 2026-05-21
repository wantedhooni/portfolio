package com.revy.example.admin.api.rbac;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.rbac.payload.RolePayload;
import com.revy.example.admin.api.rbac.usecase.RoleUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import com.revy.example.domain.admin.AdminPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1)
@RequiredArgsConstructor
public class RoleController {

    private final RoleUseCase roleUseCase;

    /** 지원 권한 목록 조회 */
    @GetMapping("/role/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<List<String>> listPermissions() {
        List<String> perms = Arrays.stream(AdminPermission.values()).map(Enum::name).toList();
        return ApiResponse.ok(perms);
    }

    @GetMapping("/role")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiPageResponse<RolePayload.ModelResponse> list(Pageable pageable, RolePayload.SearchRequest request) {
        PageImpl<RolePayload.ModelResponse> page = roleUseCase.search(pageable, request);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @GetMapping("/role/{id}")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    public ApiResponse<RolePayload.ModelResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(roleUseCase.getRole(id));
    }

    @PostMapping("/role")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ApiResponse<Long> create(@RequestBody @Valid RolePayload.CreateRequest request) {
        return ApiResponse.ok(roleUseCase.createRole(request));
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @RequestBody @Valid RolePayload.UpdateRequest request) {
        roleUseCase.updateRole(id, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/role/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_WRITE')")
    public void delete(@PathVariable Long id) {
        roleUseCase.deleteRole(id);
    }

    /** 어드민에 역할 부여 */
    @PostMapping("/admin/{adminId}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ADMIN_WRITE')")
    public ApiResponse<Void> assignRole(@PathVariable Long adminId, @PathVariable Long roleId) {
        roleUseCase.assignRole(adminId, roleId);
        return ApiResponse.ok();
    }

    /** 어드민에서 역할 제거 */
    @DeleteMapping("/admin/{adminId}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN_WRITE')")
    public void removeRole(@PathVariable Long adminId, @PathVariable Long roleId) {
        roleUseCase.removeRole(adminId, roleId);
    }
}
