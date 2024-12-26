package com.example.picture.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间表
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级：0-普通空间 1-专业版 2-企业版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;

    /**
     * 最大容量
     */
    private Long maxSize;

    /**
     * 已使用容量
     */
    private Long totalSize;

    /**
     * 最大存储数量
     */
    private Long maxCount;

    /**
     * 已使用数量
     */
    private Long totalCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}