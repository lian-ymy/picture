package com.example.picture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Getter
public enum UserRoleEnum {

    USER("用户", "user", 0),
    ADMIN("管理员", "admin", 1);

    private final String text;

    private final String value;

    private final int role;

    UserRoleEnum(String text, String value, int role) {
        this.text = text;
        this.value = value;
        this.role = role;
    }

    public static UserRoleEnum getEnumByValue(String value) {
        if(ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : values()) {
            if (anEnum.getValue().equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public static UserRoleEnum getEnumByValue(int value) {
        if(ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum anEnum : values()) {
            if (anEnum.getRole() == value) {
                return anEnum;
            }
        }
        return null;
    }
}
