package im.youdu.entapp.message;

import com.google.gson.JsonElement;
import im.youdu.entapp.exception.ParamParserException;

// 消息体接口
public abstract class MessageBody {

    public abstract MessageBody fromJsonString(String json) throws ParamParserException;

    public abstract MessageBody fromJsonElement(JsonElement json) throws ParamParserException;

    public abstract String toJsonString();

    public abstract JsonElement toJsonElement();

    public TextBody getAsTextBody() {
        if (this instanceof TextBody) {
            return (TextBody) this;
        }
        return null;
    }

    public ImageBody getAsImageBody() {
        if (this instanceof ImageBody) {
            return (ImageBody) this;
        }
        return null;
    }

    public FileBody getAsFileBody() {
        if (this instanceof FileBody) {
            return (FileBody) this;
        }
        return null;
    }

    public MpnewsBody getAsMpnewsBody() {
        if (this instanceof MpnewsBody) {
            return (MpnewsBody) this;
        }
        return null;
    }

    public ExlinkBody getAsExlinkBody() {
        if (this instanceof ExlinkBody) {
            return (ExlinkBody) this;
        }
        return null;
    }

    public SysMsgBody getAsSysMsgBody() {
        if (this instanceof SysMsgBody) {
            return (SysMsgBody) this;
        }
        return null;
    }

    public SmsBody getAsSmsBody() {
        if (this instanceof SmsBody) {
            return (SmsBody) this;
        }
        return null;
    }
}
