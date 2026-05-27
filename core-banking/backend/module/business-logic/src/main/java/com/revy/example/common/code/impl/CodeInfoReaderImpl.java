package com.revy.example.common.code.impl;

import com.revy.example.common.code.CodeInfoReader;
import com.revy.example.common.dto.CodeInfo;
import com.revy.example.common.enums.ExposedEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CodeInfoReaderImpl implements CodeInfoReader {
    private final Map<String, List<CodeInfo>> cache;

    public CodeInfoReaderImpl(@Value("${meta.scan-package:com.revy}") String basePackage) {
        this.cache = scanEnums(basePackage);
    }

    @Override
    public Map<String, List<CodeInfo>> getCodeInfos() {
        return cache;
    }

    private Map<String, List<CodeInfo>> scanEnums(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ExposedEnum.class));

        Map<String, List<CodeInfo>> map = new LinkedHashMap<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                Class<?> cls = Class.forName(bd.getBeanClassName());
                if (!cls.isEnum() || !ExposedEnum.class.isAssignableFrom(cls)) {
                    continue;
                }

                String key = cls.getSimpleName();

                List<CodeInfo> options = Arrays.stream(cls.getEnumConstants())
                    .map(ExposedEnum.class::cast)
                    .map(e -> new CodeInfo(e.getCode(), e.getDefaultMessage()))
                    .toList();

                map.put(key, options);
            } catch (ClassNotFoundException ignore) {
                // 스캔 가능했지만 로딩 불가한 클래스는 무시
            }
        }
        return map;
    }
}
