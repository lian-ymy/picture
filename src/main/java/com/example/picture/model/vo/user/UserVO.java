package com.example.picture.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class UserVO implements Serializable {

    /**
     * 用户编号
     */
    private long id;

    /**
     * 用户名
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
     * 用户简介
     */
    private String profile;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    private final long serialVersionUID = 1L;
}
