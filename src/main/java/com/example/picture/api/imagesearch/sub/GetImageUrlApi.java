package com.example.picture.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
public class GetImageUrlApi {

    /**
     * 获取图片详情页地址
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        //1、获取准备请求参数
        Map<String,Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        //获取当前时间戳
        long currentTimeMillis = System.currentTimeMillis();
        //请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + currentTimeMillis;

        try {
            //2、发送当前Post请求到百度接口
            HttpResponse httpResponse = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            //判断响应状态
            if(HttpStatus.HTTP_OK != httpResponse.getStatus()) {
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "调用图片接口失败");
            }
            //解析状态
            String body = httpResponse.body();
            Map<String, Object> result = JSONUtil.toBean(body, Map.class);

            //3、处理响应结果
            if(result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "调用图片接口失败");
            }
            Map<String,Object> images = (Map<String, Object>) result.get("data");
            String rawUrl = images.get("url").toString();
            //对获取的原生Url进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            //如果Url为空
            if(searchResultUrl == null) {
                throw new BussinessException(ErrorCode.OPERATION_ERROR, "调用图片接口失败");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败",e);
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "调用图片接口失败");
        }
    }


    public static void main(String[] args) {
        //测试以图搜图功能
        String imageUrl = "https://tse3-mm.cn.bing.net/th/id/OIP-C.vCzoD7L8ZQFTCK1YufWxmQHaEH";
        System.out.println("搜索成功" + getImagePageUrl(imageUrl));
    }
}
