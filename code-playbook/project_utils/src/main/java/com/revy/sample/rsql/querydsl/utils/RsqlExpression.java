package com.revy.sample.rsql.querydsl.utils;

import com.revy.sample.rsql.querydsl.utils.enums.RsqlOperator;

public record RsqlExpression(
        String selector,
        RsqlOperator operator,
        RsqlArgument argument
) {
}
