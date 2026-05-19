package com.revy.example.admin;

import com.revy.example.domain.admin.Admin;

public interface AdminCommand {

    Admin save(Admin admin);

    void delete(Admin admin);
}
