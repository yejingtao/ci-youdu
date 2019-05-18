package im.youdu.entapp;

import im.youdu.entapp.aes.AESCrypto;
import im.youdu.entapp.exception.AESCryptoException;
import im.youdu.entapp.exception.ParamParserException;
import im.youdu.entapp.message.ReceiveMessage;


public abstract class AppCallbackHandler {

    private int buin;
    private String appId;
    private String token;
    private String uri;
    private AESCrypto crypto;
    private int errcode;
    private String errmsg;
    private String encrypt;
    /**
     * @param buin   企业总机号
     * @param appId  企业应用AppId
     * @param aesKey 企业应用AppKey
     * @param uri    回调接口的uri
     */
    public AppCallbackHandler(int buin, String appId, String aesKey, String token, String uri) {
        assert buin > 0 && !appId.isEmpty() && !aesKey.isEmpty() && !token.isEmpty() && !uri.isEmpty();
        this.buin = buin;
        this.appId = appId;
        this.token = token;
        this.uri = uri;
        this.crypto = new AESCrypto(appId, aesKey);
        this.errcode = 0;
        this.errmsg = "success";
    }

    /**
     * 处理回调
     *
     * @param encrypt 回调的加密数据
     * @return packageId，从encrypt解密取得
     */
    public abstract String handle(String encrypt);

    /**
     * 从回调的加密数据解析消息
     *
     * @param encrypt 加密数据
     * @return ReceiveMessage对象
     * @throws AESCryptoException   解密失败
     * @throws ParamParserException 参数解析失败
     */
    public ReceiveMessage parseReceiveMessage(String encrypt) throws AESCryptoException, ParamParserException {
        byte[] decryptBuffer = this.crypto.decrypt(encrypt);
        return new ReceiveMessage().fromJson(Helper.utf8String(decryptBuffer));
    }

    public int getBuin() {
        return buin;
    }

    public String getAppId() {
        return appId;
    }

    public String getToken() {
        return token;
    }

    public String getUri() {
        return uri;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getEncrypt() {
        return encrypt;
    }

    public void setResponseData(String jsonData) throws AESCryptoException, ParamParserException {
        this.encrypt = this.crypto.encrypt(Helper.utf8Bytes(jsonData));
    }

    public AESCrypto getCrypto() {
        return crypto;
    }
}
