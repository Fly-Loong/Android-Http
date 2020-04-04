package cn.com.easyadr.http;

public abstract class SyncRequestResponse {
    private int httpCode;
    private String body;

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
