package com.example.picture.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureByTextTaskRequest implements Serializable {

    /**
     * 模型
     */
    private String model = "wanx-v1";

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
         * 输入描述图片的文本语句
         */
        private String prompt;

        /**
         * 可选，反向提示词，可说出不想看到什么
         */
        @Alias("negative_prompt")
        @JsonProperty("negative_prompt")
        private String nagativePrompt;

        /**
         * 可选，参考图片，base64编码，支持jpg、png格式
         */
        @Alias("ref_img")
        @JsonProperty("ref_img")
        private String refImg;
    }

    @Data
    public static class Parameters implements Serializable {

        /**
         * 可选，图片对应的风格
         */
        private String style;

        /**
         * 可选，图片的分辨率
         */
        private String size;

        /**
         * 可选，生成图片的数量
         */
        private Integer n;

        /**
         * 可选，随机种子
         */
        private Integer seed;

        /**
         * 可选，图片与指定图片的相似度，取值范围[0,1]，数组越大，图片越接近参考图片
         */
        @JsonProperty("ref_strength")
        private Float refStrength;

        /**
         * 可选，生成图片的模式，目前支持基于参考图内容生成图像或者基于图的风格生成图像
         */
        @Alias("ref_mode")
        @JsonProperty("ref_mode")
        private String refMode;
    }
}
