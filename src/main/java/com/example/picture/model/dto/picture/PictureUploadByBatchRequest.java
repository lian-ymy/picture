package com.example.picture.model.dto.picture;

import lombok.Data;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureUploadByBatchRequest {
    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;
}
