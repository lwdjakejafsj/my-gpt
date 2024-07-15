package io.luowei.chatgpt.dao.repository;

import io.luowei.chatgpt.model.chatgpt.rule.UserAccountQuotaEntity;

public interface IOpenAiRepository {

    int subAccountQuota(String openai);

    UserAccountQuotaEntity queryUserAccount(String openid);

}