package com.revy.application.facade.administrator.admin.impl;

import com.querydsl.core.BooleanBuilder;
import com.revy.application.facade.administrator.admin.AdminReader;
import com.revy.application.facade.administrator.admin.dto.AdminReaderDto;
import com.revy.common.utils.SearchFieldUtils;
import com.revy.common.web.api.search.SearchField;
import com.revy.domain.admin.Admin;
import com.revy.domain.admin.AdminRole;
import com.revy.domain.admin.QAdmin;
import com.revy.domain.admin.QAdminRole;
import com.revy.domain.admin.repository.AdminRepository;
import com.revy.domain.admin.repository.RoleRepository;
import com.revy.utils.querydsl.ExpressionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReaderImpl implements AdminReader {
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "email",
            "status"
    );

    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;

    @Override
    public boolean hasAnySecurityData() {
        return adminRepository.findAll(QAdmin.admin.id.isNotNull(), PageRequest.of(1, 1)).getTotalElements() > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        return adminRepository.exists(QAdmin.admin.email.eq(email));
    }

    @Override
    public Optional<AdminReaderDto.RoleRef> getRoleByName(String roleName) {
        return roleRepository.findOne(QAdminRole.adminRole.name.eq(roleName))
                             .map(role -> new AdminReaderDto.RoleRef(role.getId(), role.getName()));
    }

    @Override
    public Optional<AdminReaderDto.AuthAdmin> getAuthAdminByEmail(String email) {
        return adminRepository.findOne(QAdmin.admin.email.eq(email)).map(this::toAuthAdmin);
    }

    @Override
    public Optional<AdminReaderDto.AuthAdmin> getAuthAdminById(UUID adminId) {
        return adminRepository.findOne(QAdmin.admin.id.eq(adminId)).map(this::toAuthAdmin);
    }

    @Override
    public Optional<AdminReaderDto.AdminView> getViewById(UUID adminId) {
        return adminRepository.findOne(QAdmin.admin.id.eq(adminId)).map(this::toAdminView);
    }

    @Override
    public AdminReaderDto.AdminPage getPage(int page, int size, String sortBy, String sortDirection,
                                            String paramQuery) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        Sort sort = resolveSort(sortBy, sortDirection);
        BooleanBuilder predicate = buildSearchPredicate(paramQuery);
        log.debug("predicate:{}", predicate);

        PageRequest pageable = PageRequest.of(safePage, safeSize, sort);
        Page<Admin> result = adminRepository.findAll(predicate, pageable);
        return new AdminReaderDto.AdminPage(result.getContent().stream().map(this::toAdminView).toList(),
                                            result.getTotalElements(), safePage, safeSize);
    }


    private AdminReaderDto.AuthAdmin toAuthAdmin(Admin admin) {
        RoleSnapshot roleSnapshot = buildRoleSnapshot(admin);
        return new AdminReaderDto.AuthAdmin(admin.getId(), admin.getEmail(), admin.getPassword(), admin.getStatus(),
                                            admin.isEnabled(), roleSnapshot.roleNames());
    }

    private AdminReaderDto.AdminView toAdminView(Admin admin) {
        RoleSnapshot roleSnapshot = buildRoleSnapshot(admin);
        return new AdminReaderDto.AdminView(admin.getId(), admin.getEmail(), admin.getStatus(), admin.isEnabled(),
                                            roleSnapshot.roleIds(), roleSnapshot.roleNames(), admin.getCreatedAt(),
                                            admin.getUpdatedAt());
    }

    // TODO: 정리 필요
    private BooleanBuilder buildSearchPredicate(String paramQuery) {
        BooleanBuilder predicate = new BooleanBuilder();

        if (!StringUtils.hasText(paramQuery)) {
            return predicate;
        }
        List<SearchField> fields = SearchFieldUtils.buildSearchField(paramQuery);
        log.debug("fields: {}", fields);
        fields.forEach(field -> {
            switch (field.fieldName()) {
                case "keyword" -> {
                    predicate.and(ExpressionUtil.like(QAdmin.admin.email, field.value()));
                }
//                case "email" -> {
//                    predicate.and(ExpressionUtil.like(QAdmin.admin.email, field.value()));
//                }
//                case "status" -> {
//                    predicate.and(ExpressionUtil.in(QAdmin.admin.status, field.value()));
//                }
                default -> {
                    // unsupported filter field is ignored intentionally
                }
            }
        });


        return predicate;
    }

    private Sort resolveSort(String sortBy, String sortDirection) {
        String field = StringUtils.hasText(sortBy) && ALLOWED_SORT_FIELDS.contains(sortBy)
                ? sortBy
                : DEFAULT_SORT_FIELD;

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (Exception ignored) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, field);
    }

    private RoleSnapshot buildRoleSnapshot(Admin admin) {
        Set<UUID> roleIds = new HashSet<>();
        Set<String> roleNames = new HashSet<>();

        for (AdminRole role : admin.getAdminRoles()) {
            roleIds.add(role.getId());
            roleNames.add(role.getName());
        }

        return new RoleSnapshot(roleIds, roleNames);
    }

    private record RoleSnapshot(
            Set<UUID> roleIds,
            Set<String> roleNames
    ) {
    }
}
