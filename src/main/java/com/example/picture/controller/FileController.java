package com.example.picture.controller;


import com.example.picture.annotation.AuthCheck;
import com.example.picture.common.BaseResponse;
import com.example.picture.common.ResultUtils;
import com.example.picture.constant.UserConstant;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import com.example.picture.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@RestController
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    /**
     * 上传文件
     *
     * @param multipartFile
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        //文件目录
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("test/%s", fileName);
        File file = null;
        try {
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            cosManager.putObjectRequest(filePath, file);
            //返回可访问地址
            return ResultUtils.success(filePath);
        } catch (Exception exception) {
            log.error("文件上传失败", exception);
            throw new BussinessException(ErrorCode.UPLOAD_ERROR);
        } finally {
            if (file != null) {
                //删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("删除临时文件失败", fileName);
                }
            }
        }
    }

    /**
     * 下载文件
     *
     * @param filePath
     * @param response
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filePath);
            COSObjectInputStream objectContentInput = cosObject.getObjectContent();
            //处理下载得到的流
            byte[] bytes = IOUtils.toByteArray(objectContentInput);
            //设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filePath);
            //写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception exception) {
            log.error("文件下载失败", exception);
            throw new BussinessException(ErrorCode.DOWNLOAD_ERROR);
        } finally {
            if(cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }

}
