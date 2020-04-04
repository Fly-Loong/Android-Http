package cn.com.easyadr.http;

public interface HttpCallListenter<T> {
    public void onHttpResult(int httpCode, String httpBody, T data);
}
