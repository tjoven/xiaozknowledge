package org.example.knowledge;

public class DingTalkRobotUtils {
    public static final String MESSAGE_TYPE_PICTURE = "picture";
    public static final String MESSAGE_TYPE_AUDIO = "audio";
    public static final String MESSAGE_TYPE_VIDEO = "video";
    public static final String MESSAGE_TYPE_FILE = "file";
    public static final String MESSAGE_TYPE_RICHTEXT = "richText";
    public static final String MESSAGE_TYPE_TEXT = "text";

    public static boolean robotMessageCanBackUp(String msgtype){
        return TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_RICHTEXT)
                || TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_PICTURE)
                || TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_AUDIO)
                || TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_VIDEO)
                || TextUtils.equals(msgtype, DingTalkRobotUtils.MESSAGE_TYPE_FILE);
    }
}
