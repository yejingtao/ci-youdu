package im.youdu.entapp.exception;


public class HttpRequestException extends GeneralEntAppException {

    private int errCode;

    public HttpRequestException(int errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
