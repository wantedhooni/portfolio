package com.revy.application.facade.administrator.role;


import com.revy.application.facade.administrator.role.dto.RoleReaderDto;

import java.util.Optional;
import java.util.UUID;

public interface RoleReader {
    boolean existsByName(String name);

    Optional<RoleReaderDto.RoleView> getRoleViewById(UUID roleId);

    RoleReaderDto.RolePage getPage(int page, int size);
}
