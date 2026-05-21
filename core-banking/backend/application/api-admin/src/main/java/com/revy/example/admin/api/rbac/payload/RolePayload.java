package com.revy.example.admin.api.rbac.payload;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

public class RolePayload {

    public record CreateRequest(
            @NotBlank String name,
            String description,
            Set<String> permissions
    ) {}

    public record UpdateRequest(
            @NotBlank String name,
            String description,
            Set<String> permissions
    ) {}

    public record SearchRequest(String name) {}

    public record ModelResponse(
            Long id,
            String name,
            String description,
            Set<String> permissions
    ) {}

    public record AssignRoleRequest(Long roleId) {}

    /** 어드민 상세 응답에 포함되는 역할 요약 */
    public record RoleSummary(Long id, String name, List<String> permissions) {}
}
