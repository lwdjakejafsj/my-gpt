package io.luowei.chatgpt.common.config;

import io.luowei.sdk.session.OpenAiSession;
import io.luowei.sdk.session.OpenAiSessionFactory;
import io.luowei.sdk.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatGptConfig {

    @Bean
    public OpenAiSession getOpenAiSession() {
        // 1. 配置文件
        io.luowei.sdk.session.Configuration configuration = new io.luowei.sdk.session.Configuration();
        configuration.setApiHost("https://open.bigmodel.cn/");
        configuration.setApiSecretKey("b960e564e87f342bf86bbd80b9d15a16.yWDk5kdyLVwsNYyD");
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        return factory.openSession();
    }

}
