package io.luowei.chatgpt.service.order;

import io.luowei.chatgpt.common.constants.Constants;
import io.luowei.chatgpt.common.exception.ChatGptException;
import io.luowei.chatgpt.dao.repository.IOrderRepository;
import io.luowei.chatgpt.model.order.entity.*;
import io.luowei.chatgpt.model.order.valobj.PayStatusVO;
import io.luowei.chatgpt.model.order.valobj.PayTypeVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
public abstract class AbstractOrderService implements IOrderService{

    @Resource
    protected IOrderRepository orderRepository;

    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) {

        try {
            String openid = shopCartEntity.getOpenid();
            Integer productId = shopCartEntity.getProductId();

            // 查询有效的未支付订单，如果存在直接返回支付 Native CodeUrl
            UnpaidOrderEntity unpaidOrderEntity = orderRepository.queryUnpaidOrder(shopCartEntity);
            if (unpaidOrderEntity != null && PayStatusVO.WAIT.equals(unpaidOrderEntity.getPayStatus()) && null != unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，已生成支付信息，返回 openid: {} orderId: {} payUrl: {}", openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getPayUrl());
                return PayOrderEntity.builder()
                        .openid(openid)
                        .payUrl(unpaidOrderEntity.getPayUrl())
                        .orderId(unpaidOrderEntity.getOrderId())
                        .payStatus(unpaidOrderEntity.getPayStatus())
                        .build();
            } else if (null != unpaidOrderEntity && null == unpaidOrderEntity.getPayUrl()) {
                log.info("创建订单-存在，未生成微信支付，返回 openid: {} orderId: {}", openid, unpaidOrderEntity.getOrderId());
                PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, unpaidOrderEntity.getOrderId(), unpaidOrderEntity.getProductName(), unpaidOrderEntity.getTotalAmount());
                log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, payOrderEntity.getOrderId(), payOrderEntity.getPayUrl());
                return payOrderEntity;
            }

            // 商品查询
            ProductEntity product = orderRepository.queryProduct(productId);
            if (!product.isAvailable()) {
                throw new ChatGptException(Constants.ResponseCode.ORDER_PRODUCT_ERR.getCode(), Constants.ResponseCode.ORDER_PRODUCT_ERR.getInfo());
            }

            // 保存订单
            OrderEntity order = this.doSaveOrder(openid, product);

            // 创建支付，支付宝支付和微信支付这里不相同
            PayOrderEntity payOrderEntity = this.doPrepayOrder(openid, order.getOrderId(), product.getProductName(), order.getTotalAmount());
            log.info("创建订单-完成，生成支付单。openid: {} orderId: {} payUrl: {}", openid, order.getOrderId(), payOrderEntity.getPayUrl());

            return payOrderEntity;

        } catch (Exception e) {
            log.error("创建订单，已生成微信支付，返回 openid: {} productId: {}", shopCartEntity.getOpenid(), shopCartEntity.getProductId());
            throw new ChatGptException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }
    }

    protected abstract OrderEntity doSaveOrder(String openid, ProductEntity productEntity);

    protected abstract PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal);
}
