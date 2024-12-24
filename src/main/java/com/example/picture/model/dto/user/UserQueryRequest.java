package com.example.picture.model.dto.user;


import com.example.picture.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户编号
     */
    private long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

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
