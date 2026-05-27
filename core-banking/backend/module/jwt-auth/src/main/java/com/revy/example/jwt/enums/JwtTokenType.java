package com.revy.example.jwt.enums;


import com.revy.example.common.enums.ExposedEnum;

/**
 * JWT 토큰의 용도를 구분하는 타입입니다 .
 */

public enum JwtTokenType implements ExposedEnum {
    ACCESS, REFRESH
}
