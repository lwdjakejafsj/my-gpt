package io.luowei.chatgpt.service.order.strategy;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import io.luowei.chatgpt.model.order.entity.PayOrderEntity;
import io.luowei.chatgpt.model.order.valobj.PayStatusVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;


@Component
@ConditionalOnProperty(value = "alipay.config.enabled", havingValue = "true")
public class AliPayStrategy implements PaymentStrategy{

    @Value("${alipay.config.notify-url}")
    private String notifyUrl;

    @Value("${alipay.config.return-url}")
    private String returnUrl;

    @Resource
    private AlipayClient alipayClient;

    @Override
    public PayOrderEntity doPrepayOrder(String openid, String orderId, String productName, BigDecimal amountTotal) {

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);
        bizContent.put("total_amount", amountTotal.toString());
        bizContent.put("subject", productName);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());

        String form = "";
        if (null != alipayClient) {
            try {
                form = alipayClient.pageExecute(request).getBody();
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        } else {
            form = "因你未配置支付渠道，所以暂时不能生成有效的支付URL。请配置支付渠道后，在application-dev.yml中配置支付渠道信息";
        }

        PayOrderEntity payOrderEntity = PayOrderEntity.builder()
                .openid(openid)
                .orderId(orderId)
                .payUrl(form)
                .payStatus(PayStatusVO.WAIT)
                .build();

        return payOrderEntity;
    }
}
