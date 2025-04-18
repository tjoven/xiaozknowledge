package org.example.models;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class CardPrivateData {
  @JSONField(name = "actionIds")
  private List<String> actionIds;
  @JSONField(name = "params")
  private JSONObject params;
}
