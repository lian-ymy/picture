package com.example.picture.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.picture.annotation.AuthCheck;
import com.example.picture.common.BaseResponse;
import com.example.picture.common.DeleteRequest;
import com.example.picture.common.ResultUtils;
import com.example.picture.constant.UserConstant;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import com.example.picture.model.Space;
import com.example.picture.model.SpaceLevel;
import com.example.picture.model.User;
import com.example.picture.model.dto.space.SpaceAddRequest;
import com.example.picture.model.dto.space.SpaceEditRequest;
import com.example.picture.model.dto.space.SpaceQueryRequest;
import com.example.picture.model.dto.space.SpaceUpdateRequest;
import com.example.picture.model.enums.SpaceLevelEnum;
import com.example.picture.model.vo.space.SpaceVO;
import com.example.picture.service.SpaceService;
import com.example.picture.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@RequestMapping("/space")
@RestController
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    /**
     * 更新空间
     *
     * @param spaceUpdateRequest
     * @return
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long spaceId = spaceUpdateRequest.getId();
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BussinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        Space updateSpace = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, updateSpace);
        boolean update = spaceService.updateById(updateSpace);
        ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevels = Arrays.stream(SpaceLevelEnum.values()).map(spaceLevelEnum -> new SpaceLevel(
                spaceLevelEnum.getValue(),
                spaceLevelEnum.getText(),
                spaceLevelEnum.getMaxCount(),
                spaceLevelEnum.getMaxSize()
        )).collect(Collectors.toList());
        return ResultUtils.success(spaceLevels);
    }

    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.NULL_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest==null, ErrorCode.NULL_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceQueryRequest==null, ErrorCode.NULL_ERROR);
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.NULL_ERROR);
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        User loginUser = userService.getLoginUser(request);
        spaceService.deleteSpace(space, loginUser);
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        return ResultUtils.success(spaceVO);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long spaceId = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        if(!userService.isAdmin(loginUser) || !oldSpace.getUserId().equals(loginUser.getId())){
            throw new BussinessException(ErrorCode.NO_AUTHOR, "没有权限");
        }
        Space newSpace = new Space();
        newSpace.setEditTime(new Date());
        spaceService.validSpace(newSpace, false);
        BeanUtil.copyProperties(spaceEditRequest, newSpace);
        boolean update = spaceService.updateById(newSpace);
        ThrowUtils.throwIf(update, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
