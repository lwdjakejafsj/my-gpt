package io.luowei.chatgpt.service.auth;

import io.luowei.chatgpt.model.auth.AuthStateEntity;

public interface IAuthService {

    /**
     * 登录验证
     * author: luowei
     * date:
     */
    AuthStateEntity doLogin(String code);

    boolean checkToken(String token);
}
