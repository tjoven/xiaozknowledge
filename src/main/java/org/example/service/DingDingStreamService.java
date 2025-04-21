package org.example.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.callback.DingTalkStreamTopics;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import com.dingtalk.open.app.api.models.bot.MessageContent;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import org.example.CardCallbackHandler;
import org.example.ChatBotHandler;
import org.example.knowledge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DingDingStreamService {
    private static final Logger log = LoggerFactory.getLogger(DingDingStreamService.class);
    //钉钉xyb测试 企业
    private static final String clientId = "dingk2eryvqsjcp6q3xp";
    private static final String clientSecret = "nS_N3lS2hvrSFSbYpMsB5DUydiJO1gCvP_iOM_Wr5hIQ93cNadlCeMAEqdXCIEj5";
//private static final String clientId = "dingmde68pmxq6ofgd01";
//    private static final String clientSecret = "CRQVM6WscgX8FGFe8B-u5wG_i_g1nTIixN0dSNADpGKol-qPw5QRCN_5TWDZofvF";

    public static final String SEARCH_STATUS_IDLE = "0";
    public static final String SEARCH_STATUS_KNOWLEDGE = "1";
    public static final String SEARCH_STATUS_INPUT_CONTENT = "2";
    public static final String SEARCH_STATUS_SEARCHING = "3";
    public static String searchStatus = "0";
    public static ArrayList<KnowledgeEntry> knowledgeList;
    public static String conversationId = "";
    public static final String COMMAND = "##";
    public static final String COMMAND_HINT = "*************输入 "+COMMAND+" 可重新选择知识库*************";
    public static ArrayList<KnowledgeEntry> selectedKnowledgeList = null;
    private static StringBuffer knowledgeBuffer = new StringBuffer();

    @Autowired
    private CardCallbackHandler cardCallbackHandler;
    @Autowired
    private ChatBotHandler chatBotHandler;

    public DingDingStreamService() {
        try {
            String key = SearchKnowledgeApi.getPublicKey();
            SearchKnowledgeApi.login("admin", "123qwe", key);
            knowledgeList = SearchKnowledgeApi.getKnowledgeList();
//            if(TextUtils.isEmpty(conversationId)){
//                conversationId = SearchKnowledgeApi.createConversation("钉钉测试","all");
//            }
            if(knowledgeList.size() == 0){
                knowledgeBuffer.append("请选择知识库。并用中文逗号（，）隔开 \n");
                knowledgeBuffer.append("0：全部 \n");
            }else {
                for (int i = 0; i < knowledgeList.size(); i++) {
                    if(i == 0){
                        knowledgeBuffer.append("请选择知识库。并用中文逗号（，）隔开 \n");
                        knowledgeBuffer.append("0：全部 \n");
                    }
                    KnowledgeEntry entry = knowledgeList.get(i);
                    knowledgeBuffer.append(i+1+"：").append(entry.getKnowledgeName());
                    if(i < knowledgeList.size()-1){
                        knowledgeBuffer.append("\n");
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerRobotEvent() throws Exception {
        OpenDingTalkStreamClientBuilder
                .custom()
                .credential(new AuthClientCredential(clientId, clientSecret))
                //注册机器人监听器
//                .registerCallbackListener(DingTalkStreamTopics.BOT_MESSAGE_TOPIC, new OpenDingTalkCallbackListener<ChatbotMessage, JSONObject>() {
//                    @Override
//                    public JSONObject execute(ChatbotMessage chatbotMessage) {
//                        log.info("receive robotMessage, {}", JSON.toJSONString(chatbotMessage));
////                    //开发者根据自身业务需求，处理机器人回调
//                    processRobotMessage(chatbotMessage);
//                    return new JSONObject();
//                    }
//                })
                .registerCallbackListener(DingTalkStreamTopics.BOT_MESSAGE_TOPIC,chatBotHandler)
                .registerCallbackListener(DingTalkStreamTopics.CARD_CALLBACK_TOPIC,cardCallbackHandler)
                .build().start();
    }

    //保存上次 换行符之后的内容
    static StringBuffer chatMessagePart = new StringBuffer();
    private static void processRobotMessage(ChatbotMessage chatbotMessage) {
        if(!isValidMessage(chatbotMessage)){
            return;
        }

        DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
        MessageContent text = chatbotMessage.getText();
        String content = text.getContent();

        if(TextUtils.equals(content,COMMAND)){
            searchStatus = SEARCH_STATUS_IDLE;
        }

        if(TextUtils.equals(searchStatus,SEARCH_STATUS_IDLE)){
            log.info("选择知识库");
            searchStatus = SEARCH_STATUS_KNOWLEDGE;
            com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
            jsonObjContent.put("content",knowledgeBuffer.toString());
            // 文本消息，接收人 userId
            String receiveId = chatbotMessage.getSenderStaffId();
            dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(receiveId));

        }else if(TextUtils.equals(searchStatus,SEARCH_STATUS_KNOWLEDGE)){
            selectedKnowledgeList = getSelectedKnowledgeList(content);
            log.info("输入搜索内容");
            searchStatus = SEARCH_STATUS_INPUT_CONTENT;
            com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
            StringBuffer selectedKnowledgeName = new StringBuffer();
            if(selectedKnowledgeList == null || selectedKnowledgeList.isEmpty()){
                selectedKnowledgeName.append("全部知识库");
            }else {
                for (int i = 0; i < selectedKnowledgeList.size(); i++) {
                    selectedKnowledgeName.append(selectedKnowledgeList.get(i).getKnowledgeName());
                    if(i < selectedKnowledgeList.size()-1){
                        selectedKnowledgeName.append("，");
                    }
                }
            }

            jsonObjContent.put("content","已选择 \n"+selectedKnowledgeName.toString()+"\n 有什么问题，尽管问我");
            // 文本消息，接收人 userId
            String receiveId = chatbotMessage.getSenderStaffId();
            dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(receiveId));
        }else if(TextUtils.equals(searchStatus,SEARCH_STATUS_INPUT_CONTENT)){
            log.info("开始搜索");
            searchStatus = SEARCH_STATUS_SEARCHING;
            StringBuffer selectedKnowledgeId = new StringBuffer();
            if(selectedKnowledgeList != null){
                for (int i = 0; i < selectedKnowledgeList.size(); i++) {
                    selectedKnowledgeId.append(selectedKnowledgeList.get(i).getKnowledgeId());
                    if(i < selectedKnowledgeList.size()-1){
                        selectedKnowledgeId.append("");
                    }
                }
            }
            if(TextUtils.isEmpty(conversationId)){
                conversationId = SearchKnowledgeApi.createConversation(content,selectedKnowledgeId.toString());
            }
            chatMessagePart = new StringBuffer();
            SearchKnowledgeHelper.searchByAi(conversationId, content, selectedKnowledgeId.toString(), new SearchKnowledgeHelper.IKnowledgeCallback() {
                @Override
                public void onEvent(String message) {
                    JSONObject json = com.alibaba.fastjson.JSON.parseObject(message);
                    String answer = json.getString("answer");
                    int index = answer.lastIndexOf("\n\n");
                    String chatMessage = "";
                    if(index > 0){
                        chatMessage = answer.substring(0,index);
                        chatMessagePart.append(chatMessage);

                        String receiveId = chatbotMessage.getSenderStaffId();
                        sendSampleTextChatMessage(chatMessagePart.toString(),receiveId);


                        chatMessagePart = new StringBuffer();
                        chatMessagePart.append(answer.substring(index));
                    }else {
                        chatMessagePart.append(answer);

                    }

                }


                @Override
                public void onClosed() {
                    searchStatus = SEARCH_STATUS_INPUT_CONTENT;
                    String receiveId = chatbotMessage.getSenderStaffId();
                    sendSampleTextChatMessage(chatMessagePart.toString(),receiveId);

                    String chatMessage = COMMAND_HINT;
                    sendSampleTextChatMessage(chatMessage,receiveId);
                }

                @Override
                public void onFailure() {
                    String receiveId = chatbotMessage.getSenderStaffId();
                    sendSampleTextChatMessage("查找知识库 失败",receiveId);
                    searchStatus = SEARCH_STATUS_INPUT_CONTENT;
                }
            });

        }else {
            com.alibaba.fastjson2.JSONObject jsonObjContent = new com.alibaba.fastjson2.JSONObject();
            jsonObjContent.put("content","正在查找，请稍后再试");
            String receiveId = chatbotMessage.getSenderStaffId();
            dingDingApiUtils.sendSingleChatMessage(jsonObjContent, "sampleText", Arrays.asList(receiveId));
        }


    }


    public static boolean sendSampleTextChatMessage(String content, String receiveId){
        DingDingApiUtils dingDingApiUtils = new DingDingApiUtils();
        com.alibaba.fastjson2.JSONObject referenceObj = new com.alibaba.fastjson2.JSONObject();
        referenceObj.put("content",content.trim());
        if(TextUtils.isEmpty(content)){
            return false;
        }
        // 文本消息，接收人 userId
        return dingDingApiUtils.sendSingleChatMessage(referenceObj, "sampleText", Arrays.asList(receiveId));
    }
    private static  ArrayList<KnowledgeEntry> getSelectedKnowledgeList(String content) {
        String[] ids = content.split("，");
        if(ids.length <= 0){
            return null;
        }
        ArrayList<KnowledgeEntry> selectedKnowledgeList = new ArrayList();
        for (int i = 0; i < ids.length ; i++) {
            String selectIndex = ids[i];
            if(TextUtils.equals(selectIndex,"0")){
                return null;
            }
            try {
                int index = Integer.parseInt(selectIndex);
                if((index>0 )&& index <= knowledgeList.size()){
                    selectedKnowledgeList.add(knowledgeList.get(index-1));
                }
            }catch (Exception e){
                continue;
            }

        }
        return selectedKnowledgeList;
    }

    public static boolean isValidMessage(ChatbotMessage chatbotMessage){
        PluginConfig pluginConfig = new PluginConfig();
        String chatbotCorpId = chatbotMessage.getChatbotCorpId();
        if (!TextUtils.equals(chatbotCorpId, pluginConfig.getCorpId())) {
            log.info("***************不是当前企业 chatbotCorpId：{} **********************", chatbotCorpId);
            return false;
        }

        String conversationType = chatbotMessage.getConversationType();//机器人 1：单聊   2：群聊
        if (TextUtils.equals(conversationType, "2")) {// 群聊
            log.info("***************不是单聊 **********************", conversationType);
            return false;
        }

        String msgtype = chatbotMessage.getMsgtype();//消息类型。
        MessageContent text = chatbotMessage.getText();
        String content = text.getContent();
        log.info("***************消息内容 ********************** {}", content);
        log.info("***************消息类型 ********************** {}", msgtype);
        if (TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_TEXT)) {

            return true;
        }
        return false;
    }
}
