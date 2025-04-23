package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest;
import com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaPair;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import com.dingtalk.open.app.api.models.bot.MessageContent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.knowledge.*;
import org.example.service.DingDingStreamService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static org.example.service.DingDingStreamService.*;

@Slf4j
@Component
public class ChatBotHandler implements OpenDingTalkCallbackListener<ChatbotMessage, JSONObject> {

    public static final String KnowledgeCardTemplateId = "290b8d46-6056-45ec-b063-6a434df3a68c.schema";
    public static final String AiCardTemplateId = "c7407136-9714-4be0-8aab-e9eee3edf05c.schema";

    @Override
    public JSONObject execute(ChatbotMessage chatbotMessage) {
        ApplicationConstant.chatbotMessage = chatbotMessage;
        processRobotMessage(chatbotMessage);
//        DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
//        try {
//            String aiCardInstanceId = dingDingApiUtils.createAndDeliverCard(AiCardTemplateId, chatbotMessage, buildAiCardData());
//            streaming(aiCardInstanceId, "content", "结束", true, true, false);
//            dingDingApiUtils.updateCard(aiCardInstanceId,buildAiUpdateCardData());
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
        return null;
    }

    private static UpdateCardRequest.UpdateCardRequestCardData buildAiUpdateCardData() {
        List<String> questionList = SearchKnowledgeApi.recommendationQuestions(conversationId);
        List<FileEntry> answerRefList = SearchKnowledgeApi.getAnswerRef(conversationId);
        log.info("buildAiUpdateCardData questionList:"+JSON.toJSONString(questionList));
        log.info("buildAiUpdateCardData answerRefList:"+JSON.toJSONString(answerRefList));
        JSONArray questionArray = new JSONArray();
        if(questionList != null && !questionList.isEmpty()){
            for (int i = 0; i < questionList.size(); i++) {
                com.alibaba.fastjson2.JSONObject object = new com.alibaba.fastjson2.JSONObject();
                object.put("name",questionList.get(i));
                questionArray.add(object);
            }
        }
        JSONArray answerRefArray = new JSONArray();
        if(answerRefList != null && !answerRefList.isEmpty()){
            for (int i = 0; i < answerRefList.size(); i++) {
                com.alibaba.fastjson2.JSONObject object = new com.alibaba.fastjson2.JSONObject();
                FileEntry entry = answerRefList.get(i);
                object.put("file_name",entry.getName());
                String url = String.format("http://172.16.52.62/v/preview/ent/%1s?self_nsid=%2s",entry.getNeid(),entry.getNsid());
                object.put("link",url);
                answerRefArray.add(object);
            }
        }


        java.util.Map<String, String> cardDataCardParamMap = TeaConverter.buildMap(
                new TeaPair("resources", JSON.toJSONString(questionArray)),
                new TeaPair("resources_ref", JSON.toJSONString(answerRefArray))
        );
        com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardData cardData = new com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardData()
                .setCardParamMap(cardDataCardParamMap);
        return cardData;
    }


    public static void processRobotMessage(ChatbotMessage chatbotMessage) {

        if(chatbotMessage == null){
            return;
        }
        if (!DingDingStreamService.isValidMessage(chatbotMessage)) {
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
                dingDingApiUtils.createAndDeliverCard(KnowledgeCardTemplateId, chatbotMessage, DataFactory.buildKnowledgeCardData());
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
                    if (Integer.parseInt(id) < 0) {
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
            try {
                String aiCardInstanceId = dingDingApiUtils.createAndDeliverCard(AiCardTemplateId, chatbotMessage, DataFactory.buildAiCardData());

                Semaphore semaphore = new Semaphore(0);
                StringBuilder fullContent = new StringBuilder();
                StreamState state = new StreamState();

                SearchKnowledgeHelper.searchByAi(conversationId, content, selectedKnowledgeId.toString(), new SearchKnowledgeHelper.IKnowledgeCallback() {
                    @Override
                    public void onEvent(String message) {
                        JSONObject json = com.alibaba.fastjson.JSON.parseObject(message);
                        String answer = json.getString("answer");
                        fullContent.append(answer);
                        String content = fullContent.toString();
                        int fullContentLen = content.length();
                        if (fullContentLen - state.contentLen > 20) {
                            streaming(aiCardInstanceId, "content", content, true, false, false);
                            log.info("调用流式更新接口更新内容：current_length=" + state.contentLen + ", next_length=" + fullContentLen);
                            state.contentLen = fullContentLen;
                        }

                    }


                    @Override
                    public void onClosed() {
                        searchStatus = SEARCH_STATUS_INPUT_CONTENT;
                        streaming(aiCardInstanceId, "content", fullContent.toString(), true, true, false);
                        try {
                            //打字机 的搜索内容展示完，显示相关推荐
                            dingDingApiUtils.updateCard(aiCardInstanceId,buildAiUpdateCardData());
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        semaphore.release();
                    }

                    @Override
                    public void onFailure() {
                        searchStatus = SEARCH_STATUS_INPUT_CONTENT;
                        log.error("streamCallWithMessage get exception, msg: " + "查找知识库 失败");
                        streaming(aiCardInstanceId, "content", fullContent.toString(), true, false, true);
                        semaphore.release();
                    }
                });
                semaphore.acquire();
            } catch (Exception e) {
                log.error(e.getMessage());
            }

        } else {
            com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
            jsonObjContent.put("content", "正在查找，请稍后再试");
            String receiveId = chatbotMessage.getSenderStaffId();
            dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(receiveId));
        }

    }


    public static void streaming(
            String cardInstanceId,
            String contentKey,
            String contentValue,
            Boolean isFull,
            Boolean isFinalize,
            Boolean isError) {

        // 流式更新: https://open.dingtalk.com/document/isvapp/api-streamingupdate
        JSONObject data = new JSONObject().fluentPut("outTrackId", cardInstanceId);
        data.put("key", contentKey);
        data.put("content", contentValue);
        data.put("isFull", isFull);
        data.put("isFinalize", isFinalize);
        data.put("isError", isError);
        data.put("guid", UUID.randomUUID().toString());

        String url = "https://api.dingtalk.com" + "/v1.0/card/streaming";

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(data.toJSONString(), JSON);

        DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("x-acs-dingtalk-access-token", dingDingApiUtils.getAccessToken())
                .put(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            log.info("streaming update card: " + data.toJSONString());
            if (response.code() != 200) {
                log.error("streaming update card failed: " + response.code() + " " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class StreamState {
        int contentLen = 0;
    }
}

