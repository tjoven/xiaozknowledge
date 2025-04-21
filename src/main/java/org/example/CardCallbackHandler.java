package org.example;

import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import com.dingtalk.open.app.api.models.bot.MessageContent;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.example.knowledge.*;
import org.example.models.CardCallbackMessage;
import org.example.models.CardPrivateData;
import org.example.models.CardPrivateDataWrapper;
import org.example.service.DingDingStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

import static org.example.service.DingDingStreamService.*;
import static org.example.service.DingDingStreamService.SEARCH_STATUS_INPUT_CONTENT;

@Slf4j
@Component
public class CardCallbackHandler implements OpenDingTalkCallbackListener<String, JSONObject> {

  @Autowired
  private JSONObjectUtils jsonObjectUtils;

  @Override
  public JSONObject execute(String messageString) {
    /**
     * 卡片事件回调文档：https://open.dingtalk.com/document/orgapp/event-callback-card
     */
    log.info("card callback message: " + messageString);
    CardCallbackMessage message = JSON.parseObject(messageString, CardCallbackMessage.class);
    CardPrivateDataWrapper content = JSON.parseObject(message.getContent(), CardPrivateDataWrapper.class);
    CardPrivateData cardPrivateData = content.getCardPrivateData();
    JSONObject params = cardPrivateData.getParams();

    searchStatus = SEARCH_STATUS_INPUT_CONTENT;
    KnowledgeEntry knowledgeEntry = JSON.parseObject(params.toJSONString(), KnowledgeEntry.class);
    selectedKnowledgeList = new ArrayList<>();
    selectedKnowledgeList.add(knowledgeEntry);
    DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
    com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
    jsonObjContent.put("content","已选择 \n"+knowledgeEntry.getKnowledgeName()+"\n 有什么问题，尽管问我");

    String userId = message.getUserId();
    dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(userId));





    String local_input = params.getString("local_input");
    JSONObject userPrivateData = new JSONObject();

    if (local_input != null) {
      userPrivateData.put("private_input", local_input);
      userPrivateData.put("submitted", true);
    }

    JSONObject cardUpdateOptions = new JSONObject();
    cardUpdateOptions.put("updateCardDataByKey", true);
    cardUpdateOptions.put("updatePrivateDataByKey", true);

    JSONObject response = new JSONObject();
    response.put("cardUpdateOptions", cardUpdateOptions);
    response.put("userPrivateData",
        new JSONObject().fluentPut("cardParamMap", jsonObjectUtils.convertJSONValuesToString(userPrivateData)));

    log.info("card callback response: " + JSON.toJSONString(response));
    return response;
  }

}
