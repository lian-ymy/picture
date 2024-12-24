package com.example.picture.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.picture.annotation.AuthCheck;
import com.example.picture.common.BaseResponse;
import com.example.picture.common.DeleteRequest;
import com.example.picture.common.ResultUtils;
import com.example.picture.constant.UserConstant;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import com.example.picture.model.Picture;
import com.example.picture.model.PictureTagCategory;
import com.example.picture.model.User;
import com.example.picture.model.dto.picture.PictureQueryRequest;
import com.example.picture.model.dto.picture.PictureReviewRequest;
import com.example.picture.model.dto.picture.PictureUpdateRequest;
import com.example.picture.model.dto.picture.PictureUploadRequest;
import com.example.picture.model.enums.PictureReviewStatusEnum;
import com.example.picture.model.vo.picture.PictureVO;
import com.example.picture.service.PictureService;
import com.example.picture.service.UserService;
import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断用户是否存在
        //判断是否存在
        Picture deletePicture = pictureService.getById(id);
        ThrowUtils.throwIf(deletePicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        //仅本人或者管理员才能删除
        if(loginUser.getId() != deletePicture.getUserId() && !userService.isAdmin(loginUser)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限删除");
        }
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片
     * @param pictureUpdateRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        //判断是否存在，只有存在才能进行更新
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        //将实体类与DTO进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        //这里由于tags在不同类中的不同属性表示，因此要进行互相的转化
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //校验图片是否合法
        pictureService.validPicture(picture);
        //更新审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        //更新图片
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取图片
     * @param id
     * @return
     */
    @GetMapping(value = "/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> getPictureVOById(Long id) {
        if (id == null || id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        return ResultUtils.success(PictureVO.entityToVo(picture));
    }

    /**
     * 分页获取图片列表
     * @param pictureQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表
     * @param pictureQueryRequest
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<Picture>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 编辑图片
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);
        long id = pictureUpdateRequest.getId();
        Picture editPicture = pictureService.getById(id);
        ThrowUtils.throwIf(editPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        User loginUser = userService.getLoginUser(request);
        //判断是否本人或者管理员
        if(loginUser.getId() != editPicture.getUserId() && !userService.isAdmin(loginUser)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限编辑");
        }
        //更新审核参数
        pictureService.fillReviewParams(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "编辑失败");
        return ResultUtils.success(true);
    }

    /**
     * 获取标签分类列表信息
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "校园", "二次元", "红色", "生活", "自然", "运动", "美术", "创意", "中国", "世界", "科技", "想象");
        List<String> categoryList = Arrays.asList("模板", "表情包", "素材", "海报", "背景图", "推文", "配图");
        pictureTagCategory.setCategoryList(categoryList);
        pictureTagCategory.setTagList(tagList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPicture(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        pictureService.doPictureReview(pictureReviewRequest, userService.getLoginUser(request));
        return ResultUtils.success(true);
    }
}