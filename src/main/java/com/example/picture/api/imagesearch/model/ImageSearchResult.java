package com.example.picture.api.imagesearch.model;

import lombok.Data;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 原图地址
     */
    private String originUrl;
}
