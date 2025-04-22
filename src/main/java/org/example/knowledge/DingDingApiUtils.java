package org.example.knowledge;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverResponse;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponse;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponseBody;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaPair;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.open.app.api.models.bot.ChatbotMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public String createAndDeliverCard(String cardTemplateId,ChatbotMessage message, com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCardData cardData) throws Exception {
            com.aliyun.dingtalkcard_1_0.Client client = createClient();
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverHeaders createAndDeliverHeaders = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverHeaders();
            createAndDeliverHeaders.xAcsDingtalkAccessToken = getAccessToken();


            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestDocOpenDeliverModel docOpenDeliverModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestDocOpenDeliverModel()
                    .setUserId("example_user_id");
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCoFeedOpenDeliverModel coFeedOpenDeliverModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCoFeedOpenDeliverModel()
                    .setBizTag("example_biz_tag")
                    .setGmtTimeLine(1665473229000L);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestTopOpenDeliverModel topOpenDeliverModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestTopOpenDeliverModel()
                    .setExpiredTimeMillis(1665473229000L)
                    .setUserIds(java.util.Arrays.asList(
                            "example_user_id"
                    ))
                    .setPlatforms(java.util.Arrays.asList(
                            "android"
                    ));
            java.util.Map<String, String> imRobotOpenDeliverModelExtension = TeaConverter.buildMap(
                    new TeaPair("key", "example_ext_value")
            );
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenDeliverModel imRobotOpenDeliverModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenDeliverModel()
                    .setSpaceType("IM_ROBOT")
                    .setRobotCode("dingk2eryvqsjcp6q3xp")//dingk2eryvqsjcp6q3xp
//                    .setExtension(imRobotOpenDeliverModelExtension)
                    ;
            java.util.Map<String, String> imGroupOpenDeliverModelExtension = TeaConverter.buildMap(
                    new TeaPair("key", "example_ext_value")
            );
            java.util.Map<String, String> imGroupOpenDeliverModelAtUserIds = TeaConverter.buildMap(
                    new TeaPair("key", "小明")
            );
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenDeliverModel imGroupOpenDeliverModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenDeliverModel()
                    .setRobotCode("dingg3xmqdkpaojuakm8")
                    .setAtUserIds(imGroupOpenDeliverModelAtUserIds)
                    .setRecipients(java.util.Arrays.asList(
                            "example_user_id"
                    ))
                    .setExtension(imGroupOpenDeliverModelExtension);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestTopOpenSpaceModel topOpenSpaceModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestTopOpenSpaceModel()
                    .setSpaceType("ONE_BOX");
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCoFeedOpenSpaceModel coFeedOpenSpaceModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestCoFeedOpenSpaceModel()
                    .setTitle("xxxx卡片")
                    .setCoolAppCode("coolAppCode123");
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModelNotification imRobotOpenSpaceModelNotification = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModelNotification()
                    .setAlertContent("你收到了1条卡片消息")
                    .setNotificationOff(false);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModelSearchSupport imRobotOpenSpaceModelSearchSupport = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModelSearchSupport()
                    .setSearchIcon("@lALPDgQ9q8hFhlHNAXzNAqI")
                    .setSearchTypeName("{\"zh_CN\":\"待办\",\"zh_TW\":\"待辦\",\"en_US\":\"ToDo\"}")
                    .setSearchDesc("卡片的具体描述");
            java.util.Map<String, String> imRobotOpenSpaceModelLastMessageI18n = TeaConverter.buildMap(
                    new TeaPair("key", "互动卡片消息")
            );
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModel imRobotOpenSpaceModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImRobotOpenSpaceModel()
                    .setSupportForward(false)
//                    .setLastMessageI18n(imRobotOpenSpaceModelLastMessageI18n)
//                    .setSearchSupport(imRobotOpenSpaceModelSearchSupport)
//                    .setNotification(imRobotOpenSpaceModelNotification)
                    ;

            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModelNotification imGroupOpenSpaceModelNotification = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModelNotification()
                    .setAlertContent("你收到了1条卡片消息")
                    .setNotificationOff(false);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModelSearchSupport imGroupOpenSpaceModelSearchSupport = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModelSearchSupport()
                    .setSearchIcon("@lALPDgQ9q8hFhlHNAXzNAqI")
                    .setSearchTypeName("{\"zh_CN\":\"待办\",\"zh_TW\":\"待辦\",\"en_US\":\"ToDo\"}")
                    .setSearchDesc("卡片的具体描述");
            java.util.Map<String, String> imGroupOpenSpaceModelLastMessageI18n = TeaConverter.buildMap(
                    new TeaPair("key", "互动卡片消息")
            );
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModel imGroupOpenSpaceModel = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestImGroupOpenSpaceModel()
                    .setSupportForward(false)
                    .setLastMessageI18n(imGroupOpenSpaceModelLastMessageI18n)
                    .setSearchSupport(imGroupOpenSpaceModelSearchSupport)
                    .setNotification(imGroupOpenSpaceModelNotification);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfigDynamicDataSourceConfigsPullConfig openDynamicDataConfigDynamicDataSourceConfigs0PullConfig = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfigDynamicDataSourceConfigsPullConfig()
                    .setPullStrategy("INTERVAL")
                    .setInterval(30)
                    .setTimeUnit("SECONDS");
            java.util.Map<String, String> openDynamicDataConfigDynamicDataSourceConfigs0ConstParams = TeaConverter.buildMap(
                    new TeaPair("key", "const-01")
            );
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfigDynamicDataSourceConfigs openDynamicDataConfigDynamicDataSourceConfigs0 = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfigDynamicDataSourceConfigs()
                    .setDynamicDataSourceId("example_ds_01")
                    .setConstParams(openDynamicDataConfigDynamicDataSourceConfigs0ConstParams)
                    .setPullConfig(openDynamicDataConfigDynamicDataSourceConfigs0PullConfig);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfig openDynamicDataConfig = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest.CreateAndDeliverRequestOpenDynamicDataConfig()
                    .setDynamicDataSourceConfigs(java.util.Arrays.asList(
                            openDynamicDataConfigDynamicDataSourceConfigs0
                    ));
            java.util.Map<String, String> privateDataValueKeyCardParamMap = TeaConverter.buildMap(
                    new TeaPair("key", "example_private_value")
            );
            com.aliyun.dingtalkcard_1_0.models.PrivateDataValue privateDataValueKey = new com.aliyun.dingtalkcard_1_0.models.PrivateDataValue()
                    .setCardParamMap(privateDataValueKeyCardParamMap);
            java.util.Map<String, com.aliyun.dingtalkcard_1_0.models.PrivateDataValue> privateData = TeaConverter.buildMap(
                    new TeaPair("privateDataValueKey", privateDataValueKey)
            );
        String cardInstanceId = genCardId(message);
            com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest createAndDeliverRequest = new com.aliyun.dingtalkcard_1_0.models.CreateAndDeliverRequest()
//                    .setUserId("example_user_id")
                    .setCardTemplateId(cardTemplateId)
                    .setOutTrackId(cardInstanceId)
                    .setCallbackType("STREAM")
//                    .setCallbackRouteKey("example_route_key")
                    .setCardData(cardData)
//                    .setPrivateData(privateData)
//                    .setOpenDynamicDataConfig(openDynamicDataConfig)
//                    .setImGroupOpenSpaceModel(imGroupOpenSpaceModel)
                    .setImRobotOpenSpaceModel(imRobotOpenSpaceModel)
//                    .setCoFeedOpenSpaceModel(coFeedOpenSpaceModel)
//                    .setTopOpenSpaceModel(topOpenSpaceModel)
                    .setOpenSpaceId( "dtv1.card//IM_ROBOT." + message.getSenderStaffId())
//                    .setImGroupOpenDeliverModel(imGroupOpenDeliverModel)
                    .setImRobotOpenDeliverModel(imRobotOpenDeliverModel)
//                    .setTopOpenDeliverModel(topOpenDeliverModel)
//                    .setCoFeedOpenDeliverModel(coFeedOpenDeliverModel)
//                    .setDocOpenDeliverModel(docOpenDeliverModel)
//                    .setUserIdType(1)
                    ;
            try {
                CreateAndDeliverResponse response = client.createAndDeliverWithOptions(createAndDeliverRequest, createAndDeliverHeaders, new com.aliyun.teautil.models.RuntimeOptions());
                log.info("CreateAndDeliverResponse ",response.getBody().toString());
            } catch (TeaException err) {
                if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    log.error(err.message);
                }

            } catch (Exception _err) {
                TeaException err = new TeaException(_err.getMessage(), _err);
                if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                    // err 中含有 code 和 message 属性，可帮助开发定位问题
                    log.error(err.message);
                }

            }
            return cardInstanceId;
    }

    public void updateCard(String cardInstanceId,com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardData updateCardData) throws Exception {
        log.info("updateCard");
        com.aliyun.dingtalkcard_1_0.Client client = createClient();
        com.aliyun.dingtalkcard_1_0.models.UpdateCardHeaders updateCardHeaders = new com.aliyun.dingtalkcard_1_0.models.UpdateCardHeaders();
        updateCardHeaders.xAcsDingtalkAccessToken =  getAccessToken();
        com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardUpdateOptions cardUpdateOptions = new com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardUpdateOptions()
                .setUpdateCardDataByKey(true)
                .setUpdatePrivateDataByKey(false);
        java.util.Map<String, String> privateDataValueKeyCardParamMap = TeaConverter.buildMap(
                new TeaPair("key", "example-value")
        );
        com.aliyun.dingtalkcard_1_0.models.PrivateDataValue privateDataValueKey = new com.aliyun.dingtalkcard_1_0.models.PrivateDataValue()
                .setCardParamMap(privateDataValueKeyCardParamMap);
        java.util.Map<String, com.aliyun.dingtalkcard_1_0.models.PrivateDataValue> privateData = TeaConverter.buildMap(
                new TeaPair("privateDataValueKey", privateDataValueKey)
        );
        java.util.Map<String, String> cardDataCardParamMap = TeaConverter.buildMap(
                new TeaPair("key", "example-value")
        );
        com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardData cardData = new com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest.UpdateCardRequestCardData()
                .setCardParamMap(cardDataCardParamMap);
        com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest updateCardRequest = new com.aliyun.dingtalkcard_1_0.models.UpdateCardRequest()
                .setOutTrackId(cardInstanceId)
                .setCardData(updateCardData)
//                .setPrivateData(privateData)
                .setCardUpdateOptions(cardUpdateOptions)
//                .setUserIdType(1)
                ;
        try {
            client.updateCardWithOptions(updateCardRequest, updateCardHeaders, new com.aliyun.teautil.models.RuntimeOptions());
        } catch (TeaException err) {
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error(err.message);
            }

        } catch (Exception _err) {
            TeaException err = new TeaException(_err.getMessage(), _err);
            if (!com.aliyun.teautil.Common.empty(err.code) && !com.aliyun.teautil.Common.empty(err.message)) {
                // err 中含有 code 和 message 属性，可帮助开发定位问题
                log.error(err.message);
            }

        }
    }

    private static com.aliyun.dingtalkcard_1_0.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        return new com.aliyun.dingtalkcard_1_0.Client(config);
    }

    public static String genCardId(ChatbotMessage message) throws NoSuchAlgorithmException {
        String factor = message.getSenderId() + '_' + message.getSenderCorpId() + '_' + message.getConversationId() + '_'
                + message.getMsgId() + '_' + UUID.randomUUID().toString();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(factor.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
