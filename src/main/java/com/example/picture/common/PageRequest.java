package com.example.picture.common;

import lombok.Data;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PageRequest {
    /**
     * 页面大小，设置默认值，在实际参数为空时可以不用进行多余判断
     */
    private int pageSize = 10;

    /**
     * 页面编号
     */
    private int current = 1;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
