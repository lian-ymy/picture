package com.example.picture.model.vo.space;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.picture.model.Space;
import com.example.picture.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Data
public class SpaceVO implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级：0-普通空间 1-专业版 2-企业版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-个人空间 1-团队空间
     */
    private Integer spaceType;

    /**
     * 最大容量
     */
    private Long maxSize;

    /**
     * 已使用容量
     */
    private Long totalSize;

    /**
     * 最大存储数量
     */
    private Long maxCount;

    /**
     * 已使用数量
     */
    private Long totalCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 关联的用户信息
     */
    private UserVO user;

    /**
     * 实体类转化为视图
     *
     * @param space
     * @return
     */
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }

    /**
     * 视图转化为实体类
     *
     * @param spaceVO
     * @return
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }
}
