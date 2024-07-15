package io.luowei.chatgpt.service.chatgpt.rule.Impl;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import io.luowei.chatgpt.common.annotation.LogicStrategy;
import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.chatgpt.model.chatgpt.MessageEntity;
import io.luowei.chatgpt.model.chatgpt.rule.LogicCheckTypeVO;
import io.luowei.chatgpt.model.chatgpt.rule.RuleLogicEntity;
import io.luowei.chatgpt.model.chatgpt.rule.UserAccountQuotaEntity;
import io.luowei.chatgpt.service.chatgpt.rule.ILogicFilter;
import io.luowei.chatgpt.service.chatgpt.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 敏感词过滤
 * author: luowei
 * date:
 */
@Slf4j
@Service
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.SENSITIVE_WORD)
public class SensitiveWordFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Resource
    private SensitiveWordBs words;

    @Value("${app.config.white-list}")
    private String whiteListStr;

    @Override
    public RuleLogicEntity filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 白名单用户不做敏感词处理
        if (chatProcess.isWhiteList(whiteListStr)) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        ChatProcessAggregate newChatProcessAggregate = new ChatProcessAggregate();
        newChatProcessAggregate.setOpenId(chatProcess.getOpenId());
        newChatProcessAggregate.setModel(chatProcess.getModel());

        List<MessageEntity> list = chatProcess.getMessages().stream()
                .map(message -> {
                    String content = message.getContent();
                    String replace = words.replace(content);
                    return MessageEntity.builder()
                            .role(message.getRole())
                            .name(message.getName())
                            .content(replace)
                            .build();
                }).collect(Collectors.toList());

        newChatProcessAggregate.setMessages(list);

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .type(LogicCheckTypeVO.SUCCESS)
                .data(newChatProcessAggregate)
                .build();
    }
}

