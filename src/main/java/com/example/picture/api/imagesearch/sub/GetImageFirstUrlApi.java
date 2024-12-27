package com.example.picture.api.imagesearch.sub;

import com.example.picture.exception.BussinessException;
import com.example.picture.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
public class GetImageFirstUrlApi {

    /**
     * 获取图片详情页地址
     * @param url
     * @return
     */
    public static String getImageFirstUrl(String url) {
       try {
           //使用Jsoup获取网页的html内容
           Document document = Jsoup.connect(url).timeout(5000).get();
           //获取所有对应的scrips标签对应内容
           Elements scriptElements = document.getElementsByTag("script");

           //遍历找到包含firstUrl的脚本内容
           for (Element element : scriptElements) {
               String scriptContent = element.html();
               if (scriptContent.contains("\"firstUrl\"")) {
                   //通过正则表达式提取firstUrl的值
                   Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                   Matcher matcher = pattern.matcher(scriptContent);
                   if(matcher.find()) {
                       String firstUrl = matcher.group(1);
                       //处理转义字符
                       firstUrl = firstUrl.replace("\\/", "/");
                       return firstUrl;
                   }
               }
           }
           throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取图片详情页地址失败");
       }catch (Exception e) {
           log.error("获取图片详情页地址失败", e);
           throw new BussinessException(ErrorCode.OPERATION_ERROR, "获取图片详情页地址失败");
       }
    }

    public static void main(String[] args) {
        //测试获取图片详细url功能
        String imageUrl = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=12338411988395352555&sign=126eb8815871879e23e7e01735201395&tpl_from=pc";
        System.out.println("搜索成功" + getImageFirstUrl(imageUrl));
    }
}
