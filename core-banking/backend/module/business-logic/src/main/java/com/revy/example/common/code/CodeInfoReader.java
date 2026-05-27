package com.revy.example.common.code;

import com.revy.example.common.dto.CodeInfo;

import java.util.List;
import java.util.Map;

public interface CodeInfoReader {
    Map<String, List<CodeInfo>> getCodeInfos();
}
