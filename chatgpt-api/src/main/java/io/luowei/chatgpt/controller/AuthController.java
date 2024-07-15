package io.luowei.chatgpt.controller;

import com.alibaba.fastjson.JSON;
import io.luowei.chatgpt.common.constants.Constants;
import io.luowei.chatgpt.model.auth.AuthStateEntity;
import io.luowei.chatgpt.model.auth.AuthTypeVO;
import io.luowei.chatgpt.model.Response;
import io.luowei.chatgpt.service.auth.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private IAuthService authService;

    @PostMapping("/login")
    public Response<String> doLogin(@RequestParam String code) {
        log.info("鉴权验证码：{}",code);

        try {
            AuthStateEntity authStateEntity = authService.doLogin(code);
            log.info("鉴权登录校验完成，验证码: {} 结果: {}", code, JSON.toJSONString(authStateEntity));

            if (!AuthTypeVO.A0000.getCode().equals(authStateEntity.getCode())) {
                // 验证失败
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            // 放行，鉴权成功
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(authStateEntity.getToken())
                    .build();

        } catch (Exception e) {
            log.error("鉴权登录校验失败，验证码: {}", code);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

}
