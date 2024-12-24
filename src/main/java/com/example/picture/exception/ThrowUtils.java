package com.example.picture.exception;

/**
 * 自定义抛出异常方法
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
public class ThrowUtils {

    /**
     * 条件成立抛出异常
     *
     * @param condition
     * @param exception
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throwIf(condition, new BussinessException(errorCode));
        }
    }

    /**
     * 条件成立抛出异常
     *
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String description) {
        if (condition) {
            throwIf(condition, new BussinessException(errorCode, description));
        }
    }
}
