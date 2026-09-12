package com.revy.application.facade.administrator.admin;


import com.revy.application.facade.ViewReader;
import com.revy.application.facade.administrator.admin.dto.AdminReaderDto;

import java.util.Optional;
import java.util.UUID;

public interface AdminReader extends ViewReader<AdminReaderDto.AdminPage, AdminReaderDto.AdminView> {
    boolean hasAnySecurityData();

    boolean existsByEmail(String email);

    Optional<AdminReaderDto.RoleRef> getRoleByName(String roleName);

    Optional<AdminReaderDto.AuthAdmin> getAuthAdminByEmail(String email);

    Optional<AdminReaderDto.AuthAdmin> getAuthAdminById(UUID adminId);


}
