package io.luowei.chatgpt.dao.repository;

import io.luowei.chatgpt.model.order.aggregate.CreateOrderAggregate;
import io.luowei.chatgpt.model.order.entity.PayOrderEntity;
import io.luowei.chatgpt.model.order.entity.ProductEntity;
import io.luowei.chatgpt.model.order.entity.ShopCartEntity;
import io.luowei.chatgpt.model.order.entity.UnpaidOrderEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface IOrderRepository {

    UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity);

    ProductEntity queryProduct(Integer productId);

    void saveOrder(CreateOrderAggregate aggregate);

    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime);

    CreateOrderAggregate queryOrder(String orderId);

    void deliverGoods(String orderId);

    List<String> queryReplenishmentOrder();

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(String orderId);

    List<ProductEntity> queryProductList();
}
