package com.example.picture.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    /**
     * 模型
     */
    private String model = "image-out-painting";

    /**
     * 输入图像信息
     */
    private Input input;

    /**
     * 图像处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input implements Serializable {
        /**
         * 图像Url
         */
        @Alias("image_url")
        private String imageUrl;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 可选，逆时针旋转角度，默认为0，取值范围为0-359
         */
        private Integer angle;

        /**
         * 可选：输出图像的宽高比，字符串，
         * 取值范围为1:1-2:1-4:1-1:2-1:4-1:8-1:16，
         */
        @Alias("output_ratio")
        private String outputRatio;


        /**
         * 可选：图像居中，在水平方向上按照比例扩展，默认值1.0
         * 取值范围为1.0-3.0
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * 可选：图像居中，在垂直方向上按照比例扩展，默认值1.0
         * 取值范围为1.0-3.0
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 可选：在图像上方添加像素，默认值为0
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 可选：在图像下方添加像素，默认值为0
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 可选：在图像右侧添加像素，默认值为0
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 可选：在图像左侧添加像素，默认值为0
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 可选：是否使用高质量，默认值为false
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 可选：是否限制图像生成的大小
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 可选：是否添加水印，默认值为false
         */
        @Alias("add_watermark")
        private Boolean addWatermark;
    }
}
