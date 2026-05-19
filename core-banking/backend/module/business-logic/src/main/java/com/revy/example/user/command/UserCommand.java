package com.revy.example.user.command;

import com.revy.example.user.command.dto.RegisterUserCommand;

public interface UserCommand {

    /** 회원 가입 — email 중복 시 EmailAlreadyExistsException */
    Long register(RegisterUserCommand command);

    void updateName(Long userId, String name);

    /** 비밀번호 변경 — encodedPassword 전달 (호출부에서 인코딩) */
    void changePassword(Long userId, String encodedPassword);

    void delete(Long userId);
}
