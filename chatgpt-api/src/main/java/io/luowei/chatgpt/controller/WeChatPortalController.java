package io.luowei.chatgpt.controller;

import io.luowei.chatgpt.model.weixin.MessageTextEntity;
import io.luowei.chatgpt.model.weixin.UserBehaviorMessageEntity;
import io.luowei.chatgpt.service.wechat.WeChatBehaviorService;
import io.luowei.chatgpt.service.wechat.WeChatValidateService;
import io.luowei.chatgpt.utils.XmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 微信公众号接口
 * author: luowei
 * date:
 */
@RestController
@RequestMapping("/wx/portal/{appid}")
public class WeChatPortalController {

    private Logger log = LoggerFactory.getLogger(WeChatPortalController.class);

    @Value("${wx.config.originalId}")
    private String originalId;

    @Resource
    private WeChatValidateService weChatValidateService;

    @Resource
    private WeChatBehaviorService weChatBehaviorService;

    /**
     * 处理微信服务器发来的get请求，进行签名的验证【apix.natapp1.cc 是我在 <a href="https://natapp.cn/">https://natapp.cn</a> 购买的渠道，你需要自己购买一个使用】
     * <a href="http://apix.natapp1.cc/api/v1/wx/portal/wxad979c0307864a66">http://apix.natapp1.cc/api/v1/wx/portal/wxad979c0307864a66</a>
     * <p>
     * appid     微信端AppID
     * signature 微信端发来的签名
     * timestamp 微信端发来的时间戳
     * nonce     微信端发来的随机字符串
     * echostr   微信端发来的验证字符串
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@PathVariable String appid,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {
            log.info("微信公众号验签信息{}开始 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);

            if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
                throw new IllegalArgumentException("请求参数非法，请核实!");
            }

            boolean check = weChatValidateService.checkSign(signature, timestamp, nonce);

            log.info("微信公众号验签信息{}完成 check：{}", appid, check);

            if (!check) {
                return null;
            }

            return echostr;
        } catch (Exception e) {
            log.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
            return null;
        }
    }

    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@PathVariable String appid,
                       @RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        try {

            log.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);

            // 消息转换
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);

            // 构建实体
            UserBehaviorMessageEntity entity = UserBehaviorMessageEntity.builder()
                    .openId(openid)
                    .fromUserName(message.getFromUserName())
                    .toUserName(message.getToUserName())
                    .msgType(message.getMsgType())
                    .content(StringUtils.isBlank(message.getContent()) ? null : message.getContent().trim())
                    .event(message.getEvent())
                    .createTime(new Date(Long.parseLong(message.getCreateTime()) * 1000L))
                    .build();

            // 受理消息
            String result = weChatBehaviorService.acceptUserBehavior(entity);

            log.info("接收微信公众号信息请求{}完成 {}", openid, result);

            return result;
        } catch (Exception e) {
            log.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }
    }



//    /**
//     * 签名验证
//     * author: luowei
//     */
//    @GetMapping(produces = "text/plain;charset=utf-8")
//    public String validate(@PathVariable String appid,
//                           @RequestParam(value = "signature", required = false) String signature,
//                           @RequestParam(value = "timestamp", required = false) String timestamp,
//                           @RequestParam(value = "nonce", required = false) String nonce,
//                           @RequestParam(value = "echostr", required = false) String echostr) {
//        try {
//
//            logger.info("微信公众号验签信息{}开始 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
//            if (StringUtils.isAnyBlank(appid,signature,timestamp,echostr)) {
//                throw new IllegalArgumentException("请求参数非法");
//            }
//
//            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);
//
//            logger.info("微信公众号验签信息{}完成 check：{}", appid, check);
//            if (!check) {
//                return null;
//            }
//            return echostr;
//
//        } catch (Exception e) {
//            logger.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
//            return null;
//        }
//
//    }

//    /**
//     * 应答消息 (简单请求)
//     * author: luowei
//     */
//    @PostMapping(produces = "application/xml; charset=UTF-8")
//    public String post(@PathVariable String appid,
//                       @RequestBody String requestBody,
//                       @RequestParam("signature") String signature,
//                       @RequestParam("timestamp") String timestamp,
//                       @RequestParam("nonce") String nonce,
//                       @RequestParam("openid") String openid,
//                       @RequestParam(name = "encrypt_type", required = false) String encType,
//                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
//        try {
//            logger.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);
//            // 接收用户发送的消息
//            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);
//
//
//            BehaviorMatter behaviorMatter = new BehaviorMatter();
//            behaviorMatter.setOpenId(openid);
//            behaviorMatter.setFromUserName(message.getFromUserName());
//            behaviorMatter.setMsgType(message.getMsgType());
//            behaviorMatter.setContent(StringUtils.isBlank(message.getContent()) ? "你是谁" : message.getContent().trim());
//            behaviorMatter.setEvent(message.getEvent());
//            behaviorMatter.setCreateTime(new Date(Long.parseLong(message.getCreateTime()) * 1000L));
//
////            // OpenAi 请求
////            // 构建参数
//            ChatCompletionRequest request = new ChatCompletionRequest();
//            request.setModel(Model.GLM_3_5_TURBO);
//
//            request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>(){
//                private static final long serialVersionUID = -7988151926241837899L;
//                    {
//                    add(ChatCompletionRequest.Prompt.builder()
//                            .role(Role.user.getCode())
//                            .content(behaviorMatter.getContent())
//                            .build());
//                    }
//            });
//
//            CompletableFuture<String> future = openAiSession.completions(request);
//
//            String answer = future.get();
//
//            // 构建微信消息
//            MessageTextEntity res = new MessageTextEntity();
//            res.setToUserName(behaviorMatter.getOpenId());
//            res.setFromUserName(originalId);
//            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
//            res.setMsgType("text");
//            res.setContent(answer);
//            String result = XmlUtil.beanToXml(res);
//            return result;
//
//        } catch (Exception e) {
//            logger.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
//            return "";
//        }
//
//    }



}
