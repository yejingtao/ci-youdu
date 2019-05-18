package im.youdu.entapp.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import im.youdu.entapp.exception.ParamParserException;

import java.util.ArrayList;
import java.util.List;

// 图文消息体
public class MpnewsBody extends MessageBody {

    private List<MpnewsBodyCell> msgList;

    /**
     * @param msgList MpnewsBodyCell的列表
     */
    public MpnewsBody(List<MpnewsBodyCell> msgList) {
        this.msgList = msgList != null ? msgList : new ArrayList<MpnewsBodyCell>();
    }

    @Override
    public String toJsonString() {
        return this.toJsonElement().toString();
    }

    @Override
    public MessageBody fromJsonElement(JsonElement json) throws ParamParserException {
        throw new IllegalAccessError("Not support");
    }

    @Override
    public JsonElement toJsonElement() {
        JsonArray array = new JsonArray();
        for (MpnewsBodyCell cell : this.msgList) {
            array.add(cell.toJsonElement());
        }
        return array;
    }

    @Override
    public MessageBody fromJsonString(String json) throws ParamParserException {
        throw new IllegalAccessError("Not support");
    }

    @Override
    public String toString() {
        return "MpnewsBody{" +
                "msgList=" + msgList +
                '}';
    }

    public List<MpnewsBodyCell> getMsgList() {
        return msgList;
    }
}
