package im.youdu.entapp.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import im.youdu.entapp.Helper;
import im.youdu.entapp.exception.ParamParserException;

// 接收消息类
public class ReceiveMessage {

    public static final String MessageTypeText = "text";
    public static final String MessageTypeImage = "image";
    public static final String MessageTypeFile = "file";
    public static final String MessageTypeSms = "sms";
    private String fromUser = "";
    private long createTime;
    private String packgeId;
    private String msgType = "";
    private MessageBody msgBody;

    public ReceiveMessage() {
    }

    public ReceiveMessage fromJson(String json) throws ParamParserException {
        JsonObject result = Helper.parseJson(json);
        this.fromUser = Helper.getString("fromUser", result);
        this.createTime = Helper.getLong("createTime", result);
        this.packgeId = Helper.getString("packageId", result);
        this.msgType = Helper.getString("msgType", result);
        JsonElement jsonBody = result.get(this.msgType);
        if (jsonBody == null) {
            throw new ParamParserException("找不到消息体", null);
        }

        if (this.msgType.equals(MessageTypeText)) {
            this.msgBody = new TextBody();
        } else if (this.msgType.equals(MessageTypeImage)) {
            this.msgBody = new ImageBody();
        } else if (this.msgType.equals(MessageTypeFile)) {
            this.msgBody = new FileBody();
        } else if (this.msgType.equals(MessageTypeSms)) {
            this.msgBody = new SmsBody();
        } else {
            throw new ParamParserException(String.format("无法识别的消息类型 %s", this.msgType), null);
        }
        this.msgBody.fromJsonElement(jsonBody);
        return this;
    }

    @Override
    public String toString() {
        return "ReMessage{" +
                "fromUser='" + fromUser + '\'' +
                ", createTime=" + createTime +
                ", packgeId=" + packgeId +
                ", msgType='" + msgType + '\'' +
                ", msgBody=" + msgBody.toString() +
                '}';
    }

    /**
     * @return 发送消息的用户
     */
    public String getFromUser() {
        return fromUser;
    }

    /**
     * @return 消息的发送时间
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * @return packageId，需要返回给有度服务表示接收成功
     */
    public String getPackgeId() {
        return packgeId;
    }

    /**
     * @return 消息类型
     */
    public String getMsgType() {
        return msgType;
    }

    /**
     * @return 消息体
     */
    public MessageBody getMsgBody() {
        return msgBody;
    }
}
