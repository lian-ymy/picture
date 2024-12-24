package com.example.picture.model.dto.file;

import lombok.Data;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class UploadPictureResult {
    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String pirFormat;
}
