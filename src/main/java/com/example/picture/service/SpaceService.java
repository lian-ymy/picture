package com.example.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.picture.model.Space;
import com.example.picture.model.User;
import com.example.picture.model.dto.space.SpaceAddRequest;
import com.example.picture.model.dto.space.SpaceQueryRequest;
import com.example.picture.model.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author lian
* @description 针对表【space(空间表)】的数据库操作Service
* @createDate 2024-12-25 16:00:27
*/
public interface SpaceService extends IService<Space> {
    /**
     * 校验空间
     * @param space
     * @param add
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间等级填充空间信息
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 创建空间
     * @param spaceAddRequest
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 分页获取空间列表
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 删除空间
     * @param space
     * @param loginUser
     */
    void deleteSpace(Space space, User loginUser);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);
}
