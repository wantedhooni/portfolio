package com.revy.application.facade.administrator.admin;

public interface InitAdminProcessor {
    void initializeSecurityData(String email, String hashedPassword);
}
