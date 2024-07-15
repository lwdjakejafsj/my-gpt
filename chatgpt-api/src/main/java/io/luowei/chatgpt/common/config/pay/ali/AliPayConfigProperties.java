package io.luowei.chatgpt.common.config.pay.ali;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "alipay.config", ignoreInvalidFields = true)
public class AliPayConfigProperties {

    // 「沙箱环境」应用ID - 您的APPID，收款账号既是你的APPID对应支付宝账号。获取地址；https://open.alipay.com/develop/sandbox/app
    private String appId;
    // 「沙箱环境」商户私钥，你的PKCS8格式RSA2私钥
    private String merchantPrivateKey;
    // 「沙箱环境」支付宝公钥
    private String alipayPublicKey;
    // 「沙箱环境」服务器异步通知页面路径
    private String notifyUrl;
    // 「沙箱环境」页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    private String returnUrl;
    // 「沙箱环境」
    private String gatewayUrl;
    // 签名方式
    private String signType = "RSA2";
    // 字符编码格式
    private String charset = "utf-8";
    // 传输格式
    private String format = "json";

}
