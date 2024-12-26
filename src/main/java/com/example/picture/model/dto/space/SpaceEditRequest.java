package com.example.picture.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class SpaceEditRequest implements Serializable {
    /**
     * 空间id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;
}
