package com.revy.common.domain.enums.user;

public enum UserPermission {
    SIGNED_UP,          // 가입 완료
    EMAIL_VERIFIED,     // 이메일 인증 완료
    PROFILE_COMPLETED,  // 프로필 입력 완료
    SERVICE_USE // 서비스 사용 권한
}

/*
public enum UserPermission {
    SERVICE_USE,            // 서비스 사용 권한
    PAYMENT_CREATE,         // 결제 생성 권한
    PAYMENT_REFUND         // 환불 처리 권한
}
 */