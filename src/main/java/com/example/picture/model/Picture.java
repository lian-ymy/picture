package com.example.picture.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片
 * @TableName picture
 */
@TableName(value ="picture")
@Data
public class Picture implements Serializable {
    /**
     * id  唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 关联的用户id
     */
    private Long userId;

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
    private Integer category;

    /**
     * 图片标签
     */
    private String tags;

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
    private Integer picScale;

    /**
     * 图片格式
     */
    private Integer pirFormat;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 Id
     */
    private Long reviewUserId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}