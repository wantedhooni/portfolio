package com.revy.application.facade.user.impl;

import com.revy.application.facade.user.UserProcessor;
import com.revy.application.facade.user.UserReader;
import com.revy.application.facade.user.dto.SignupUserDto;
import com.revy.application.facade.user.dto.UserReaderDto;
import com.revy.domain.user.User;
import com.revy.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProcessorImpl implements UserProcessor {
    private final UserReader userReader;
    private final UserRepository userRepository;

    @Override
    public SignupUserDto.Result signup(String email, String hashedPassword, String firstName, String lastName,
                                       String nickName) {
        Optional<UserReaderDto.AuthUser> oldUser = userReader.getAuthUserByEmail(email);

        if (oldUser.isPresent()) {
            throw new IllegalArgumentException("존재하는 계정입니다.");
        }

        var siginUser = User.createUser(
                email,
                hashedPassword
                , firstName, lastName, nickName
        );
        siginUser = userRepository.save(siginUser);

        return new SignupUserDto.Result(siginUser.getId().toString());
    }

}
