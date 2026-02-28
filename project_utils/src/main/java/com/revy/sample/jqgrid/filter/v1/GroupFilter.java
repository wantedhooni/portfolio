package com.revy.sample.jqgrid.filter.v1;

import lombok.Data;

import java.util.Collection;

@Data
public class GroupFilter {
    /**
     * 다중 필터 적용 방식
     */
    GroupFilterOperation groupOp;
    /**
     * 필터 콜렉션
     */
    Collection<Filter> rules;

    @Data
    public static class Filter {
        /**
         * 필드
         */
        String field;
        /**
         * 비교값
         */
        String data;
        /**
         * 필터 방법
         */
        FilterOperation op;
    }
}
