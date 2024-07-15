package io.luowei.chatgpt.service.chatgpt.rule;

import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.chatgpt.model.chatgpt.rule.RuleLogicEntity;

// 策略 + 工厂 + 模板
public interface ILogicFilter<T> {

    RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess,T data)throws Exception;

}
