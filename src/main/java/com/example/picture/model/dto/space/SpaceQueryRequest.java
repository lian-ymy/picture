package com.example.picture.model.dto.space;

import com.example.picture.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceQueryRequest extends PageRequest implements Serializable {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级：0-普通空间 1-专业版 2-企业版
     */
    private Integer spaceLevel;
}
