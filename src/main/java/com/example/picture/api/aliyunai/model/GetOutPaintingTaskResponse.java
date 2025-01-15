package com.example.picture.api.aliyunai.model;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisTaskMetrics;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 输出
     */
    private Output output;

    /**
     * 请求唯一id
     */
    private String requestId;


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

        /**
         * 提交时间
         * 格式：yyyy-MM-dd HH:mm:ss
         */
        private String submitTime;

        /**
         * 调度时间
         * 格式：yyyy-MM-dd HH:mm:ss
         */
        private String scheduledTime;

        /**
         * 完成时间
         * 格式：yyyy-MM-dd HH:mm:ss
         */
        private String endTime;

        /**
         * 输出图片url
         */
        private String outputImageUrl;

        /**
         * 错误码
         */
        private String code;

        /**
         * 错误信息
         */
        private String message;

        /**
         * 任务指标
         */
        private TaskMetrics taskMetrics;
    }

    /**
     * 任务指标
     */
    @Data
    public static class TaskMetrics {
        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功数量
         */
        private Integer succeeded;

        /**
         * 失败数量
         */
        private Integer failed;
    }

}
