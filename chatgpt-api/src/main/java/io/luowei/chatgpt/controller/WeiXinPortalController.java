package io.luowei.chatgpt.controller;

import io.luowei.chatgpt.model.receive.model.BehaviorMatter;
import io.luowei.chatgpt.model.receive.model.MessageTextEntity;
import io.luowei.chatgpt.service.WeiXinValidateService;
import io.luowei.chatgpt.utils.XmlUtil;
import io.luowei.sdk.model.ChatCompletionRequest;
import io.luowei.sdk.model.ChatCompletionSyncResponse;
import io.luowei.sdk.model.Model;
import io.luowei.sdk.model.Role;
import io.luowei.sdk.session.Configuration;
import io.luowei.sdk.session.OpenAiSession;
import io.luowei.sdk.session.OpenAiSessionFactory;
import io.luowei.sdk.session.defaults.DefaultOpenAiSessionFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 微信公众号接口
 * author: luowei
 * date:
 */
@RestController
@RequestMapping("/wx/portal/{appid}")
public class WeiXinPortalController {

    private Logger logger = LoggerFactory.getLogger(WeiXinPortalController.class);

    @Value("${wx.config.originalId}")
    private String originalId;

    private OpenAiSession openAiSession;

    @Resource
    private WeiXinValidateService weiXinValidateService;

    @Resource
    private ThreadPoolTaskExecutor taskExecutor;

    // 存放OpenAi返回结果数据
    private final Map<String, String> openAiDataMap = new ConcurrentHashMap<>();

    // 存放OpenAi调用次数数据
    private final Map<String, Integer> openAiRetryCountMap = new ConcurrentHashMap<>();

    // 初始化ai会话
    public WeiXinPortalController() {
        Configuration configuration = new Configuration();
        configuration.setApiHost("https://open.bigmodel.cn/");
        configuration.setApiSecretKey("b960e564e87f342bf86bbd80b9d15a16.yWDk5kdyLVwsNYyD");

        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);
        // 3. 开启会话
        this.openAiSession = factory.openSession();
    }

    /**
     * 签名验证
     * author: luowei
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String validate(@PathVariable String appid,
                           @RequestParam(value = "signature", required = false) String signature,
                           @RequestParam(value = "timestamp", required = false) String timestamp,
                           @RequestParam(value = "nonce", required = false) String nonce,
                           @RequestParam(value = "echostr", required = false) String echostr) {
        try {

            logger.info("微信公众号验签信息{}开始 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr);
            if (StringUtils.isAnyBlank(appid,signature,timestamp,echostr)) {
                throw new IllegalArgumentException("请求参数非法");
            }

            boolean check = weiXinValidateService.checkSign(signature, timestamp, nonce);

            logger.info("微信公众号验签信息{}完成 check：{}", appid, check);
            if (!check) {
                return null;
            }
            return echostr;

        } catch (Exception e) {
            logger.error("微信公众号验签信息{}失败 [{}, {}, {}, {}]", appid, signature, timestamp, nonce, echostr, e);
            return null;
        }

    }

    /**
     * 应答消息 (简单请求)
     * author: luowei
     */
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
            logger.info("接收微信公众号信息请求{}开始 {}", openid, requestBody);
            // 接收用户发送的消息
            MessageTextEntity message = XmlUtil.xmlToBean(requestBody, MessageTextEntity.class);

