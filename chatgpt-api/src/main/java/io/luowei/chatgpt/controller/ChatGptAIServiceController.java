package io.luowei.chatgpt.controller;

import com.alibaba.fastjson.JSON;
import io.luowei.chatgpt.common.exception.ChatGptException;
import io.luowei.chatgpt.model.chatgpt.ChatGPTRequestDTO;
import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.chatgpt.model.chatgpt.MessageEntity;
import io.luowei.chatgpt.service.chatgpt.IChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1")
public class ChatGptAIServiceController {

    @Resource
    private IChatService chatService;


    @PostMapping("/chat/completions")
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request
                                                , @RequestHeader("Authorization") String token
                                                , HttpServletResponse response) {
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));

        try {
            // 基础配置；流式输出(sse)、编码、禁用缓存
            response.setContentType("text/event-steam");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 构建参数
            ChatProcessAggregate aggregate = ChatProcessAggregate.builder()
                    .token(token)
                    .model(request.getModel())
                    .messages(request.getMessages().stream()
                            .map(entity -> MessageEntity.builder()
                                    .role(entity.getRole())
                                    .content(entity.getContent())
                                    .name(entity.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            return chatService.completions(aggregate);
        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGptException(e.getMessage());
        }


    }
}
