package com.example.picture.aop;

import com.example.picture.annotation.AuthCheck;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.model.User;
import com.example.picture.model.enums.UserRoleEnum;
import com.example.picture.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    UserService userService;

    /**
     * 执行拦截
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，放行
        if(mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        //有角色限制，必须有管理员权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限拒绝
        if(userRoleEnum == null) {
            throw new BussinessException(ErrorCode.NO_AUTHOR);
        }
        //要求必须有管理员权限但是没有
        if(userRoleEnum.getRole() < mustRoleEnum.getRole()) {
            throw new BussinessException(ErrorCode.NO_AUTHOR);
        }
        //有管理员权限，放行
        return joinPoint.proceed();
    }
}
