package im.youdu.entapp;

import com.google.gson.JsonObject;
import im.youdu.entapp.aes.AESCrypto;
import im.youdu.entapp.exception.AESCryptoException;
import im.youdu.entapp.exception.FileIOException;
import im.youdu.entapp.exception.HttpRequestException;
import im.youdu.entapp.exception.ParamParserException;
import im.youdu.entapp.message.Message;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.IOException;
import java.util.Date;


public class AppClient {

    /**
     * 文件类型
     */
    public static final String FileTypeFile = "file";
    /**
     * 图片类型
     */
    public static final String FileTypeImage = "image";
    private AESCrypto crypto;
    private String addr;
    private int buin;
    private String appId;
    private Token tokenInfo;

    /**
     * @param address        目标服务器地址，"ip:port"的形式
     * @param buin           企业号
     * @param appId          AppId
     * @param encodingAesKey encodingAesKey
     */
    public AppClient(String address, int buin, String appId, String encodingAesKey) {
        assert !address.isEmpty() && buin > 0 && !appId.isEmpty() && !encodingAesKey.isEmpty();
        this.addr = address;
        this.buin = buin;
        this.appId = appId;
        this.crypto = new AESCrypto(appId, encodingAesKey);
    }

    private String apiGetToken() {
        return EntAppApi.SCHEME + this.addr + EntAppApi.API_GET_TOKEN;
    }

    private String apiSendMsg() {
        return EntAppApi.SCHEME + this.addr + EntAppApi.API_SEND_MSG;
    }

    private String apiUploadFile() {
        return EntAppApi.SCHEME + this.addr + EntAppApi.API_UPLOAD_FILE;
    }

    private String apiDownloadFile() {
        return EntAppApi.SCHEME + this.addr + EntAppApi.API_DOWNLOAD_FILE;
    }

    private String apiSearchFile() {
        return EntAppApi.SCHEME + this.addr + EntAppApi.API_SEARCHE_FILE;
    }

    private Token getToken() throws AESCryptoException, ParamParserException, HttpRequestException {
        long now = new Date().getTime() / 1000;
        String timestamp = String.format("%d", now);
        String encryptTime = this.crypto.encrypt(timestamp.getBytes());
        JsonObject param = new JsonObject();
        param.addProperty("buin", this.buin);
        param.addProperty("appId", this.appId);
        param.addProperty("encrypt", encryptTime);

        JsonObject jsonRsp = Helper.postJson(this.apiGetToken(), param.toString());
        String encrypt = Helper.getString("encrypt", jsonRsp);
        if (encrypt.isEmpty()) {
            throw new ParamParserException("找不到返回结果的加密字段", null);
        }
        byte[] rspBuffer = this.crypto.decrypt(encrypt);
        JsonObject jsonToken = Helper.parseJson(Helper.utf8String(rspBuffer));
        String token = Helper.getString("accessToken", jsonToken);
        int expire = Helper.getInt("expireIn", jsonToken);
        if (token.isEmpty() || expire <= 0) {
            throw new ParamParserException(String.format("返回结果不合法，Token: %s, Expire: %d\n", token, expire), null);
        }
        return new Token(token, now, expire);
    }

    private void checkAndRefreshToken() throws AESCryptoException, ParamParserException, HttpRequestException {
        if (this.tokenInfo == null) {
            this.tokenInfo = this.getToken();
        }
        long endTime = this.tokenInfo.activeTime + this.tokenInfo.expire;
        if (endTime <= new Date().getTime() / 1000) {
            this.tokenInfo = this.getToken();
        }
    }

    private String tokenUri(String prefix) {
        return String.format("%s?accessToken=%s", prefix, this.tokenInfo.token);
    }

    public String getAddr() {
        return addr;
    }

    public int getBuin() {
        return buin;
    }

    public String getAppId() {
        return appId;
    }

    /**
     * 发送消息
     *
     * @param msg Message对象
     * @throws AESCryptoException   加解密失败
     * @throws ParamParserException 参数解析失败
     * @throws HttpRequestException http请求失败
     */
    public void sendMsg(Message msg) throws AESCryptoException, ParamParserException, HttpRequestException {
        this.checkAndRefreshToken();
        System.out.println(msg.toJson());
        String cipherText = this.crypto.encrypt(Helper.utf8Bytes(msg.toJson()));
        JsonObject param = new JsonObject();
        param.addProperty("buin", this.buin);
        param.addProperty("appId", this.appId);
        param.addProperty("encrypt", cipherText);
        Helper.postJson(this.tokenUri(this.apiSendMsg()), param.toString());
    }

