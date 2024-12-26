package com.example.picture.model.dto.picture;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureEditRequest implements Serializable {
    /**
     * 图片编号
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片描述
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tags;

    /**
     * 图片空间
     */
    private Long spaceId;

    @Serial
    private static final long serialVersionUID = 1L;
}
