package io.luowei.sdk.session;

import io.luowei.sdk.model.ChatCompletionRequest;
import io.luowei.sdk.model.ChatCompletionSyncResponse;
import io.luowei.sdk.model.ImageCompletionRequest;
import io.luowei.sdk.model.ImageCompletionResponse;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.util.concurrent.CompletableFuture;


/**
 * OpenAi会话接口
 * author: luowei
 * date:
 */
public interface OpenAiSession {
    EventSource completions(ChatCompletionRequest chatCompletionRequest, EventSourceListener eventSourceListener) throws Exception;

    CompletableFuture<String> completions(ChatCompletionRequest chatCompletionRequest) throws Exception;

    ChatCompletionSyncResponse completionsSync(ChatCompletionRequest chatCompletionRequest) throws Exception;

    ImageCompletionResponse genImages(ImageCompletionRequest imageCompletionRequest) throws Exception;

    Configuration configuration();
}
