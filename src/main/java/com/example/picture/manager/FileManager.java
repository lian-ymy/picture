package com.example.picture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.picture.config.CosClientConfig;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import com.example.picture.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     * @param multipartFile
     * @param uploadFilePrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadFilePrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, originalFilename);
        String uploadPath = String.format("/%s/%s", uploadFilePrefix, uploadFilename);
        File file = null;
        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
            //将图片写入临时文件中
            multipartFile.transferTo(file);
            //上传图片
            PutObjectResult putObjectResult = cosManager.putPictureRequest(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();

            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth*1.0/picHeight, 2).doubleValue();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPirFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception exception) {
            log.error("上传图片失败", exception);
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "上传图片失败");
        } finally {
            //删除临时文件，防止内存泄漏
            deleteTempFile(file);
        }
    }

    /**
     * 校验图片
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile==null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        //1、校验文件大小
        long size = multipartFile.getSize();
        final long maxSize = 1024L*1024;
        ThrowUtils.throwIf(size>2*maxSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
        //2、校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //允许上传的文件后缀
        final List<String> ALLOW_FILE_SUFFIX = List.of("jpg","jpeg","png","gif", "webp");
        ThrowUtils.throwIf(!ALLOW_FILE_SUFFIX.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     * @param file
     */
    public void deleteTempFile(File file) {
        if(file == null) {
            return;
        }

        //删除临时文件，防止内存泄漏
        boolean delete = file.delete();
        if(!delete) {
            log.error("删除临时文件失败");
        }
    }
}
