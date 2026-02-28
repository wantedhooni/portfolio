package com.revy.sample.jqgrid.filter.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FilterOperation {
    Equal("eq"),
    NotEqual("ne"),
    LessThan("lt"),
    LessThanEqual("le"),
    GreaterThan("gt"),
    GreaterThanEqual("ge"),
    IsNull("nu"),
    IsNotNull("nn"),
    In("cn"),
    NotIn("nc");

    private String opCode;

    FilterOperation(String op) {
        this.opCode = op;
    }

    public String getOp() {
        return opCode;
    }

    @JsonCreator
    public static FilterOperation forValue(String value) {
        for(FilterOperation t :  FilterOperation.values()) {
            if(t.getOp().equalsIgnoreCase(value)) {
                return t;
            }
        }

        return null;
    }

    @JsonValue
    public String toValue() {
        return getOp();
    }
}
