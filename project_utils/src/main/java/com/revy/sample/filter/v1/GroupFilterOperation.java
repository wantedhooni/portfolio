package com.revy.sample.filter.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupFilterOperation {
    And("AND"),
    Or("OR");

    private String groupop;

    GroupFilterOperation(String op) {
        this.groupop = op;
    }

    public String getGroupOp() {
        return groupop;
    }

    @JsonCreator
    public static GroupFilterOperation forValue(String value) {
        for (GroupFilterOperation t : GroupFilterOperation.values()) {
            if (t.getGroupOp().equalsIgnoreCase(value)) {
                return t;
            }
        }

        return null;
    }

    @JsonValue
    public String toValue() {
        return getGroupOp();
    }
}
