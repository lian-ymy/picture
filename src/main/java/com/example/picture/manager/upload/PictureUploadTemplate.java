package com.example.picture.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.picture.config.CosClientConfig;
import com.example.picture.manager.CosManager;
import com.example.picture.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 使用模板方法模式优化图片上传部分的代码框架
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 校验图片(模板方法)
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public final UploadPictureResult uploadPicture(Object inputSource,  String uploadPathPrefix) {
        //1、校验图片
        validPicture(inputSource);
        //2、获取图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, originalFilename);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File file = null;
        try {
            //3、创建临时文件
            file = File.createTempFile(uploadPath, null);
            //处理文件来源
            processFile(inputSource, file);

            //4、上传文件到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureRequest(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if(CollUtil.isNotEmpty(objectList)) {
                //获取压缩图对象
                CIObject compressedCiObject = objectList.get(0);
                //获取缩略图对象
                CIObject thumbnailCiObject = objectList.get(1);
                //封装压缩图返回结果
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject);
            }
            //5、封装原图返回结果
            return buildResult(originalFilename, file, uploadPath, imageInfo)   ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 处理文件来源，是否是文件，是否是url
     * @param inputSource
     * @param file
     * @throws IOException
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 校验图片(钩子方法)
     * @param inputSource
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取原始文件名(钩子方法)
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(Object inputSource);

    protected UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();

        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth*1.0/picHeight, 2).doubleValue();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPirFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    protected UploadPictureResult buildResult(String originalFilename, CIObject compressCiObject, CIObject thumbnailCiObject) {
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();

        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth*1.0/picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPirFormat(compressCiObject.getFormat());
        //将图片地址设置为压缩过后的对应地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
        //将缩略图地址设置为缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }


    /**
     * 删除临时文件
     * @param file
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }

        //删除临时文件，防止内存泄漏
        boolean delete = file.delete();
        if (!delete) {
            log.error("删除临时文件失败");
        }
    }
}
