package com.example.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import com.example.picture.manager.CosManager;
import com.example.picture.manager.upload.FilePictureUpload;
import com.example.picture.manager.upload.PictureUploadTemplate;
import com.example.picture.manager.upload.UrlPictureUpload;
import com.example.picture.mapper.PictureMapper;
import com.example.picture.model.Picture;
import com.example.picture.model.Space;
import com.example.picture.model.User;
import com.example.picture.model.dto.file.UploadPictureResult;
import com.example.picture.model.dto.picture.*;
import com.example.picture.model.enums.PictureReviewStatusEnum;
import com.example.picture.model.vo.picture.PictureVO;
import com.example.picture.model.vo.user.UserVO;
import com.example.picture.service.PictureService;
import com.example.picture.service.SpaceService;
import com.example.picture.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lian
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-25 11:40:21
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private final FilePictureUpload filePictureUpload;

    @Resource
    private final UrlPictureUpload urlPictureUpload;

    private PictureUploadTemplate pictureUploadTemplate;

    @Resource
    private final UserService userService;

    @Resource
    @Lazy
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CosManager cosManager;

    public PictureServiceImpl(FilePictureUpload filePictureUpload, UrlPictureUpload urlPictureUpload, UserService userService) {
        this.filePictureUpload = filePictureUpload;
        this.urlPictureUpload = urlPictureUpload;
        this.userService = userService;
    }

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTHOR, "用户未登录");
        //判断当前用户是否有空间权限
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!space.getUserId().equals(loginUser.getId())) {
                throw new BussinessException(ErrorCode.NO_AUTHOR, "只有创建者才能够上传图片");
            }
            //检验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "空间额度不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
//判断是新增图片还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getPictureId();
        }
//如果是更新图片，检验图片id是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            if (!Objects.equals(loginUser.getId(), oldPicture.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限");
            }
            //如果是更新操作，需要判断对应的空间id是否一致，只有空间id对应一致时才能够进行更新
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                }
            } else {
                if (!Objects.equals(spaceId, oldPicture.getSpaceId())) {
                    throw new BussinessException(ErrorCode.NO_AUTHOR, "空间id不一致");
                }
            }
        }
//上传图片，得到信息
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        if (spaceId == null) {
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        } else {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
//使用模板方法模式进行改造
        if (inputSource instanceof String) {
//如果是url
            pictureUploadTemplate = urlPictureUpload;
        } else {
            pictureUploadTemplate = filePictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
//构造要入库的信息到数据库中
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUploadRequest, picture);

        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setSpaceId(spaceId);
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());

        picture.setUserId(loginUser.getId());
        picture.setUrl(pictureUploadRequest.getFileUrl());
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
//如果图片id不为空，表示更新，否则为新增
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
//设置图片审核信息
        fillReviewParams(picture, loginUser);
        //遇到多个数据库操作更新，开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            boolean update = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "上传图片失败");
            if(finalSpaceId != null) {
                boolean spaceUpdate = spaceService.lambdaUpdate().eq(Space::getId, finalSpaceId)
                        .setSql("totalCount = totalCount + 1")
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!spaceUpdate, ErrorCode.SYSTEM_ERROR, "更新空间信息失败");
            }
            return picture;
        });
        return PictureVO.entityToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String pirFormat = pictureQueryRequest.getPirFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        Long reviewUserId = pictureQueryRequest.getReviewUserId();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long spaceId = pictureQueryRequest.getSpaceId();
