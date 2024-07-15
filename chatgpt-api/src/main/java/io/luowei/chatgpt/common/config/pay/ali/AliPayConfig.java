package io.luowei.chatgpt.common.config.pay.ali;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class AliPayConfig {

    @Bean(name = "alipayClient")
    @ConditionalOnProperty(value = "alipay.config.enabled", havingValue = "true", matchIfMissing = false)
    public AlipayClient alipayClient(AliPayConfigProperties properties){

//        System.out.println("appId:  " + properties.getAppId());
//        System.out.println("MerchantPrivateKey:  " + properties.getMerchantPrivateKey());
//        System.out.println("GatewayUrl:  " + properties.getGatewayUrl());
//        System.out.println("AlipayPublicKey:  " +  properties.getAlipayPublicKey());
//        System.out.println("appId:  " + properties.getAppId());


        return new DefaultAlipayClient(properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getMerchantPrivateKey(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType());
    }

}
