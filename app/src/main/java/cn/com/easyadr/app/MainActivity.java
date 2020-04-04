package cn.com.easyadr.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import java.util.Map;

import cn.com.easyadr.app.http.IHttpService;
import cn.com.easyadr.app.http.TestObject;
import cn.com.easyadr.app.http.protocol.BaseResponse;
import cn.com.easyadr.http.HttpCall;
import cn.com.easyadr.http.HttpCallListenter;
import cn.com.easyadr.http.HttpRequester;
import cn.com.easyadr.http.Interceptor;
import cn.com.easyadr.http.JsonConvertor;

public class MainActivity extends AppCompatActivity implements Interceptor, JsonConvertor {
    private static final String SERVER_URL = "http://192.168.3.116:8080/http";

    private IHttpService httpService;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpRequester httpRequester = new HttpRequester()
                .setBaseUrl(SERVER_URL)
                .setConnectionTimeout(30 * 1000)
                .setReadTimeout(30 * 1000)
                .setInterceptor(this)
                .setJsonConvertor(this);
        httpService = httpRequester.create(IHttpService.class);
        startTest();
    }

    public void handleHttpTest(View view) {
        startTest();
    }

    public HttpCall<TestObject> test() {
        return null;
    }

    public void startTest() {
        httpService.getApi().run(new HttpCallListenter<BaseResponse>() {
            @Override
            public void onHttpResult(int httpCode, String httpBody, BaseResponse data) {
                int tmp = 0;
            }
        });

    }

    @Override
    public void preprocesseHeaders(String url, Map<String, String> headers) {
        Log.e("Http request", url);
    }

    @Override
    public <T> T json2Object(String jsonStr, Class<T> clazz) {
        return gson.fromJson(jsonStr, clazz);
    }

    @Override
    public String object2Json(Object object) {
        return gson.toJson(object);
    }
}
