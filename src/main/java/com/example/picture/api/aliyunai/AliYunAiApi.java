package com.example.picture.api.aliyunai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.example.picture.api.aliyunai.model.*;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Component
@Slf4j
public class AliYunAiApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    @Value("${aliYunAi.agentId}")
    private String agentId;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    //文字作画任务地址
    public static final String CREATE_PICTURE_TEXT_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis";

    //文字作画查询任务地址
    public static final String GET_TEXT_PICTURE_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%S";

    /**
     * 创建任务
     *
     * @param createOutPaintingTaskRequest
     * @return
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        if (createOutPaintingTaskRequest == null) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                // 必须开启异步处理
                .header("X-DashScope-Async", "enable")
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        // 处理响应
        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            CreateOutPaintingTaskResponse createOutPaintingTaskResponse = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            if (createOutPaintingTaskResponse.getCode() != null) {
                String errorMessage = createOutPaintingTaskResponse.getMessage();
                log.error("请求异常：{}", errorMessage);
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败，" + errorMessage);
            }
            return createOutPaintingTaskResponse;
        }
    }

    /**
     * 创建任务
     *
     * @param createPictureByTextTaskRequest
     * @return
     */
    public PictureByTextTaskResponse createPictureByTextTask(PictureByTextTaskRequest createPictureByTextTaskRequest) {
        if (createPictureByTextTaskRequest == null) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        String prompt = createPictureByTextTaskRequest.getInput().getPrompt();
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                        .model(ImageSynthesis.Models.WANX_V1)
                        .prompt(prompt)
                        .style("<watercolor>")
                        .n(1)
                        .size("1024*1024")
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            throw new RuntimeException(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(result));
        PictureByTextTaskResponse pictureByTextTaskResponse = BeanUtil.copyProperties(result, PictureByTextTaskResponse.class);
        return pictureByTextTaskResponse;
    }

    /**
     * 查询任务状态
     *
     * @param taskId
     * @return
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "任务id不能为空");
        }
        // 处理响应
        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(url)
                .header("Authorization", "Bearer " + apiKey)
                .execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取任务结果失败");
            }
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId
     * @return
     */
    public GetPictureByTextTaskResponse getPictureByTextTask (String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "任务id不能为空");
        }
        GetPictureByTextTaskResponse getPictureByTextTaskResponse = null;
        try {
            ImageSynthesis is = new ImageSynthesis();
            // If set DASHSCOPE_API_KEY environment variable, apiKey can null.
            ImageSynthesisResult result = is.fetch(taskId, null);
            getPictureByTextTaskResponse = BeanUtil.copyProperties(result, GetPictureByTextTaskResponse.class);
            System.out.println(result.getOutput());
            System.out.println(result.getUsage());
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return getPictureByTextTaskResponse;
    }
}