    /**
     * 上传文件
     *
     * @param type 文件类型
     * @param name 文件名称
     * @param path 文件路径
     * @return mediaId 文件的资源ID
     * @throws AESCryptoException   加解密失败
     * @throws ParamParserException 参数解析失败
     * @throws FileIOException      文件读写失败
     * @throws HttpRequestException http请求失败
     */
    public String uploadFile(String type, String name, String path) throws
            AESCryptoException, ParamParserException, FileIOException, HttpRequestException {

        this.checkAndRefreshToken();

        String encryptFile = Helper.readAndEncryptFile(this.crypto, path);
        JsonObject fileNameJson = new JsonObject();
        fileNameJson.addProperty("type", type);
        fileNameJson.addProperty("name", name);
        String cipherName = this.crypto.encrypt(Helper.utf8Bytes(fileNameJson.toString()));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpRsp = null;
        try {
            HttpPost httpPost = new HttpPost(this.tokenUri(this.apiUploadFile()));
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("buin", String.format("%d", this.buin));
            builder.addTextBody("appId", this.appId);
            builder.addTextBody("encrypt", cipherName);
            builder.addBinaryBody("file", encryptFile.getBytes(), ContentType.TEXT_PLAIN, "file");
            httpPost.setEntity(builder.build());

            httpRsp = httpClient.execute(httpPost);
            Helper.parseHttpStatus(httpRsp);
            JsonObject jsonResult = Helper.readHttpResponse(httpRsp);
            Helper.parseApiStatus(jsonResult);
            String encryptResult = Helper.getString("encrypt", jsonResult);
            byte[] decryptBuffer = this.crypto.decrypt(encryptResult);
            String decryptResult = Helper.utf8String(decryptBuffer);
            jsonResult = Helper.parseJson(decryptResult);
            String mediaId = Helper.getString("mediaId", jsonResult);
            if (mediaId.isEmpty()) {
                throw new ParamParserException("mediaId 为空", null);
            }
            return mediaId;

        } catch (IOException e) {
            throw new HttpRequestException(0, e.getMessage(), e);
        } finally {
            Helper.close(httpRsp);
            Helper.close(httpClient);
        }
    }

    /**
     * 下载文件
     *
     * @param mediaId 文件的资源ID
     * @param destDir 目标存储目录
     * @return (文件名, 文件字节数大小, 文件内容)
     * @throws AESCryptoException   加解密失败
     * @throws ParamParserException 参数解析失败
     * @throws FileIOException      文件读写失败
     * @throws HttpRequestException http请求失败
     */
    public Triplet<String, Long, byte[]> downloadFile(String mediaId, String destDir) throws
            AESCryptoException, ParamParserException, FileIOException, HttpRequestException {

        this.checkAndRefreshToken();

        JsonObject mediaIdJson = new JsonObject();
        mediaIdJson.addProperty("mediaId", mediaId);

        String cipherId = this.crypto.encrypt(mediaIdJson.toString().getBytes());

        JsonObject param = new JsonObject();
        param.addProperty("buin", this.buin);
        param.addProperty("appId", this.appId);
        param.addProperty("encrypt", cipherId);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse httpRsp = null;
        try {
            HttpPost httpPost = new HttpPost(this.tokenUri(this.apiDownloadFile()));
            StringEntity entity = new StringEntity(param.toString(), ContentType.APPLICATION_JSON);
            entity.setContentEncoding("UTF-8");
            httpPost.setEntity(entity);

            httpRsp = httpClient.execute(httpPost);
            Header header = httpRsp.getLastHeader("encrypt");
            if (header == null) {
                Helper.parseApiStatus(Helper.readHttpResponse(httpRsp));
                throw new ParamParserException("Header找不到加密内容字段", null);
            }
            String fileInfo = header.getValue();

            byte[] fileInfoBuffer = this.crypto.decrypt(fileInfo);
            String fileInfoResult = Helper.utf8String(fileInfoBuffer);

            System.out.println(fileInfoResult);

            JsonObject fileInfoJson = Helper.parseJson(fileInfoResult);
            String name = Helper.getString("name", fileInfoJson);
            long size = Helper.getLong("size", fileInfoJson);
            if (name.isEmpty() || size <= 0) {
                throw new ParamParserException("空文件", null);
            }
            byte[] fileContent = Helper.decryptAndWriteFile(this.crypto, httpRsp, name, destDir);
            return new Triplet<String, Long, byte[]>(name, size, fileContent);
        } catch (IOException e) {
            throw new HttpRequestException(0, e.getMessage(), e);
        } finally {
            Helper.close(httpRsp);
            Helper.close(httpClient);
        }
    }

    /**
     * 搜索文件信息
     *
     * @param mediaId 资源ID
     * @return (文件名, 字节数大小)
     * @throws AESCryptoException   加解密失败
     * @throws ParamParserException 参数解析失败
     * @throws HttpRequestException http请求失败
     */
    public Pair<String, Long> searchFile(String mediaId) throws AESCryptoException, ParamParserException, HttpRequestException {

        this.checkAndRefreshToken();

        JsonObject mediaIdJson = new JsonObject();
        mediaIdJson.addProperty("mediaId", mediaId);

        String cipherId = this.crypto.encrypt(mediaIdJson.toString().getBytes());

        JsonObject param = new JsonObject();
        param.addProperty("buin", this.buin);
        param.addProperty("appId", this.appId);
        param.addProperty("encrypt", cipherId);

        JsonObject jsonRsp = Helper.postJson(tokenUri(this.apiSearchFile()), param.toString());

        String encrypt = Helper.getString("encrypt", jsonRsp);
        if (encrypt.isEmpty()) {
            throw new ParamParserException("找不到返回结果的加密字段", null);
        }
        byte[] rspBuffer = this.crypto.decrypt(encrypt);
        JsonObject jsonResult = Helper.parseJson(Helper.utf8String(rspBuffer));
        String name = Helper.getString("name", jsonResult);
        long size = Helper.getLong("size", jsonResult);
        if (name.isEmpty() || size <= 0) {
            throw new ParamParserException("文件信息不合法", null);
        }
        return new Pair<String, Long>(name, size);
    }

    private class Token {
        private String token;
        private long activeTime;
        private int expire;

        private Token(String token, long activeTime, int expire) {
            this.token = token;
            this.activeTime = activeTime;
            this.expire = expire;
        }
    }
}
