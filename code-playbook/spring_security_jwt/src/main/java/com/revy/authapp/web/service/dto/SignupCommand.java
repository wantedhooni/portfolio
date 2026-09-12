package com.revy.authapp.web.service.dto;

/**
 *
 */
/*
TODO:Revy 레코드를 따라가니 이름이 이상하다.
public interface SignupCommand {
    String email();

    String password();

    String name();

    String phone();

    String address();
}
*/
public interface SignupCommand {
    String getEmail();

    String getPassword();

    String getName();

    String getPhone();

    String getAddress();
}