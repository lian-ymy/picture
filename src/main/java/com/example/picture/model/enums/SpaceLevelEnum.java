package com.example.picture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Getter
public enum SpaceLevelEnum {
    NORMAL("普通空间", 0, 100, 1024L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1024L * 1024 * 1024 * 1024),
    ENTERPRISE("企业版", 2, 10000, 1024L * 1024 * 1024 * 1024 * 1024);

    private final String text;

    private final int value;

    private final long maxCount;

    private final long maxSize;

    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }


    public static SpaceLevelEnum getSpaceLevelByValue(int value) {
        if(ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnunm : SpaceLevelEnum.values()) {
            if(spaceLevelEnunm.getValue() == value) {
                return spaceLevelEnunm;
            }
        }
        return null;
    }

}
