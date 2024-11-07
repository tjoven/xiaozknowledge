package org.example.knowledge;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponse;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DingDingApiUtils {

    private static final Logger log = LoggerFactory.getLogger(DingDingApiUtils.class);
    private PluginConfig pluginConfig = new PluginConfig() ;

    public Client createSendMessageClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new Client(config);
    }

    public String getAccessToken() {
        String accessToken = "";
        try {
            com.aliyun.dingtalkoauth2_1_0.Client client = createAuthClient();
            GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest()
                    .setAppKey(pluginConfig.getClientId())
                    .setAppSecret(pluginConfig.getClientSecret());
//                    .setAppKey("dingk2eryvqsjcp6q3xp")
//                    .setAppSecret("nS_N3lS2hvrSFSbYpMsB5DUydiJO1gCvP_iOM_Wr5hIQ93cNadlCeMAEqdXCIEj5");
            GetAccessTokenResponse accessTokenResponse = client.getAccessToken(getAccessTokenRequest);
            if(accessTokenResponse.getStatusCode() == 200 && accessTokenResponse.getBody() != null){
                accessToken = accessTokenResponse.getBody().getAccessToken();

                log.info("accessToken:{}",accessTokenResponse.getBody().accessToken);
            }
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.info("TeaException err.code:{} err.message:{} ",err.code,err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.info("Exception err.code:{} err.message:{} ",err.code,err.message);
            }

        }
        return accessToken;
    }

    public com.aliyun.dingtalkoauth2_1_0.Client createAuthClient() throws Exception {
        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkoauth2_1_0.Client(config);
    }

    public boolean sendSingleChatMessage(JSONObject msgParam, String msgKey, List<String> userIds)  {
        log.info("sendSingleChatMessage JSONObject:{}, ",msgParam.toJSONString());

        try {
            Client client = createSendMessageClient();
            BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
            batchSendOTOHeaders.xAcsDingtalkAccessToken = getAccessToken();
            BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest()
                    .setMsgParam(msgParam.toJSONString())
                    .setMsgKey(msgKey)
                    .setRobotCode(pluginConfig.getRobotCode())
                    .setUserIds(userIds);
            BatchSendOTOResponse batchSendOTOResponse = client.batchSendOTOWithOptions(batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());
            if(batchSendOTOResponse.getStatusCode() == 200 && batchSendOTOResponse.getBody() != null){
                BatchSendOTOResponseBody body = batchSendOTOResponse.getBody();
                return true;
            }
        } catch (TeaException err) {
            if (!Common.empty(err.code) && !Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.info("sendSingleChatMessage err.code:{} err.message:{}",err.code,err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            log.info("sendSingleChatMessage err.code:{} err.message:{}",err.code,err.message);
            log.info("sendSingleChatMessage err.code:{} err.message:{}",err.code,err.message);

        }
        return false;
    }


}
