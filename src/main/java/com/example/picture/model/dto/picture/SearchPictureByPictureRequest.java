package com.example.picture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片id
     */
    private Long pictureId;
}
