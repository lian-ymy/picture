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
import com.example.picture.model.Space;
import com.example.picture.model.User;
import com.example.picture.model.dto.picture.*;
import com.example.picture.model.enums.PictureReviewStatusEnum;
import com.example.picture.model.vo.picture.PictureVO;
import com.example.picture.service.PictureService;
import com.example.picture.service.SpaceService;
import com.example.picture.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private SpaceService spaceService;

    //构造本地缓存，设置缓存容量和过期时间
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    //缓存6分钟移除
                    .expireAfterWrite(6, TimeUnit.MINUTES)
                    .build();

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
        pictureService.deletePicture(deletePicture, loginUser);
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
    public BaseResponse<PictureVO> getPictureVOById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
        //空间权限校验
        Long spaceId = picture.getSpaceId();
        if(spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
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
    public BaseResponse<Page<Picture>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Long spaceId = pictureQueryRequest.getSpaceId();
        if(spaceId == null) {
            //普通用户只能查看自己已经通过审核的图片数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            //私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间不存在");
            if(!space.getUserId().equals(loginUser.getId())) {
                throw new BussinessException(ErrorCode.NO_AUTHOR, "没有空间权限");
            }
        }
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表，构建多级缓存策略
     * @param pictureQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //普通用户只能够查看自己已经通过审核的图片数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        //构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String redisKey = "lianpicture:listPictureVOByPageCache:" + hashKey;

        //1、使用本地缓存
        String cachedValue = LOCAL_CACHE.getIfPresent(hashKey);
        if(cachedValue != null) {
            //如果缓存命中，直接返回结果
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }

        //2、使用分布式缓存
        ValueOperations valueOperations = redisTemplate.opsForValue();
        cachedValue = (String) valueOperations.get(redisKey);
        if(cachedValue != null) {
            //如果分布式缓存命中，存入本地缓存中
            LOCAL_CACHE.put(hashKey, cachedValue);
            //返回结果
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }

        // 3、查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);

        //将查询结果存入缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        //1)存入本地缓存中
        LOCAL_CACHE.put(hashKey, cacheValue);
        //2)存入分布式缓存中
        valueOperations.set(redisKey, cacheValue);

        //返回结果
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if(pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        pictureService.editPicture(pictureEditRequest, userService.getLoginUser(request));
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

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        User loginUser = userService.getLoginUser(request);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(count);
    }
}
