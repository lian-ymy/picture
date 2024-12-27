package com.example.picture.model.vo.picture;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.picture.model.Picture;
import com.example.picture.model.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class PictureVO implements Serializable {
    /**
     * id  唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 关联的用户id
     */
    private Long userId;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片描述
     */
    private String introduction;

    /**
     * 图片分类
     */
    private Integer category;

    /**
     * 图片标签
     */
    private List<String> tags;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String pirFormat;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 图片颜色
     */
    private String picColor;

    /**
     * 缩略图地址
     */
    private String thumbnailUrl;

    public static Picture voToEntity(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureVO, picture);
        //类型不同需要转换
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

    public static PictureVO entityToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        //类型不同需要转换
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
