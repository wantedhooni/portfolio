package com.revy.user_service.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
class AuthController {

    @PostMapping("/signUp")
    void signUp(@RequestBody String body) {
    }

    @PostMapping("/login")
    void login(){}

    @PostMapping("/logout")
    void logout(){}
}