//            logger.info("请求次数：{}", null == openAiRetryCountMap.get(message.getContent().trim()) ? 1 : openAiRetryCountMap.get(message.getContent().trim()));
//
//            // 异步任务【加入超时重试，对于小体量的调用反馈，可以在重试有效次数内返回结果】
//            if (openAiDataMap.get(message.getContent().trim()) == null || "NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
//                String data = "消息处理中，请再回复我一句【" + message.getContent().trim() + "】";
//
//                Integer retryCount = openAiRetryCountMap.get(message.getContent().trim());
//
//                if(null == retryCount) {
//                    if (openAiDataMap.get(message.getContent().trim()) == null) {
//                        doChatGPTTask(message.getContent().trim());
//                    }
//
//                    logger.info("超时重试：{}", 1);
//                    openAiRetryCountMap.put(message.getContent().trim(), 1);
//                    TimeUnit.SECONDS.sleep(5);
//                    new CountDownLatch(1).await();
//                }else if (retryCount < 2) {
//                    retryCount = retryCount + 1;
//                    logger.info("超时重试：{}", retryCount);
//                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
//                    TimeUnit.SECONDS.sleep(5);
//                    new CountDownLatch(1).await();
//                } else {
//                    retryCount = retryCount + 1;
//                    logger.info("超时重试：{}", retryCount);
//                    openAiRetryCountMap.put(message.getContent().trim(), retryCount);
//                    TimeUnit.SECONDS.sleep(3);
//                    if (openAiDataMap.get(message.getContent().trim()) != null && !"NULL".equals(openAiDataMap.get(message.getContent().trim()))) {
//                        data = openAiDataMap.get(message.getContent().trim());
//                    }
//                }
//
//                // 反馈信息[文本]
//                MessageTextEntity res = new MessageTextEntity();
//                res.setToUserName(openid);
//                res.setFromUserName(originalId);
//                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
//                res.setMsgType("text");
//                res.setContent(data);
//                return XmlUtil.beanToXml(res);
//
//            }
//
//            // 反馈信息[文本]
//            MessageTextEntity res = new MessageTextEntity();
//            res.setToUserName(openid);
//            res.setFromUserName(originalId);
//            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
//            res.setMsgType("text");
//            res.setContent(openAiDataMap.get(message.getContent().trim()));
//            String result = XmlUtil.beanToXml(res);
//            logger.info("接收微信公众号信息请求{}完成 {}", openid, result);
//            openAiDataMap.remove(message.getContent().trim());
//            return result;

            BehaviorMatter behaviorMatter = new BehaviorMatter();
            behaviorMatter.setOpenId(openid);
            behaviorMatter.setFromUserName(message.getFromUserName());
            behaviorMatter.setMsgType(message.getMsgType());
            behaviorMatter.setContent(StringUtils.isBlank(message.getContent()) ? "你是谁" : message.getContent().trim());
            behaviorMatter.setEvent(message.getEvent());
            behaviorMatter.setCreateTime(new Date(Long.parseLong(message.getCreateTime()) * 1000L));

//            // OpenAi 请求
//            // 构建参数
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(Model.GLM_3_5_TURBO);

            request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>(){
                private static final long serialVersionUID = -7988151926241837899L;
                    {
                    add(ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(behaviorMatter.getContent())
                            .build());
                    }
            });

            CompletableFuture<String> future = openAiSession.completions(request);

            String answer = future.get();

            // 构建微信消息
            MessageTextEntity res = new MessageTextEntity();
            res.setToUserName(behaviorMatter.getOpenId());
            res.setFromUserName(originalId);
            res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
            res.setMsgType("text");
            res.setContent(answer);
            String result = XmlUtil.beanToXml(res);
            return result;

        } catch (Exception e) {
            logger.error("接收微信公众号信息请求{}失败 {}", openid, requestBody, e);
            return "";
        }

    }

    private void doChatGPTTask(String content) {
        openAiDataMap.put(content,"NULL");
        taskExecutor.execute(() -> {
            // 入参；模型、请求信息；记得更新最新版 ChatGLM-SDK-Java
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(Model.GLM_3_5_TURBO); // chatGLM_6b_SSE、chatglm_lite、chatglm_lite_32k、chatglm_std、chatglm_pro
            request.setPrompt(new ArrayList<ChatCompletionRequest.Prompt>() {
                private static final long serialVersionUID = -7988151926241837899L;
                {
                    add(ChatCompletionRequest.Prompt.builder()
                            .role(Role.user.getCode())
                            .content(content)
                            .build());
                }
            });
            // 同步获取结果
            try {
                // 2.1 提供的官网方法
                ChatCompletionSyncResponse response = openAiSession.completionsSync(request);
                // 保存数据
                openAiDataMap.put(content, response.getChoices().get(0).getMessage().getContent());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }


}
