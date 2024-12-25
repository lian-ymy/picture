package com.example.picture.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("审核中", 0),
    PASS("审核通过", 1),
    REJECT("审核拒绝", 2);

    private final String message;
    private final int value;

    PictureReviewStatusEnum(String message, int value) {
        this.message = message;
        this.value = value;
    }

    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if(ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum anEnum : PictureReviewStatusEnum.values()) {
            if (anEnum.getValue() == value) {
                return anEnum;
            }
        }
        return null;
    }

}
