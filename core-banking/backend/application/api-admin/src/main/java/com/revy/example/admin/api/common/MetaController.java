package com.revy.example.admin.api.common;

import com.revy.example.common.code.CodeInfoReader;
import com.revy.example.common.dto.CodeInfo;
import com.revy.example.common.enums.ExposedEnum;
import com.revy.example.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 운영 화면에서 사용하는 공통 메타 enum 옵션을 제공하는 컨트롤러.
 *
 * <p>{@link ExposedEnum}을 구현한 모든 enum을 기동 시점에 클래스패스 스캔하여
 * {@code (enum 키 → 옵션 목록)} 형태로 캐싱합니다. 프론트엔드는 단일 호출로 모든
 * 셀렉트 옵션과 i18n 메타데이터를 한 번에 받습니다.
 */
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/meta")
@RequiredArgsConstructor
public class MetaController {

    private final CodeInfoReader codeInfoReader;


    /**
     * 프론트엔드 셀렉트 옵션용 enum 메타 정보를 일괄 조회합니다.
     */
    @GetMapping("/codes")
    public ApiResponse<Map<String, List<CodeInfo>>> codes() {
        return ApiResponse.ok(codeInfoReader.getCodeInfos());
    }
}
