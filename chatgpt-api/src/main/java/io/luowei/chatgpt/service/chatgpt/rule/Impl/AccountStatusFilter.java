package io.luowei.chatgpt.service.chatgpt.rule.Impl;

import io.luowei.chatgpt.common.annotation.LogicStrategy;
import io.luowei.chatgpt.model.chatgpt.ChatProcessAggregate;
import io.luowei.chatgpt.model.chatgpt.rule.LogicCheckTypeVO;
import io.luowei.chatgpt.model.chatgpt.rule.RuleLogicEntity;
import io.luowei.chatgpt.model.chatgpt.rule.UserAccountQuotaEntity;
import io.luowei.chatgpt.model.chatgpt.rule.UserAccountStatusVO;
import io.luowei.chatgpt.service.chatgpt.rule.ILogicFilter;
import io.luowei.chatgpt.service.chatgpt.rule.factory.DefaultLogicFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 账户校验
 * author: luowei
 * date:
 */
@Slf4j
@Service
@LogicStrategy(logicMode = DefaultLogicFactory.LogicModel.ACCOUNT_STATUS)
public class AccountStatusFilter implements ILogicFilter<UserAccountQuotaEntity> {

    @Override
    public RuleLogicEntity<ChatProcessAggregate> filter(ChatProcessAggregate chatProcess, UserAccountQuotaEntity data) throws Exception {
        // 账户可用，直接放行
        if (UserAccountStatusVO.AVAILABLE.equals(data.getUserAccountStatusVO())) {
            return RuleLogicEntity.<ChatProcessAggregate>builder()
                    .type(LogicCheckTypeVO.SUCCESS).data(chatProcess).build();
        }

        return RuleLogicEntity.<ChatProcessAggregate>builder()
                .info("您的账户已冻结，暂时不可使用。如果有疑问，可以联系客户解冻账户。")
                .type(LogicCheckTypeVO.REFUSE).data(chatProcess).build();
    }

}
