package org.example;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.knowledge.DingDingApiUtils;
import org.example.knowledge.DingTalkRobotUtils;
import org.example.knowledge.PluginConfig;
import org.example.knowledge.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

@Slf4j
@Component
public class ChatBotHandler implements OpenDingTalkCallbackListener<ChatbotMessage, JSONObject> {

  @Override
  public JSONObject execute(ChatbotMessage chatbotMessage) {
    DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
    PluginConfig pluginConfig = new PluginConfig();
    String chatbotCorpId = chatbotMessage.getChatbotCorpId();
    if(!TextUtils.equals(chatbotCorpId,pluginConfig.getCorpId())){
      log.info("***************不是当前企业 chatbotCorpId：{} **********************",chatbotCorpId);
      return null;
    }

    String conversationType =  chatbotMessage.getConversationType();//机器人 1：单聊   2：群聊
    if(TextUtils.equals(conversationType,"2")){// 群聊
      log.info("***************不是单聊 **********************",conversationType);
      return null;
    }

    String msgtype =  chatbotMessage.getMsgtype();//消息类型。
    log.info("***************消息类型 ********************** {}",msgtype);
    if (TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_TEXT)) {
        try {
            dingDingApiUtils.createAndDeliverCard(chatbotMessage);
        } catch (Exception e) {
          log.error(e.getMessage());
        }
    }
    return null;
  }
}
