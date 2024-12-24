package com.example.picture.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="https://github.com/lian-ymy">lian</a>
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * bucket
     */
    private String bucket;

    @Bean
    public COSClient cosClient() {
        //初始化用户身份信息
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        //设置bucket的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        //返回cos客户端
        return new COSClient(credentials, clientConfig);
    }
}
