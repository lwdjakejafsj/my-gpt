package io.luowei.chatgpt.service.chatgpt;

import io.luowei.chatgpt.common.constants.Constants;
import io.luowei.chatgpt.common.exception.ChatGptException;
import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.sdk.model.ChatCompletionRequest;
import io.luowei.sdk.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;

@Slf4j
public abstract class AbstractChatService implements IChatService{

    @Resource
    protected OpenAiSession openAiSession;


    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess) {
        // 1. 校验权限
        if (!"lllwww".equals(chatProcess.getToken())) {
            throw new ChatGptException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
        }

        // 请求应答
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
        emitter.onCompletion(() -> {
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });

        emitter.onError(throwable ->
                log.error("流式问答请求错误，使用模型：{}", chatProcess.getModel(), throwable)
        );

        // 应答处理
        // 3. 应答处理
        try {
            this.doMessageResponse(chatProcess, emitter);
        } catch (Exception e) {
            throw new ChatGptException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
        // 4. 返回结果
        return emitter;
    }

    public abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception;
}
