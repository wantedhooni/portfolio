package com.revy.example.admin;

import com.revy.example.admin.dto.RegisterAdminCommand;

public interface AdminCommand {

    Long register(RegisterAdminCommand command);

    void updateName(Long adminId, String name);

    void changePassword(Long adminId, String encodedPassword);

    void delete(Long adminId);
}
