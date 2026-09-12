package com.revy.application.facade.administrator.admin;


import com.revy.application.facade.administrator.admin.dto.CreateAdminDto;
import com.revy.common.domain.enums.admin.AdminStatus;

import java.util.List;
import java.util.UUID;

public interface AdminProcessor {
    CreateAdminDto.Result createAdmin(String email, String rawPassword);

    void update(UUID adminId,
                String email,
                String rawPassword,
                AdminStatus status,
                Boolean enabled,
                List<String> roleIds);

    void delete(UUID adminId);
}
