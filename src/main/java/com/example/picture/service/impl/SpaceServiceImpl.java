package com.example.picture.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import com.example.picture.mapper.SpaceMapper;
import com.example.picture.model.Picture;
import com.example.picture.model.Space;
import com.example.picture.model.User;
import com.example.picture.model.dto.space.SpaceAddRequest;
import com.example.picture.model.dto.space.SpaceQueryRequest;
import com.example.picture.model.enums.SpaceLevelEnum;
import com.example.picture.model.vo.space.SpaceVO;
import com.example.picture.service.PictureService;
import com.example.picture.service.SpaceService;
import com.example.picture.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lian
 * @description 针对表【space(空间表)】的数据库操作Service实现
 * @createDate 2024-12-25 16:00:27
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private final UserService userService;

    @Resource
    private final PictureService pictureService;

    @Autowired
    public SpaceServiceImpl(UserService userService, PictureService pictureService) {
        this.userService = userService;
        this.pictureService = pictureService;
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelByValue = SpaceLevelEnum.getSpaceLevelByValue(spaceLevel);
        if (add) {
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            ThrowUtils.throwIf(spaceName == null, ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        }
        if (spaceLevel != null && spaceLevelByValue == null) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (spaceName != null && spaceName.length() > 20) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        Integer spaceLevel = space.getSpaceLevel();
        if (spaceLevel == null) {
            return;
        }
        SpaceLevelEnum spaceLevelByValue = SpaceLevelEnum.getSpaceLevelByValue(spaceLevel);
        if (spaceLevelByValue != null) {
            if (space.getMaxCount() == null) {
                space.setMaxCount(spaceLevelByValue.getMaxCount());
            }
            if (space.getMaxSize() == null) {
                space.setMaxSize(spaceLevelByValue.getMaxSize());
            }
        }
    }

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //将实体类与DTO对象进行转化
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        //默认值
        if(StrUtil.isEmpty(space.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(space.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.NORMAL.getValue());
        }
        //填充最大容量等相关数据
        this.fillSpaceBySpaceLevel(space);
        //校验数据
        this.validSpace(space, true);
        Long loginUserId = loginUser.getId();
        space.setUserId(loginUserId);
        //权限校验，普通用户只能创建普通空间
        if(space.getSpaceLevel() != SpaceLevelEnum.NORMAL.getValue() && !userService.isAdmin(loginUser)){
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "普通用户只能创建普通空间");
        }
        //针对用户进行加锁，通过使用intern确保每次针对同一个用户进行加锁
        String lock = String.valueOf(loginUserId).intern();
        synchronized (lock) {
            Long spaceId = transactionTemplate.execute(status -> {
                boolean exists = this.lambdaQuery().eq(Space::getId, loginUserId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "每个用户只能够有一个私人空间");
                //写入数据库
                boolean save = this.save(space);
                ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "创建空间失败");
                return space.getId();
            });
            //返回结果是包装类，做一些处理
            return Optional.ofNullable(spaceId).orElse(-1L);
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return spaceQueryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();

        spaceQueryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        spaceQueryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        spaceQueryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        spaceQueryWrapper.eq(ObjUtil.isNotNull(spaceLevel), "spaceLevel", spaceLevel);
        return spaceQueryWrapper;
    }

    @Override
    public void deleteSpace(Space space, User loginUser) {
        //删除空间时要删除用户创建的空间以及空间下面所有图片
        //检查用户是否有权限进行删除
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BussinessException(ErrorCode.NO_AUTHOR, "无权限进行删除！");
        }
        //查出空间下的所有图片
        Long spaceId = space.getId();
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.SYSTEM_ERROR, "要删除的空间不存在");
        Space oldSpace = this.getById(spaceId);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.SYSTEM_ERROR, "要删除的空间不存在");
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.eq("spaceId", spaceId);
        List<Picture> deletePictures = pictureService.list(pictureQueryWrapper);
        transactionTemplate.execute(status -> {
            //先删除空间下面的所有图片
            deletePictures.forEach(picture -> pictureService.deletePicture(picture, loginUser));
            //再删除空间
            boolean remove = this.removeById(spaceId);
            ThrowUtils.throwIf(!remove, ErrorCode.SYSTEM_ERROR, "删除空间失败");
            return true;
        });
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        ThrowUtils.throwIf(spacePage == null, ErrorCode.PARAMS_ERROR);
        List<Space> spaceList = spacePage.getRecords();
        User loginUser = userService.getLoginUser(request);
        for (Space space : spaceList) {
            ThrowUtils.throwIf(!space.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTHOR);
        }
        //关联查询的用户视图信息
        List<SpaceVO> spaceVOS = spaceList.stream().map(space -> SpaceVO.objToVo(space)).collect(Collectors.toList());
        List<Long> userIdList = spaceList.stream().map(Space::getUserId).collect(Collectors.toList());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdList).stream().collect(Collectors.groupingBy(User::getId));
        //填充用户信息
        spaceVOS.forEach(spaceVO -> {
            Long id = spaceVO.getId();
            User user = null;
            if (userIdList.contains(id)) {
                List<User> users = userIdUserListMap.get(id);
                if (users != null && users.size() > 0) {
                    user = users.get(0);
                }
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        spaceVOPage.setRecords(spaceVOS);
        return spaceVOPage;
    }
}




