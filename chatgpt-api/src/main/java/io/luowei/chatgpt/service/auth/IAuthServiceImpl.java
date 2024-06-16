package io.luowei.chatgpt.service.auth;

import com.google.common.cache.Cache;
import io.luowei.chatgpt.model.auth.AuthStateEntity;
import io.luowei.chatgpt.model.auth.AuthTypeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class IAuthServiceImpl extends AbstractAuthService{

    @Resource
    private Cache<String,String> codeCache;

    @Override
    protected AuthStateEntity checkCode(String code) {

        String openId = codeCache.getIfPresent(code);

        if (StringUtils.isBlank(openId)){
            log.info("鉴权，用户收入的验证码不存在 {}", code);
            return AuthStateEntity.builder()
                    .code(AuthTypeVO.A0001.getCode())
                    .info(AuthTypeVO.A0001.getInfo())
                    .build();
        }

        // 移除缓存key值
        codeCache.invalidate(openId);
        codeCache.invalidate(code);

        return null;
    }

    @Override
    public boolean checkToken(String token) {
        return false;
    }
}
