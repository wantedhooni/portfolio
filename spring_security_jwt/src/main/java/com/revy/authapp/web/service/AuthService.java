package com.revy.authapp.web.service;

import com.revy.authapp.web.service.dto.LoginCommand;
import com.revy.authapp.web.service.dto.LoginResult;
import com.revy.authapp.web.service.dto.SignupCommand;

public interface AuthService {
    Long signup(SignupCommand signupCommand);

    LoginResult login(LoginCommand loginCommand);
}
