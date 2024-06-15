package io.luowei.chatgpt.service.chatgpt;

import com.alibaba.fastjson2.JSON;
import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.sdk.constants.Constants;
import io.luowei.sdk.model.*;
import io.luowei.sdk.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IChatServiceImpl extends AbstractChatService {


    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(Model.GLM_3_5_TURBO);
        request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>(){
            private static final long serialVersionUID = -7988151926241837899L;
            {
                addAll(this.getPromptList());
            }

            private ArrayList<? extends ChatCompletionRequest.Prompt> getPromptList() {
                 ArrayList<ChatCompletionRequest.Prompt> list = new ArrayList();
                 chatProcess.getMessages().forEach(m -> {
                     ChatCompletionRequest.Prompt prompt = ChatCompletionRequest.Prompt.builder()
                         .role(Role.user.getCode())
                         .content(m.getContent())
                         .build();
                     list.add(prompt);
                 });

                 return list;
            }
        });

        openAiSession.completions(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                ChatCompletionResponse response = JSON.parseObject(data, ChatCompletionResponse.class);

//                List<ChatCompletionResponse.Choice> choices = response.getChoices();
//                for (ChatCompletionResponse.Choice chatChoice : choices) {
//                    ChatCompletionResponse.Delta delta = chatChoice.getDelta();
//                    if (Constants.Role.ASSISTANT.getCode().equals(delta.getRole())) continue;
//                    // 应答完成
//                    String finishReason = chatChoice.getFinishReason();
//                    if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
//                        emitter.complete();
//                        break;
//                    }
//                    // 发送信息
//                    try {
//                        emitter.send(delta.getContent());
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }

                if (EventType.finish.getCode().equals(type)) {
                    emitter.complete();
                    ChatCompletionResponse.Meta meta = com.alibaba.fastjson.JSON.parseObject(response.getMeta(), ChatCompletionResponse.Meta.class);
                    log.info("[输出结束] Tokens {}", com.alibaba.fastjson.JSON.toJSONString(meta));
                }

                // 发送信息
                try {
                    emitter.send(response.getData());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                log.info("对话完成");
            }

        });
    }
}
