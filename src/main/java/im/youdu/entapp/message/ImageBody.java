package im.youdu.entapp.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import im.youdu.entapp.Helper;
import im.youdu.entapp.exception.ParamParserException;

// 图片消息体
public class ImageBody extends MessageBody {

    private String mediaId;

    public ImageBody() {
    }

    /**
     * @param mediaId 资源Id
     */
    public ImageBody(String mediaId) {
        this.mediaId = mediaId != null ? mediaId : "";
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
            throw new ParamParserException("json字段类型不匹配", null);
        }
        this.mediaId = Helper.getString("media_id", json.getAsJsonObject());
        return this;
    }

    @Override
    public JsonElement toJsonElement() {
        JsonObject json = new JsonObject();
        json.addProperty("media_id", this.mediaId);
        return json;
    }

    @Override
    public String toString() {
        return "ImageBody{" +
                "mediaId='" + mediaId + '\'' +
                '}';
    }

    public String getMediaId() {
        return mediaId;
    }
}
