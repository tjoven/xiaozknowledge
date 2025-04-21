package org.example;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import com.dingtalk.open.app.api.models.bot.MessageContent;
import com.sun.xml.bind.v2.TODO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.knowledge.*;
import org.example.service.DingDingStreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import static org.example.service.DingDingStreamService.*;

@Slf4j
@Component
public class ChatBotHandler implements OpenDingTalkCallbackListener<ChatbotMessage, JSONObject> {

  @Override
  public JSONObject execute(ChatbotMessage chatbotMessage) {
    processRobotMessage(chatbotMessage);
    return null;
  }


  //保存上次 换行符之后的内容
  static StringBuffer chatMessagePart = new StringBuffer();
  private static void processRobotMessage(ChatbotMessage chatbotMessage) {

    if(!DingDingStreamService.isValidMessage(chatbotMessage)){
      return;
    }

      DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
      MessageContent text = chatbotMessage.getText();
      String content = text.getContent();

      if (TextUtils.equals(content, DingDingStreamService.COMMAND)) {
        DingDingStreamService.searchStatus = DingDingStreamService.SEARCH_STATUS_IDLE;
      }

      if (TextUtils.equals(DingDingStreamService.searchStatus, DingDingStreamService.SEARCH_STATUS_IDLE)) {
        log.info("选择知识库");
        DingDingStreamService.searchStatus = SEARCH_STATUS_KNOWLEDGE;
        try {
          KnowledgeEntry entry = new KnowledgeEntry();
          entry.setKnowledgeId("-1");
          entry.setKnowledgeName("全部");
          knowledgeList.add(0,entry);
          dingDingApiUtils.createAndDeliverCard(chatbotMessage, knowledgeList);
        } catch (Exception e) {
          log.error(e.getMessage());
        }

      } else if (TextUtils.equals(searchStatus, SEARCH_STATUS_INPUT_CONTENT)) {
        log.info("开始搜索");
        searchStatus = SEARCH_STATUS_SEARCHING;
        StringBuffer selectedKnowledgeId = new StringBuffer();
        if (selectedKnowledgeList != null) {
          for (int i = 0; i < selectedKnowledgeList.size(); i++) {
            String id = selectedKnowledgeList.get(i).getKnowledgeId();
            if( Integer.parseInt(id) < 0){
              continue;
            }
            selectedKnowledgeId.append(selectedKnowledgeList.get(i).getKnowledgeId());
            if (i < selectedKnowledgeList.size() - 1) {
              selectedKnowledgeId.append("");
            }
          }
        }
        if (TextUtils.isEmpty(conversationId)) {
          conversationId = SearchKnowledgeApi.createConversation(content, selectedKnowledgeId.toString());
        }
        chatMessagePart = new StringBuffer();
        SearchKnowledgeHelper.searchByAi(conversationId, content, selectedKnowledgeId.toString(), new SearchKnowledgeHelper.IKnowledgeCallback() {
          @Override
          public void onEvent(String message) {
            JSONObject json = com.alibaba.fastjson.JSON.parseObject(message);
            String answer = json.getString("answer");
            int index = answer.lastIndexOf("\n\n");
            String chatMessage = "";
            if (index > 0) {
              chatMessage = answer.substring(0, index);
              chatMessagePart.append(chatMessage);

              String receiveId = chatbotMessage.getSenderStaffId();
              sendSampleTextChatMessage(chatMessagePart.toString(), receiveId);


              chatMessagePart = new StringBuffer();
              chatMessagePart.append(answer.substring(index));
            } else {
              chatMessagePart.append(answer);

            }

          }


          @Override
          public void onClosed() {
            searchStatus = SEARCH_STATUS_INPUT_CONTENT;
            String receiveId = chatbotMessage.getSenderStaffId();
            sendSampleTextChatMessage(chatMessagePart.toString(), receiveId);

            String chatMessage = COMMAND_HINT;
            sendSampleTextChatMessage(chatMessage, receiveId);
          }

          @Override
          public void onFailure() {
            String receiveId = chatbotMessage.getSenderStaffId();
            sendSampleTextChatMessage("查找知识库 失败", receiveId);
            searchStatus = SEARCH_STATUS_INPUT_CONTENT;
          }
        });

      } else {
        com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
        jsonObjContent.put("content", "正在查找，请稍后再试");
        String receiveId = chatbotMessage.getSenderStaffId();
        dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(receiveId));
      }

  }
}

