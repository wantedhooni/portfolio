package com.revy.example.admin.impl;

import com.revy.example.admin.AdminCommand;
import com.revy.example.domain.admin.Admin;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class AdminCommandImpl implements AdminCommand {

    private final EntityManager entityManager;

    @Override
    public Admin save(Admin admin) {
        entityManager.persist(admin);
        return admin;
    }

    @Override
    public void delete(Admin admin) {
        entityManager.remove(admin);
    }
}
