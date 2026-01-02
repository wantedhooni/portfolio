package com.revy.authapp.web.service.impl;

import com.revy.authapp.domain.user.User;
import com.revy.authapp.domain.user.repo.UserRepository;
import com.revy.authapp.web.common.ApiException;
import com.revy.authapp.web.common.ErrorCode;
import com.revy.authapp.web.service.UserService;
import com.revy.authapp.web.service.dto.UserInfoResult;
import com.revy.authapp.web.service.dto.impl.UserInfoResultImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserInfoResult getUserInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResultImpl
                .builder()
                .email(user.getEmail())
                .status(user.getStatus())
                .name(user.getDetail().getName())
                .phone(user.getDetail().getPhone())
                .address(user.getDetail().getAddress())
                .build();
    }
}
