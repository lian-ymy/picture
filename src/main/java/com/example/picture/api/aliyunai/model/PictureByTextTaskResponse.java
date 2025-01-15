package com.example.picture.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PictureByTextTaskResponse {

    private Output output;

    /**
     * 输出
     */
    @Data
    public static class Output {

        /**
         * 任务id
         */
        private String taskId;

        /**
         * 任务状态
         */
        private String taskStatus;
    }

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 请求id
     */
    private String requestId;
}
