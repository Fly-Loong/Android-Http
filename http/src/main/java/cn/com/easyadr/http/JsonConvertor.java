package cn.com.easyadr.http;

public interface JsonConvertor {
    public <T> T json2Object(String jsonStr, Class<T> clazz);

    public String object2Json(Object object);
}
