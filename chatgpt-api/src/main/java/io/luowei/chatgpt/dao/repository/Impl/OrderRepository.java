package io.luowei.chatgpt.dao.repository.Impl;

import io.luowei.chatgpt.common.enums.OpenAIProductEnableModel;
import io.luowei.chatgpt.dao.mapper.OpenaiOrderMapper;
import io.luowei.chatgpt.dao.mapper.OpenaiProductMapper;
import io.luowei.chatgpt.dao.mapper.UserAccountMapper;
import io.luowei.chatgpt.dao.po.OpenaiOrder;
import io.luowei.chatgpt.dao.po.OpenaiProduct;
import io.luowei.chatgpt.dao.po.UserAccount;
import io.luowei.chatgpt.dao.repository.IOrderRepository;
import io.luowei.chatgpt.model.chatgpt.rule.UserAccountStatusVO;
import io.luowei.chatgpt.model.order.aggregate.CreateOrderAggregate;
import io.luowei.chatgpt.model.order.entity.*;
import io.luowei.chatgpt.model.order.valobj.OrderStatusVO;
import io.luowei.chatgpt.model.order.valobj.PayStatusVO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private OpenaiOrderMapper openaiOrderMapper;

    @Resource
    private OpenaiProductMapper openaiProductMapper;

    @Resource
    private UserAccountMapper userAccountMapper;

    @Override
    public UnpaidOrderEntity queryUnpaidOrder(ShopCartEntity shopCartEntity) {
        OpenaiOrder order = new OpenaiOrder();
        order.setOpenid(shopCartEntity.getOpenid());
        order.setProductId(shopCartEntity.getProductId());

        OpenaiOrder unpaidOrder = openaiOrderMapper.queryUnpaidOrder(order);
        if (null == unpaidOrder)
            return null;

        return UnpaidOrderEntity.builder()
                .openid(unpaidOrder.getOpenid())
                .orderId(unpaidOrder.getOrderId())
                .productName(unpaidOrder.getProductName())
                .totalAmount(unpaidOrder.getTotalAmount())
                .payUrl(unpaidOrder.getPayUrl())
                .payStatus(PayStatusVO.get(unpaidOrder.getPayStatus()))
                .build();
    }

    @Override
    public ProductEntity queryProduct(Integer productId) {
        OpenaiProduct product = openaiProductMapper.queryProductByProductId(productId);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductId(product.getProductId());
        productEntity.setProductName(product.getProductName());
        productEntity.setProductDesc(product.getProductDesc());
        productEntity.setQuota(product.getQuota());
        productEntity.setPrice(product.getPrice());
        productEntity.setEnable(OpenAIProductEnableModel.get(product.getIsEnabled()));
        return productEntity;
    }

    @Override
    public void saveOrder(CreateOrderAggregate aggregate) {
        String openid = aggregate.getOpenid();
        ProductEntity product = aggregate.getProduct();
        OrderEntity order = aggregate.getOrder();
        OpenaiOrder orderPO = new OpenaiOrder();
        orderPO.setOpenid(openid);
        orderPO.setProductId(product.getProductId());
        orderPO.setProductName(product.getProductName());
        orderPO.setProductQuota(product.getQuota());
        orderPO.setOrderId(order.getOrderId());
        orderPO.setOrderTime(order.getOrderTime());
        orderPO.setOrderStatus(order.getOrderStatus().getCode());
        orderPO.setTotalAmount(order.getTotalAmount());
        orderPO.setPayType(order.getPayTypeVO().getCode());
        orderPO.setPayStatus(PayStatusVO.WAIT.getCode());

        openaiOrderMapper.insert(orderPO);
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        OpenaiOrder orderPO = new OpenaiOrder();
        orderPO.setOpenid(payOrderEntity.getOpenid());
        orderPO.setOrderId(payOrderEntity.getOrderId());
        orderPO.setPayUrl(payOrderEntity.getPayUrl());
        orderPO.setPayStatus(payOrderEntity.getPayStatus().getCode());
        openaiOrderMapper.updateOrderPayInfo(orderPO);
    }

    @Override
    public boolean changeOrderPaySuccess(String orderId, String transactionId, BigDecimal totalAmount, Date payTime) {
        OpenaiOrder orderPO = new OpenaiOrder();
        orderPO.setOrderId(orderId);
        orderPO.setPayAmount(totalAmount);
        orderPO.setPayTime(payTime);
        orderPO.setTransactionId(transactionId);
        int count = openaiOrderMapper.changeOrderPaySuccess(orderPO);
        return count == 1;
    }
    @Override
    public CreateOrderAggregate queryOrder(String orderId) {
        OpenaiOrder order = openaiOrderMapper.queryOrderByOrderId(orderId);

        ProductEntity product = new ProductEntity();
        product.setProductId(order.getProductId());
        product.setProductName(order.getProductName());

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(order.getOrderId());
        orderEntity.setOrderTime(order.getOrderTime());
        orderEntity.setOrderStatus(OrderStatusVO.get(order.getOrderStatus()));
        orderEntity.setTotalAmount(order.getTotalAmount());

        CreateOrderAggregate createOrderAggregate = new CreateOrderAggregate();
        createOrderAggregate.setOpenid(order.getOpenid());
        createOrderAggregate.setOrder(orderEntity);
        createOrderAggregate.setProduct(product);

        return createOrderAggregate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class,timeout = 350,propagation = Propagation.REQUIRED)
    public void deliverGoods(String orderId) {
        OpenaiOrder order = openaiOrderMapper.queryOrderByOrderId(orderId);

        // 变更发货状态
        int updateOrderStatusDeliverGoodsCount = openaiOrderMapper.updateOrderStatusDeliverGoods(orderId);
        if (1 != updateOrderStatusDeliverGoodsCount)
            throw new RuntimeException("updateOrderStatusDeliverGoodsCount update count is not equal 1");

        // 账户额度变更
        UserAccount userAccount = userAccountMapper.queryUserAccount(order.getOpenid());
        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setOpenid(order.getOpenid());
        newUserAccount.setTotalQuota(order.getProductQuota());
        newUserAccount.setSurplusQuota(order.getProductQuota());
        newUserAccount.setModelTypes(order.getProductModelTypes());

        if (null != userAccount) {
            int addAccountQuotaCount = userAccountMapper.addAccountQuota(newUserAccount);
            if (1 != addAccountQuotaCount) throw new RuntimeException("addAccountQuotaCount update count is not equal 1");
        } else {
            newUserAccount.setStatus(UserAccountStatusVO.AVAILABLE.getCode());
            newUserAccount.setModelTypes("gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,chatglm_lite,chatglm_std,chatglm_pro");
            userAccountMapper.insert(newUserAccount);
        }
    }

    @Override
    public List<String> queryReplenishmentOrder() {
        return openaiOrderMapper.queryReplenishmentOrder();
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return openaiOrderMapper.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeoutCloseOrderList() {
        return openaiOrderMapper.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return openaiOrderMapper.changeOrderClose(orderId);
    }

    @Override
    public List<ProductEntity> queryProductList() {
        List<OpenaiProduct> openAIProductPOList =  openaiProductMapper.queryProductList();
        List<ProductEntity> productEntityList = new ArrayList<>(openAIProductPOList.size());
        for (OpenaiProduct openaiProduct : openAIProductPOList) {
            ProductEntity productEntity = new ProductEntity();
            productEntity.setProductId(openaiProduct.getProductId());
            productEntity.setProductName(openaiProduct.getProductName());
            productEntity.setProductDesc(openaiProduct.getProductDesc());
            productEntity.setQuota(openaiProduct.getQuota());
            productEntity.setPrice(openaiProduct.getPrice());
            productEntityList.add(productEntity);
        }
        return productEntityList;
    }
}
