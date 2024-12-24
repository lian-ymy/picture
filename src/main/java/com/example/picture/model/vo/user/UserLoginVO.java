package com.example.picture.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class UserLoginVO implements Serializable {
    /**
     * id  唯一标识
     */
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户身份 0 普通用户 1 管理员用户
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
