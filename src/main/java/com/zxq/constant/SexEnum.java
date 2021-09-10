package com.zxq.constant;

/**
 * 选手性别枚举
 */
public enum SexEnum {

    MALE("male"),
    FEMALE("female");


    private String value;

    private SexEnum(String value) {
        this.value = value;
    }

    public String sex() {
        return this.value;
    }
}
