package io.luowei.chatgpt.model.chatgpt;

import io.luowei.chatgpt.common.constants.Constants;
import io.luowei.sdk.model.Model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatProcessAggregate {
    /** 验证信息 */
    private String openId;

    /** 默认模型 */
    private String model = Model.GLM_3_5_TURBO.getCode();

    /** 问题描述 */
    private List<MessageEntity> messages;

    public boolean isWhiteList(String whiteListStr) {
        String[] whiteList = whiteListStr.split(Constants.SPLIT);
        for (String whiteOpenid : whiteList) {
            if (whiteOpenid.equals(openId))
                return true;
        }
        return false;
    }
}