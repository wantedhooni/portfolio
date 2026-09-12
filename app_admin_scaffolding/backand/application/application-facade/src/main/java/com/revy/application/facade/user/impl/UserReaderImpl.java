package com.revy.application.facade.user.impl;


import com.querydsl.core.BooleanBuilder;
import com.revy.application.facade.user.UserReader;
import com.revy.application.facade.user.dto.UserReaderDto;
import com.revy.common.utils.SearchFieldUtils;
import com.revy.common.web.api.search.SearchField;
import com.revy.domain.user.QUser;
import com.revy.domain.user.User;
import com.revy.domain.user.repository.UserRepository;
import com.revy.utils.querydsl.ExpressionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserReaderImpl implements UserReader {

    private static final QUser Q_USER = QUser.user;

    private final UserRepository userRepository;


    @Override
    public Optional<UserReaderDto.AuthUser> getByAuthUserId(UUID userId) {
        return userRepository.findOne(QUser.user.id.eq(userId)).map(this::toAuthUser);
    }

    @Override
    public Optional<UserReaderDto.AuthUser> getAuthUserByEmail(String email) {
        return userRepository.findOne(QUser.user.email.eq(email)).map(this::toAuthUser);
    }

    @Override
    public boolean hasAnySecurityData() {
        return userRepository.findAll(QUser.user.id.isNotNull(), PageRequest.of(1, 1)).getTotalElements() > 0;
    }

    @Override
    public Optional<UserReaderDto.UserView> getViewById(UUID id) {
        return userRepository.findOne(Q_USER.id.eq(id)).map(this::toView);
    }

    @Override
    public UserReaderDto.UserPage getPage(int page, int size, String sortBy, String sortDirection, String paramQuery) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Sort sort = Sort.by(Sort.Direction.valueOf(sortDirection), "id");

        if (StringUtils.isNotBlank(sortBy)) {
            sort = Sort.by(Sort.Direction.valueOf(sortDirection), sortBy);
        }

        BooleanBuilder predicate = buildSearchPredicate(paramQuery);
        log.debug("predicate:{}", predicate);

        PageRequest pageable = PageRequest.of(safePage, safeSize, sort);

        Page<User> result = userRepository.findAll(predicate, pageable);

        return new UserReaderDto.UserPage(result.getContent().stream().map(this::toView).toList(),
                                          result.getTotalElements(), safePage, safeSize);
    }

    private BooleanBuilder buildSearchPredicate(String paramQuery) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.isBlank(paramQuery)) {
            return predicate;
        }
        List<SearchField> fields = SearchFieldUtils.buildSearchField(paramQuery);
        log.debug("fields: {}", fields);

        fields.forEach(field -> {
            switch (field.fieldName()) {
                case "keyword" -> {
                    predicate.or(ExpressionUtil.like(Q_USER.email, field.value()));
                    predicate.or(ExpressionUtil.like(Q_USER.firstName, field.value()));
                    predicate.or(ExpressionUtil.like(Q_USER.lastName, field.value()));
                    predicate.or(ExpressionUtil.like(Q_USER.nickName, field.value()));
                }
                case "email" -> {
                    predicate.and(ExpressionUtil.like(Q_USER.email, field.value()));
                }
                case "firstName" -> {
                    predicate.and(ExpressionUtil.like(Q_USER.firstName, field.value()));
                }
                case "lastName" -> {
                    predicate.and(ExpressionUtil.like(Q_USER.lastName, field.value()));
                }
                case "nickName" -> {
                    predicate.and(ExpressionUtil.like(Q_USER.nickName, field.value()));
                }
                case "status" -> {
                    //TODO:Revy 고민좀 해보자
                }
                default -> {
                    // unsupported filter field is ignored intentionally
                }
            }
        });
        return predicate;
    }

    private UserReaderDto.AuthUser toAuthUser(User user) {
        Set<String> roleNames = user.getPermissions().stream().map(Enum::name).collect(Collectors.toSet());

        return new UserReaderDto.AuthUser(user.getId(), user.getEmail(), user.getPassword(), user.getStatus(),
                                          user.isEnabled(), roleNames);
    }

    private UserReaderDto.UserView toView(User user) {
        Set<String> permissions = user.getPermissions().stream().map(Enum::name).collect(Collectors.toSet());
        return new UserReaderDto.UserView(user.getId(),
                                          user.getEmail(),
                                          user.getFirstName(),
                                          user.getLastName(),
                                          user.getNickName(),
                                          user.getStatus(),
                                          user.getFailCount(),
                                          user.isEnabled(),
                                          permissions,
                                          user.getCreatedAt(),
                                          user.getUpdatedAt());
    }
}
