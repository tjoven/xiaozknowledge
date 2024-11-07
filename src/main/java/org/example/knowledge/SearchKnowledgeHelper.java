package org.example.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchKnowledgeHelper {
    private static final Logger log = LoggerFactory.getLogger(SearchKnowledgeHelper.class);

    public interface IKnowledgeCallback{
        void onEvent(String message);
        void onClosed();
        void onFailure();
    }
    public static KnowledgeResultEntry search(String conversationId, String question, String selectedKnowledgeArray){
        return SearchKnowledgeApi.search(conversationId, question,selectedKnowledgeArray);
    }

    public static void searchByAi(String conversationId, String question, String selectedKnowledgeArray,IKnowledgeCallback callback){
        SearchKnowledgeApi.searchBySSE1(conversationId, question,selectedKnowledgeArray,callback);
    }

    public static String searchByConversation(String conversationId, String question) {
        String response = "查找知识库 失败";
        try {
            response = SearchKnowledgeApi.getConversationList(conversationId);
        } catch (Exception e) {
            log.info("searchByConversation Exception {}",e.getMessage());
        }
        return response;
    }
}
