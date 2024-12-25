package com.example.picture.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.example.picture.exception.ErrorCode;
import com.example.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate{
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        //将图片写入临时文件中
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
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

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }
}
