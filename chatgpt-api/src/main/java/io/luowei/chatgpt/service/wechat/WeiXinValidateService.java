package io.luowei.chatgpt.service.wechat;

public interface WeiXinValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