//从多字段检索
        if (StrUtil.isNotBlank(searchText)) {
//拼接查询条件
            queryWrapper.and(qe -> qe.like("name", searchText)
                    .or().like("introduction", searchText));
        }
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(pirFormat), "pirFormat", pirFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotNull(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotNull(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotNull(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotNull(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotNull(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotNull(reviewUserId), "reviewUserId", reviewUserId);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotNull(spaceId), "spaceId", spaceId);
//JSON数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
//排序
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
//将对象转换为封装类
        PictureVO pictureVO = PictureVO.entityToVo(picture);
//关联用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUserVO(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
//对象列表->封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::entityToVo).collect(Collectors.toList());
//1、关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
//2、填充用户信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUserVO(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片为空");
//从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
//修改数据时，id不能为空，有参数则进行校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片id为空");
        if (StrUtil.isNotBlank(url)) {
//校验url是否合法
            ThrowUtils.throwIf(!url.matches("^http(s)?://.*"), ErrorCode.PARAMS_ERROR, "图片url不合法");
        }
        if (StrUtil.isNotBlank(introduction)) {
//校验图片简介是否超过1000字
            ThrowUtils.throwIf(introduction.length() > 1000, ErrorCode.PARAMS_ERROR, "图片简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || reviewStatusEnum == PictureReviewStatusEnum.REVIEWING || reviewStatusEnum == PictureReviewStatusEnum.PASS) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
//判断是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
//判断是否为管理员
        if (!userService.isAdmin(loginUser)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限");
        }
//更新图片审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewTime(new Date());
        updatePicture.setReviewUserId(loginUser.getId());
        boolean update = this.updateById(updatePicture);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "审核失败");
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
//分为管理员与非管理员两种情况进行考虑
        if (userService.isAdmin(loginUser)) {
            picture.setReviewMessage("审核通过");
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewUserId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
//如果没有指定名称前缀，则使用搜索词
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
//格式化数量
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count == null || count <= 0, ErrorCode.PARAMS_ERROR, "数量错误");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "数量不能超过30条");
//要抓取的图片地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("抓取图片失败");
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "抓取图片失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取图片元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                continue;
            }
//处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
//上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setPicName(namePrefix + "_" + (uploadCount + 1));
            pictureUploadRequest.setFileUrl(fileUrl);
            try {
                this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                uploadCount++;
            } catch (Exception e) {
                log.error("上传图片失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    /**
     * 异步删除图片文件
     *
     * @param deletePicture
     */
    @Async
    @Override
    public void clearPictureFile(Picture deletePicture) {
        //判断该图片是否被多条记录正在使用
        String pictureUrl = deletePicture.getUrl();
        Long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
        if (count == null || count > 1) {
            return;
        }
        //删除图片文件
        cosManager.deleteObject(pictureUrl);
        //清理缩略图
        String thumbnailUrl = deletePicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            //公共图库，只有本人或者管理员可以进行操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限");
            }
        } else {
            //私有图库，只有空间本人可以进行操作
            if (!loginUser.getId().equals(picture.getUserId())) {
                throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限");
            }
        }
    }

    @Override
    public void deletePicture(Picture picture, User loginUser) {
        ThrowUtils.throwIf(picture == null || picture.getId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.PARAMS_ERROR);
        //判断是否存在
        Picture oldPicture = this.getById(picture.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //是否有删除的权限
        this.checkPictureAuth(loginUser, picture);
        //删除图片
       transactionTemplate.execute(status -> {
           boolean delete = this.removeById(picture);
           ThrowUtils.throwIf(!delete, ErrorCode.SYSTEM_ERROR, "删除失败");
           //释放空间对应额度
           Long spaceId = picture.getSpaceId();
           if(spaceId != null) {
               boolean spaceUpdate = spaceService.lambdaUpdate().eq(Space::getId, spaceId)
                       .setSql("totalCount = totalCount - 1")
                       .setSql("totalSize = totalSize - " + picture.getPicSize())
                       .update();
               ThrowUtils.throwIf(!spaceUpdate, ErrorCode.SYSTEM_ERROR, "更新空间信息失败");
           }
           return true;
       });
        //异步删除图片文件
        this.clearPictureFile(oldPicture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        //将实体类对象与dto对象进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        //校验图片是否满足要求
        this.validPicture(picture);
        //判断是否存在
        Picture oldPicture = this.getById(picture.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //是否有编辑的权限
        this.checkPictureAuth(loginUser, oldPicture);
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
        //更新图片
        boolean update = this.updateById(picture);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "更新失败");
    }

}




