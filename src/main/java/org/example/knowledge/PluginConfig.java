package org.example.knowledge;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class PluginConfig {


    private String localPath ;
    private String corpId = "dingf1f1c8d8abb625df24f2f5cc6abecb85";
    /**
     * 钉钉版本
     */
    private String dingTalkType;
    /**
     * 流量增购包
     */
    private int dingTalkAddedServicePackageNum;

    private String clientId = "dingk2eryvqsjcp6q3xp";
    private String clientSecret = "nS_N3lS2hvrSFSbYpMsB5DUydiJO1gCvP_iOM_Wr5hIQ93cNadlCeMAEqdXCIEj5";

    private String assistantId;
    private String assistantEmail;
    private String assistantPhone;
    private String coolAppCode;
    private String appId;
    private String robotCode = "dingk2eryvqsjcp6q3xp";
    private String templateId;
//    private String templatesRobotsId;

    /**
     * zBox首页的地址
     */
    private String zBoxHomePageUrl;

    @Override
    public String toString() {
        return "PluginConfig{" +
                "localPath='" + localPath + '\'' +
                ", corpId='" + corpId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", assistantId='" + assistantId + '\'' +
                ", assistantEmail='" + assistantEmail + '\'' +
                ", coolAppCode='" + coolAppCode + '\'' +
                ", zBoxHomePageUrl='" + zBoxHomePageUrl + '\'' +
                '}';
    }
}



