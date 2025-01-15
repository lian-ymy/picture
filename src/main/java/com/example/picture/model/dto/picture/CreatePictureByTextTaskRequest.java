package com.example.picture.model.dto.picture;

import com.example.picture.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.example.picture.api.aliyunai.model.PictureByTextTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class CreatePictureByTextTaskRequest implements Serializable {

    /**
     * 图片参数
     */
    private PictureByTextTaskRequest.Parameters parameters;

    /**
     * 输入
     */
    private PictureByTextTaskRequest.Input input;
}
