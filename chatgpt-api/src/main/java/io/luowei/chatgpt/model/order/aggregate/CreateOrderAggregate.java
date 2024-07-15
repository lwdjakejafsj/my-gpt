package io.luowei.chatgpt.model.order.aggregate;

import io.luowei.chatgpt.model.order.entity.OrderEntity;
import io.luowei.chatgpt.model.order.entity.ProductEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单对象
 * author: luowei
 * date:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderAggregate {
    /** 用户ID；微信用户唯一标识 */
    private String openid;
    /** 商品 */
    private ProductEntity product;
    /** 订单 */
    private OrderEntity order;
}