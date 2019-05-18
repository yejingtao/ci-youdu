package im.youdu.entapp.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import im.youdu.entapp.Helper;
import im.youdu.entapp.exception.ParamParserException;

import java.util.*;

// 文件消息体
public class SysMsgBody extends MessageBody {
    private String title;
    private List<Map<String, MessageBody>> msg;

    public SysMsgBody() {
    }

    /**
     * @param title 标题
     */
    public SysMsgBody(String title) {
        this.title = title != null ? title : "";
        this.msg = new ArrayList<Map<String, MessageBody>>();
    }

    public SysMsgBody(String title, ArrayList<Map<String, MessageBody>> msg) {
        this.title = title != null ? title : "";
        if (msg == null) {
            msg = new ArrayList<Map<String, MessageBody>>();
        }
        this.msg = msg;
    }

    public void PutMsg(String msgType, MessageBody msgBody) {
        Map<String, MessageBody> map = new HashMap<String, MessageBody>();
        map.put(msgType, msgBody);
        this.msg.add(map);
    }

    @Override
    public String toJsonString() {
        return this.toJsonElement().toString();
    }

    @Override
    public MessageBody fromJsonString(String json) throws ParamParserException {
        JsonObject result = Helper.parseJson(json);
        return this.fromJsonElement(result);
    }

    @Override
    public MessageBody fromJsonElement(JsonElement json) throws ParamParserException {
        if (!json.isJsonObject()) {
            throw new ParamParserException("Json字段类型不匹配", null);
        }
        this.title = Helper.getString("title", json.getAsJsonObject());
        return this;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject json = new JsonObject();
        json.addProperty("title", this.title);
        JsonArray array = new JsonArray();
        for (Map map : this.msg) {
            Iterator<Map.Entry<String, MessageBody>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, MessageBody> entry = entries.next();
                JsonObject cellJson = new JsonObject();
                cellJson.add(entry.getKey(), entry.getValue().toJsonElement());
                array.add(cellJson);
            }
        }
        json.add("msg", array);
        return json;
    }

    @Override
    public String toString() {
        return "SysMsgBody{" +
                "title='" + title + '\'' +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
