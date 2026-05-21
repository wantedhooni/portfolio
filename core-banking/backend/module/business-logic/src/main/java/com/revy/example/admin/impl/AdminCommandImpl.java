package com.revy.example.admin.impl;

import com.revy.example.admin.AdminCommand;
import com.revy.example.admin.AdminReader;
import com.revy.example.admin.dto.RegisterAdminCommand;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.admin.Admin;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class AdminCommandImpl implements AdminCommand {

    private final EntityManager entityManager;
    private final AdminReader   adminReader;

    @Override
    public Long register(RegisterAdminCommand command) {
        if (adminReader.existsByEmail(command.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Admin admin = Admin.create(command.email(), command.encodedPassword(), command.name());
        entityManager.persist(admin);
        // TODO:REVY - EVENT 발행(AdminRegistered) - commit after
        return admin.getId();
    }

    @Override
    public void updateName(Long adminId, String name) {
        loadAdmin(adminId).updateName(name);
        // TODO:REVY - EVENT 발행(AdminNameUpdated) - commit after
    }

    @Override
    public void changePassword(Long adminId, String encodedPassword) {
        loadAdmin(adminId).updatePassword(encodedPassword);
        // TODO:REVY - EVENT 발행(AdminPasswordChanged) - commit after
    }

    @Override
    public void delete(Long adminId) {
        entityManager.remove(loadAdmin(adminId));
        // TODO:REVY - EVENT 발행(AdminDeleted) - commit after
    }

    private Admin loadAdmin(Long adminId) {
        Admin admin = entityManager.find(Admin.class, adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
        return admin;
    }
}
