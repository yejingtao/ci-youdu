package im.youdu.entapp;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import im.youdu.entapp.aes.Signature;
import im.youdu.entapp.exception.ParamParserException;
import im.youdu.entapp.exception.ServiceException;
import im.youdu.entapp.exception.SignatureException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;


public class AppServer implements HttpHandler {

    private final Map<String, AppCallbackHandler> handlers = new HashMap<String, AppCallbackHandler>();
    private HttpServer server;

    /**
     * @param port 本地服务端口
     */
    public AppServer(int port) throws ServiceException {

        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        }

        this.server.setExecutor(null);
    }

    private static String handlerKey(AppCallbackHandler handler) {
        return handlerKey(handler.getBuin(), handler.getAppId(), handler.getUri());
    }

    private static String handlerKey(int buin, String appId, String uri) {
        return String.format("%d_%s_%s", buin, appId, uri);
    }

    public void setHandler(AppCallbackHandler handler) {
        this.handlers.put(handlerKey(handler), handler);
        this.server.createContext(handler.getUri(), this);
    }

    /**
     * 启动服务
     */
    public void start() {
        this.server.start();
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String uri = t.getRequestURI().getPath();
        System.out.println(uri);

        InputStream in = t.getRequestBody();
        byte[] contentBuffer;
        try {
            contentBuffer = Helper.readInputStream(in);
        } finally {
            Helper.close(in);
        }

        try {
            String content = Helper.utf8String(contentBuffer);
            JsonObject reqJson = Helper.parseJson(content);
            int buin = Helper.getInt("toBuin", reqJson);
            String appId = Helper.getString("toApp", reqJson);
            String encrypt = Helper.getString("encrypt", reqJson);

            AppCallbackHandler handler;
            synchronized (this.handlers) {
                handler = this.handlers.get(handlerKey(buin, appId, uri));
            }

            if (handler != null) {

                Map<String, String> params = Helper.queryToMap(t.getRequestURI().getQuery());
                String timestamp = params.get("timestamp");
                String nonce = params.get("nonce");
                String msgSignature = params.get("msg_signature");

                String mySignature = Signature.generateSignature(handler.getToken(), timestamp, nonce, encrypt);

                if (mySignature.equals(msgSignature)) {

                    String packageId = handler.handle(encrypt);
                    OutputStream out = t.getResponseBody();
                    byte[] bytes;
                    if (packageId != null && !packageId.isEmpty()) {
                        bytes = packageId.getBytes();
                    } else {
                        int errcode = handler.getErrcode();
                        String errmsg = handler.getErrmsg();
                        String encryptRsp = handler.getEncrypt();
                        JsonObject result = new JsonObject();
                        result.addProperty("errcode", errcode);
                        result.addProperty("errmsg", errmsg);
                        if (encryptRsp != null && !encryptRsp.isEmpty()) {
                            result.addProperty("encrypt", encryptRsp);
                        }
                        bytes = Helper.utf8Bytes(result.toString());
                    }
                    try {
                        t.sendResponseHeaders(200, bytes.length);
                        out.write(bytes);
                    } finally {
                        Helper.close(out);
                    }
                    return;
                }
            }

        } catch (ParamParserException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        t.sendResponseHeaders(500, 0);
    }
}
