package cn.com.easyadr.http;

import java.util.Map;

public interface Interceptor {
    public void preprocesseHeaders(String url, Map<String, String> headers);
}
