package com.example.picture.api.imagesearch.sub;

import com.example.picture.api.imagesearch.model.ImageSearchResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Slf4j
public class ImageSearchApiFacade {
    /**
     * 图片搜索
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageListUrl = GetImageListApi.getImageList(imageFirstUrl);
        return imageListUrl;
    }

    public static void main(String[] args) {
        //测试以图搜图功能
        String imageUrl = "https://tse2-mm.cn.bing.net/th/id/OIP-C.8yauiYxBookkb04BJwuBsAHaLq";
        System.out.println("搜索成功" + searchImage(imageUrl));
    }
}
