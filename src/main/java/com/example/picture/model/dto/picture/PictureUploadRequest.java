package com.example.picture.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureUploadRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 图片编号
     */
    private Long pictureId;

    /**
     * 图片地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;
}
