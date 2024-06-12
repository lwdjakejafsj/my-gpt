package io.luowei.chatgpt.service;

public interface WeiXinValidateService {
    boolean checkSign(String signature, String timestamp, String nonce);
}
