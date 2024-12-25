package com.example.picture.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
@Deprecated
@Service
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
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
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
     * 上传图片
     * @param url
     * @param uploadFilePrefix
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String url, String uploadFilePrefix) {
        //图片上传地址
        validPicture(url);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(url);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadFilePrefix, uploadFilename);
        //上传图片
        File file = null;
        try {
            //创建临时文件
            file = File.createTempFile(uploadPath, null);
            //将图片写入临时文件中
            HttpUtil.downloadFile(url, file);
            //上传图片
            PutObjectResult putObjectResult = cosManager.putPictureRequest(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
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
        } catch (IOException e) {
            log.error("图片上传失败");
            throw new BussinessException(ErrorCode.SYSTEM_ERROR, "系统执行错误");
        } finally {
            //删除临时文件，防止内存泄漏
            deleteTempFile(file);
        }
    }

    /**
     * 校验图片
     */
    private void validPicture(String url) {
       ThrowUtils.throwIf(url==null, ErrorCode.PARAMS_ERROR, "文件不能为空");

       //1、验证文件格式是否正确
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "文件地址错误");
        }

        //2、校验URL协议
        ThrowUtils.throwIf(!url.startsWith("http")||!url.startsWith("https"), ErrorCode.PARAMS_ERROR, "文件地址错误");

        //3、发送HEAD请求来验证文件是否存在
        HttpResponse response = null;
        try {
            //使用hutool的http请求工具类
            response = (HttpResponse) HttpUtil.createRequest(Method.HEAD, url).execute();
            //如果响应码不正常，则认为文件不存在
            if(response.statusCode() != 200) {
                throw new BussinessException(ErrorCode.PARAMS_ERROR, "文件地址错误");
            }

            //4、校验文件类型
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if(StrUtil.isNotBlank(contentType)) {
                String fileType = contentType.split("/")[1];
                //允许上传的文件类型
                final List<String> ALLOW_FILE_TYPE = List.of("image/jpg","image/jpeg","image/png","image/gif", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FILE_TYPE.contains(fileType), ErrorCode.PARAMS_ERROR, "文件类型错误");
            }

            //5、校验文件大小
            String contentLength = response.headers().firstValue("Content-Length").orElse("");
            if(StrUtil.isNotBlank(contentLength)) {
                try {
                    long fileSize = Long.parseLong(contentLength);
                    final long maxSize = 1024L * 1024;
                    //限制大小2M
                    ThrowUtils.throwIf(fileSize > 2 * maxSize, ErrorCode.PARAMS_ERROR, "文件大小不能超过2M");
                } catch (NumberFormatException e) {
                    throw new BussinessException(ErrorCode.PARAMS_ERROR, "文件格式错误");
                }
            }
        } catch (Exception e) {
            throw new BussinessException(ErrorCode.PARAMS_ERROR, "文件地址错误");
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
