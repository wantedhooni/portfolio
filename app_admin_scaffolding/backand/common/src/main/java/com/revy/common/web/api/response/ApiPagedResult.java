package com.revy.common.web.api.response;

import java.util.List;

public record ApiPagedResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {}
