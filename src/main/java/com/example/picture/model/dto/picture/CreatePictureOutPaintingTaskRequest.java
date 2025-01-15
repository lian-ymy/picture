package com.example.picture.model.dto.picture;

import com.example.picture.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片id
     */
    private Long pictureId;

    /**
     * 图片参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;
}
