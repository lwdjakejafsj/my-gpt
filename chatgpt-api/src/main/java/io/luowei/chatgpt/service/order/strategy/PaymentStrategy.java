package io.luowei.chatgpt.service.order.strategy;

import io.luowei.chatgpt.model.order.entity.PayOrderEntity;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);
}
