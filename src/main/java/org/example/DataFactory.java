package org.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaPair;
import org.example.knowledge.KnowledgeEntry;

import static org.example.service.DingDingStreamService.knowledgeList;

public class DataFactory {
    static CreateAndDeliverRequest.CreateAndDeliverRequestCardData buildAiCardData() {
        java.util.Map<String, String> cardDataCardParamMap = TeaConverter.buildMap(
                new TeaPair("content", ""),
                new TeaPair("resources", "")
        );
        com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCardData cardData = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCardData()
                .setCardParamMap(cardDataCardParamMap);
        return cardData;
    }

    public static CreateAndDeliverRequest.CreateAndDeliverRequestCardData buildKnowledgeCardData() {
        JSONArray array = new JSONArray();
        com.alibaba.fastjson2.JSONObject object0 = new com.alibaba.fastjson2.JSONObject();
        object0.put("id", "-1");
        object0.put("title", "全部");
        array.add(object0);
        for (int i = 0; i < knowledgeList.size(); i++) {
            KnowledgeEntry entry = knowledgeList.get(i);
            com.alibaba.fastjson2.JSONObject object = new com.alibaba.fastjson2.JSONObject();
            object.put("id", entry.getKnowledgeId());
            object.put("title", entry.getKnowledgeName());
            array.add(object);
        }
        java.util.Map<String, String> cardDataCardParamMap = TeaConverter.buildMap(
                new TeaPair("list", JSON.toJSONString(array))
        );
        com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCardData cardData = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCardData()
                .setCardParamMap(cardDataCardParamMap);
        return cardData;
    }
}
