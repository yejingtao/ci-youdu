package im.youdu.entapp.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import im.youdu.entapp.exception.ParamParserException;

import java.util.ArrayList;
import java.util.List;

// 外链消息体
public class ExlinkBody extends MessageBody {

    private List<ExlinkBodyCell> msgList;

    /**
     * @param msgList ExlinkBodyCell的列表
     */
    public ExlinkBody(List<ExlinkBodyCell> msgList) {
        this.msgList = msgList != null ? msgList : new ArrayList<ExlinkBodyCell>();
    }

    @Override
    public String toString() {
        return "ExlinkBody{" +
                "msgList=" + msgList +
                '}';
    }

    @Override
    public MessageBody fromJsonString(String json) throws ParamParserException {
        throw new IllegalAccessError("Not support");
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
        for (ExlinkBodyCell cell : this.msgList) {
            array.add(cell.toJsonElement());
        }
        return array;
    }

    public List<ExlinkBodyCell> getMsgList() {
        return msgList;
    }
}
