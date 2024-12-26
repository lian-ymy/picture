package com.example.picture.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级：0-普通空间 1-专业版 2-企业版
     */
    private Integer spaceLevel;

    /**
     * id
     */
    private Long id;

    /**
     * 最大容量
     */
    private Long maxSize;

    /**
     * 最大存储数量
     */
    private Long maxCount;
}
