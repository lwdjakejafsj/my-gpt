package io.luowei.sdk.interceptor;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import io.luowei.sdk.session.Configuration;
import io.luowei.sdk.utils.BearerTokenUtils;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * 自定义拦截器
 * author: luowei
 * date:
 */
public class OpenAiInterceptor implements Interceptor {
    /**
     * 智普Ai，Jwt加密Token
     */
    private final Configuration configuration;

    public OpenAiInterceptor(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public @NotNull Response intercept(Chain chain) throws IOException {
        // 1. 获取原始 Request
        Request original = chain.request();
        // 2. 构建请求
        // 有两种方式 ，官网 https://open.bigmodel.cn/dev/api#http_auth
        // 这里使用的是token鉴权，还可以直接使用apikey
        Request request = original.newBuilder()
                .url(original.url())
                .header("Authorization", "Bearer " + BearerTokenUtils.getToken(configuration.getApiKey(), configuration.getApiSecret()))
                .header("Content-Type", Configuration.JSON_CONTENT_TYPE)
                .header("User-Agent", Configuration.DEFAULT_USER_AGENT)
//                .header("Accept", null != original.header("Accept") ? original.header("Accept") : Configuration.SSE_CONTENT_TYPE)
                .method(original.method(), original.body())
                .build();

        // 3. 返回执行结果
        return chain.proceed(request);
    }
}
