package com.example.picture.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id  唯一标识
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 用户个人简介
     */
    private String profile;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态
     */
    private Integer userStatus;

    /**
     * 登录密码
     */
    private String userPassword;

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
     * 用户身份 0 普通用户 1 管理员用户
     */
    private Integer userRole;

    /**
     * vip过期时间
     */
    private Date vipExpireTime;

    /**
     * vip兑换码
     */
    private String vipCode;

    /**
     * vip编号
     */
    private Long vipNumber;

    /**
     * 分享码
     */
    private String shareCode;

    /**
     * 邀请人id
     */
    private Long inviteUserId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}