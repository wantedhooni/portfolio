package com.revy.example.admin.command;

import com.revy.example.domain.admin.Admin;

public interface AdminCommand {

    Admin save(Admin admin);

    void delete(Admin admin);
}
