package com.example.picture.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.picture.api.imagesearch.model.ImageSearchResult;
import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     *
     * @param url
     * @return
     */
    public static List<ImageSearchResult> getImageList(String url) {
       try {
           //发起get请求
           HttpResponse httpResponse = HttpUtil.createGet(url).execute();

           //获取响应内容
           int status = httpResponse.getStatus();
           String body = httpResponse.body();

           //处理响应
           if(status == 200) {
                return processResponse(body);
           } else  {
               throw new BussinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
           }
       } catch (Exception e) {
           log.error("调用接口失败", e);
           throw new BussinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
       }
    }

    /**
     * 处理响应
     *
     * @param body
     * @return
     */
    private static List<ImageSearchResult> processResponse(String body) {
        //解析响应对象
        JSONObject jsonObject = new JSONObject(body);
        if(!jsonObject.containsKey("data")) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if(!data.containsKey("list")) {
            throw new BussinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        JSONArray jsonArray = data.getJSONArray("list");
        return JSONUtil.toList(jsonArray, ImageSearchResult.class);
    }

    public static void main(String[] args) {
        //测试以图搜图功能
        String imageUrl = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=12338411988395352555&sign=126eb8815871879e23e7e01735201395&tk=c82c1&tpl_from=pc";
        System.out.println("搜索成功" + getImageList(imageUrl));
    }

}
