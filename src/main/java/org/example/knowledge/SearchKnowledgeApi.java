package org.example.knowledge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import okhttp3.*;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import okio.BufferedSource;
import okio.Okio;
import org.apache.cxf.jaxrs.ext.Nullable;
import org.example.network.NetworkEngine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SearchKnowledgeApi {
    private static final Logger log = LoggerFactory.getLogger(SearchKnowledgeApi.class);
    private static final String HOST = "http://172.16.52.62";
public static ArrayList getKnowledgeList()  {
    ArrayList knowledgeList = new ArrayList();
    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    JSONObject jsonParams = new JSONObject();
    RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);

    Request request = new Request.Builder().url(HOST+"/v5/ai-meta/knowledge/applicable-list")
            .post(requestBody)
            .build();
    try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
        ResponseBody body = response.body();
        if (response.code() == 200 && body != null) {
            JSONObject respObj = JSONObject.parseObject(body.string());
            String code = respObj.getString("code");
            JSONObject dataObject = respObj.getJSONObject("data");
            if(TextUtils.equals(code,"200") && dataObject != null){
               JSONArray knowledgeArr =  dataObject.getJSONArray("knowledgeBaseInfoList");

               for (int i = 0;i< knowledgeArr.size();i++){
                   JSONObject knowledge = knowledgeArr.getJSONObject(i);
                   KnowledgeEntry entry = JSON.parseObject(knowledge.toJSONString(),KnowledgeEntry.class);
                   knowledgeList.add(entry);
               }
                return knowledgeList;
            }
        }
    } catch (IOException e) {
    }
    return knowledgeList;
}

    public static boolean login(String userName,String password,String publicKey) throws Exception {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add("loginkey",userName);
        bodyBuilder.add("password",RSAUtils.encryptByPublicKey(password,publicKey));
        bodyBuilder.add("loginType","0");

        Request request = new Request.Builder().url(HOST+"/v2/user/login_new_encrypt")
                .addHeader("Content-Type","application/x-www-form-urlencoded")
                .post(bodyBuilder.build())
                .build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                String sessionId = respObj.getString("X-LENOVO-SESS-ID");
                NetworkEngine.getInstance().setSessionId(sessionId);
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }

    public static String getPublicKey(){
        Request request = new Request.Builder().url(HOST+"/v2/config/noAuth/get_publickey?secondDomain=172")
                .build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                if( respObj.containsKey("public_key")){
                    String key = respObj.getString("public_key");
                    log.info("getPublicKey answer PublicKey={}",key);
                    return key;
                }

            }
        } catch (IOException e) {
        }
        return null;
    }
    public static String getConversationList(String conversationId){
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("id", conversationId);
        String answer = "查找知识库 失败";
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+"/v5/ai-meta/dialog/get-conversation")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                if( respObj.containsKey("data")){
                    JSONObject data = respObj.getJSONObject("data");
                    JSONArray dataList = data.getJSONArray("list");
                    if(dataList.size() > 0){
                        JSONObject conversationObj = dataList.getJSONObject(0);
                        JSONArray messageList = conversationObj.getJSONArray("message");
                        if(messageList.size() > 1){
                           JSONObject messageObj = messageList.getJSONObject(1);
                           String role = messageObj.getString("role");
                           if(TextUtils.equals("assistant",role)){
                               String content = messageObj.getString("content");
                               answer = content;
                            }
                        }
                    }

                    log.info("answer {}",answer);
                    return answer;
                }

            }
        } catch (IOException e) {
        }
        return answer;
    }
    public static List<ReferenceEntry> getSearchResource(String conversationId){
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("id", conversationId);
        List<ReferenceEntry> referenceEntryList = new ArrayList<>();
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+"/v5/ai-meta/dialog/get-conversation")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                if( respObj.containsKey("data")){
                    JSONObject data = respObj.getJSONObject("data");
                    JSONArray dataList = data.getJSONArray("list");
                    if(!dataList.isEmpty()){
                        JSONObject conversationObj = dataList.getJSONObject(0);
                        referenceEntryList = JSONArray.parseArray(conversationObj.toString(),ReferenceEntry.class);
                    }

                }

            }
        } catch (IOException e) {
        }
        log.info("referenceEntryList {}",JSON.toJSONString(referenceEntryList));
        return referenceEntryList;
    }

    public static void searchBySSE1(String conversationId, String question, String selectedKnowledgeArray, SearchKnowledgeHelper.IKnowledgeCallback callback){
        OkHttpClient client = NetworkEngine.getInstance().getHttpClient();
        // 请求体
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("conversation_id", conversationId);
        jsonParams.put("question",question);
        if(!TextUtils.isEmpty(selectedKnowledgeArray)){
            jsonParams.put("knowledgeId",selectedKnowledgeArray);
        }else {
            jsonParams.put("knowledgeId","all");
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),jsonParams.toString());
        // 请求对象
        Request request = new Request.Builder()
                .url(HOST+"/v5/ai-meta/chat/completions")
                .post(body)
                .build();


        // 自定义监听器
        EventSourceListener eventSourceListener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                super.onOpen(eventSource, response);
                log.info("onOpen ");//请求到的数据
            }

            @Override
            public void onEvent(EventSource eventSource, @Nullable String id, @Nullable String type, String data) {
                //   接受消息 data
                super.onEvent(eventSource, id, type, data);
                log.info("onEvent {}",data);//请求到的数据
                if(callback != null){
                    callback.onEvent(data);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                super.onClosed(eventSource);
                log.info("onClosed ");//请求到的数据
                if(callback != null){
                    callback.onClosed();
                }
            }

            @Override
            public void onFailure(EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                super.onFailure(eventSource, t, response);
                System.out.println("onFailure");
                if(callback != null){
                    callback.onFailure();
                }
            }
        };

        RealEventSource realEventSource = new RealEventSource(request,eventSourceListener);
        realEventSource.connect(client);//真正开始请求的一步
    }


    public static KnowledgeResultEntry search(String conversationId, String question, String selectedKnowledgeArray){
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("conversation_id", conversationId);
        jsonParams.put("stream", false);
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("content",question);
        jsonMessage.put("role","user");
        JSONArray messageArr = new JSONArray();
        messageArr.add(jsonMessage);
        jsonParams.put("messages",messageArr);
        if(!TextUtils.isEmpty(selectedKnowledgeArray)){
            jsonParams.put("knowledgeId",selectedKnowledgeArray);
        }
        KnowledgeResultEntry entry = new KnowledgeResultEntry();
        entry.setAnswer("查找知识库 失败");
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+":85/v1/conversation/completion_filez")
                .addHeader("Authorization","Bearer ragflow-E4YmQzNTk4OGY5ZjExZWY4MmU2MDI0Mm")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                if( respObj.containsKey("data")){
                    JSONObject data = respObj.getJSONObject("data");
                    entry = JSON.parseObject(data.toString(),KnowledgeResultEntry.class);
                    return entry;
                }

            }
        } catch (IOException e) {
        }

        return entry;
    }

    public static String createConversation(String question,String knowledgeId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("question",question);
        if(TextUtils.isEmpty(knowledgeId)){
            jsonParams.put("knowledgeId","all");
        }else {
            jsonParams.put("knowledgeId",knowledgeId);
        }
        String conversationId = "";
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+"/v5/ai-meta/dialog/create-conversation")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                String code = respObj.getString("code");
                if(TextUtils.equals(code,"200")){
                    JSONObject data = respObj.getJSONObject("data");
                    conversationId = data.getString("conversationId");
                    log.info("conversationId {}",conversationId);
                    return conversationId;
                }

            }
        } catch (IOException e) {
        }
        return conversationId;
    }

    public static List<String> recommendationQuestions(String conversationId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("conversationId",conversationId);
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+"/v5/ai-meta/chat/recommendation-questions")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                String code = respObj.getString("code");
                if(TextUtils.equals(code,"200")){
                    JSONObject data = respObj.getJSONObject("data");
                    JSONArray questions = data.getJSONArray("questions");
                    log.info("questions {}",questions.toString());
                    ArrayList<String> questionList = new ArrayList<>();
                    for (int i = 0;i < questions.size();i++){
                        String question = questions.getString(i);
                        questionList.add(question);
                    }
                    return questionList;
                }

            }
        } catch (IOException e) {
        }
        return null;
    }

    public static List<String> getAnswerRef(String conversationId) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonParams = new JSONObject();
        jsonParams.put("id",conversationId);
        RequestBody requestBody = RequestBody.create(jsonParams.toString(), mediaType);
        Request request = new Request.Builder().url(HOST+"/v5/ai-meta/dialog/get-conversation")
                .post(requestBody).build();
        try (Response response = NetworkEngine.getInstance().getHttpClient().newCall(request).execute()) {
            ResponseBody body = response.body();
            if (response.code() == 200 && body != null) {
                JSONObject respObj = JSONObject.parseObject(body.string());
                String code = respObj.getString("code");
                if(TextUtils.equals(code,"200")){
                    JSONObject data = respObj.getJSONObject("data");
                    log.info("getAnswerRef: "+data.toJSONString());
                    JSONArray array = data.getJSONArray("list");
                    ArrayList<String> questionList = new ArrayList<>();
                    if(!array.isEmpty()){
                        JSONObject referenceObj = array.getJSONObject(0);
                        JSONObject refArray = referenceObj.getJSONObject("reference");
                        JSONArray chunksArr = refArray.getJSONArray("chunks");
                        if(!chunksArr.isEmpty()){
                            for (int j = 0;j < chunksArr.size();j++){
                                JSONObject chunkObj = chunksArr.getJSONObject(j);
                                String docName = chunkObj.getString("doc_name");
                                questionList.add(docName);
                            }
                        }

                    }

                    return questionList;
                }

            }
        } catch (IOException e) {
        }
        return null;
    }
}
