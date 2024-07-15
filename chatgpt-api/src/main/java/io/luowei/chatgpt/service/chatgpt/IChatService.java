package io.luowei.chatgpt.service.chatgpt;

import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.sdk.model.ChatCompletionRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public interface IChatService {

    ResponseBodyEmitter completions(ResponseBodyEmitter emitter,ChatProcessAggregate aggregate);

}
