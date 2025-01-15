package com.example.picture.model.vo.picture;

import com.example.picture.api.aliyunai.model.GetPictureByTextTaskResponse;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureTextVO implements Serializable {
    private Output output;

    @Data
    public static class Output {
        @SerializedName("task_id")
        private String taskId;
        @SerializedName("task_status")
        private String taskStatus;
        private String code;
        private String message;
        private String url;
        @SerializedName("task_metrics")
        private GetPictureByTextTaskResponse.TaskMetrics taskMetrics;
    }

    private GetPictureByTextTaskResponse.Usage usage;

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
