package com.revy.api.admin.server.api.administrator.role.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RolePayload {
    @Schema(name = "RolePayload.CreateCommandReq")
    public record CreateCommandReq(
            @NotBlank
            String name,
            String description
    ) {
    }

    @Schema(name = "RolePayload.UpdateCommandReq")
    public record UpdateCommandReq(
            String name,
            String description
    ) {
    }

    @Schema(name = "RolePayload.AddAdminCommandReq")
    public record AddAdminCommandReq(
            @NotNull
            String roleId,
            @NotNull
            String adminId
    ) {
    }

    @Schema(name = "RolePayload.Res")
    public record Res(
            String id,
            String name,
            String description,
            java.util.List<AdminRef> admins,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }

    @Schema(name = "RolePayload.AdminRef")
    public record AdminRef(
            String id,
            String email
    ) {
    }

    @Schema(name = "RolePayload.DeleteRes")
    public record DeleteRes(
            String id,
            boolean deleted,
            String message
    ) {
    }
}
