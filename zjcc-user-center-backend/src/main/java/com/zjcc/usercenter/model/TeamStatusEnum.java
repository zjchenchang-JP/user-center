package com.zjcc.usercenter.model;

import lombok.Getter;

/**
 * 队伍状态枚举
 * 私有=仅受邀者加入，加密=所有人可见但需密码验证 ?
 */
@Getter
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private int value;
    private String text;

    public static TeamStatusEnum getEnumByValue(Integer value){
        if (value == null){
            return null;
        }
        TeamStatusEnum[] values = TeamStatusEnum.values();
        for (TeamStatusEnum teamStatusEnum: values){
            if (teamStatusEnum.getValue()==value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}