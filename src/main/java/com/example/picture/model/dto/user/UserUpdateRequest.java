package com.example.picture.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * 用户编号
     */
    private long id;

    /**
     * 用户名
     */
    private String userName;

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

    private final long serialVersionUID = 1L;
}
