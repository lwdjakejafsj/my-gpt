package io.luowei.chatgpt.service.wechat;

public interface WeChatValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
