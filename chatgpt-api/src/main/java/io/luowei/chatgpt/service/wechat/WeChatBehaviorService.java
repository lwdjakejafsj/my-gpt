package io.luowei.chatgpt.service.wechat;

import io.luowei.chatgpt.model.weixin.UserBehaviorMessageEntity;

public interface WeChatBehaviorService {

    String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity);

}
