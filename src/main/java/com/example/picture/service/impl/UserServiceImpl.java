package com.example.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.mapper.UserMapper;
import com.example.picture.model.User;
import com.example.picture.model.dto.user.UserQueryRequest;
import com.example.picture.model.enums.UserRoleEnum;
import com.example.picture.model.vo.user.UserLoginVO;
import com.example.picture.model.vo.user.UserVO;
import com.example.picture.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.picture.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author lian
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-12-11 15:54:48
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //对数据库的操作可以通过接口实现类进行实现，也可以通过mapper中的方法实现
    @Resource
    UserMapper userMapper;

    /**
     * 盐值、混淆密码
     */
    private static final String SALT = "ymy";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
//校验
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword) || StrUtil.isBlank(checkPassword)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户名长度小于三位！");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码长度小于八位");
        }
//判断账户是否包含特殊字符，如果包含特殊字符就抛出异常
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户账户包含特殊字符！！");
        }
//密码与校验密码相同
//字符串比较使用equals进行比较
        if (!userPassword.equals(checkPassword)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "输入密码与校验密码不同！！");
        }
//账户不能重复，这一步是对数据库进行查询，如果说条件不满足就直接返回，防止有后续的冗余操作
        Long count = query().eq("userAccount", userAccount).count();
        if (count > 0) {
            throw new BussinessException(ErrorCode.REPEATED_USER, "用户账号重复！！");
        }
//加密，将密码经过MD5单向处理之后保存到数据库中
//将真实密码与盐值共同进行加密，起到混淆视听的作用
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

//向数据库中插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean save = save(user);
        if (!save) {
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "数据库插入数据失败！！");
        }
        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
//1、校验
        if (CharSequenceUtil.isBlank(userAccount) || CharSequenceUtil.isBlank(userPassword)) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "请求参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户名长度小于三位！");
        }
        if (userPassword.length() < 8) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "密码长度小于八位");
        }
//判断账户是否包含特殊字符，如果包含特殊字符就抛出异常
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(userAccount);
        if (matcher.find()) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "用户账户包含特殊字符！！");
        }
//2、查询用户信息
//对密码进行加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
//查询数据库中是否有对应登录用户的信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = this.userMapper.selectOne(userQueryWrapper);
        if (user == null) {
//如果这里发现了错误，使用日志打印错误信息，最好使用英文，方便其他应用对信息进行检索
            log.info("User login failed, userAccount can not match password");
            throw new BussinessException(ErrorCode.NULL_ERROR, "查询不到对应用户信息！！");
        }

//3、记录用户登录态
//这里获取对应会话，通过session设置属性值传递给前端进行判断
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

//返回脱敏后的用户信息，用于在网页中展示出来
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
//先判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "用户未登录！！");
        }
        Long id = currentUser.getId();
//查询用户信息
        User user = this.userMapper.selectById(id);
        if (user == null) {
            throw new BussinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }

    public UserLoginVO getLoginUserVO(User user) {
        if (user == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "用户信息为空！！");
        }
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setUserRole(UserRoleEnum.getEnumByValue(user.getUserRole()).getValue());
        return userLoginVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
//先判断是否登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR, "用户未登录！！");
        }
//移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).toList();
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BussinessException(ErrorCode.NULL_ERROR);
        }
        long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String profile = userQueryRequest.getProfile();
        String userRole = userQueryRequest.getUserRole();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(userRole), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(profile), "profile", profile);
        return queryWrapper;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.getEnumByValue(user.getUserRole()) == UserRoleEnum.ADMIN;
    }
}




