package com.revy.domain.admin.repository;

import com.revy.domain.admin.AdminPermission;
import com.revy.domain.base.BaseRepository;

import java.util.UUID;

public interface PermissionRepository extends BaseRepository<AdminPermission, UUID> {
}
