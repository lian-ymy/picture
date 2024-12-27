package com.example.picture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class SearchPictureByColorRequest implements Serializable {
    /**
     * 图片色调
     */
    private String picColor;

    /**
     * 空间id
     */
    private Long spaceId;
}
