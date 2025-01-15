package com.example.picture.api.aliyunai.model;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisTaskMetrics;
import com.alibaba.dashscope.threads.runs.Usage;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetPictureByTextTaskResponse {

    private Output output;

    @Data
    public static class Output {
        @SerializedName("task_id")
        private String taskId;
        @SerializedName("task_status")
        private String taskStatus;
        private String code;
        private String message;
        private List<Map<String, String>> results;
        @SerializedName("task_metrics")
        private TaskMetrics taskMetrics;
    }

    private Usage usage;

    private String requestId;

    @Data
    public static class Usage {
        @SerializedName("image_count")
        private Integer imageCount;
    }

    // Inner classes for task_metrics and usage
    @Data
    public static class TaskMetrics {
        @SerializedName("TOTAL")
        private Integer total;
        @SerializedName("SUCCEEDED")
        private Integer succeeded;
        @SerializedName("FAILED")
        private Integer failed;
// Getters and Setters for total, succeeded, and failed
    }

}