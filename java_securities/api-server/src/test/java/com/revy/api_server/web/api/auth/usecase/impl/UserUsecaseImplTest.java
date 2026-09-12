package com.revy.api_server.web.api.auth.usecase.impl;

import com.revy.api_server.application.web.api.auth.usecase.impl.UserUsecaseImpl;
import com.revy.securities.domain.user.User;
import com.revy.securities.domain.user.UserDetail;
import com.revy.securities.domain.user.UserStatus;
import com.revy.securities.domain.user.repo.UserRepository;
import com.revy.api_server.application.web.api.auth.usecase.dto.UserInfoResult;
import com.revy.common.error.ApiException;
import com.revy.common.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 서비스 테스트")
class UserUsecaseImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUsecaseImpl userService;

    @Test
    @DisplayName("사용자가 없으면 예외가 발생한다")
    void getUserInfo_userNotFound_throwsApiException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(1L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
    }

    @Test
    @DisplayName("사용자 정보 조회 시 프로필을 반환한다")
    void getUserInfo_success_returnsProfile() {
        User user = new User();
        user.setEmail("a@b.com");
        user.setStatus(UserStatus.ACTIVE);
        UserDetail detail = UserDetail.builder()
                .user(user)
                .name("name")
                .phone("010")
                .address("addr")
                .build();
        user.setDetail(detail);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserInfoResult result = userService.getUserInfo(2L);

        assertThat(result.getEmail()).isEqualTo("a@b.com");
        assertThat(result.getName()).isEqualTo("name");
        assertThat(result.getPhone()).isEqualTo("010");
        assertThat(result.getAddress()).isEqualTo("addr");
    }
}
