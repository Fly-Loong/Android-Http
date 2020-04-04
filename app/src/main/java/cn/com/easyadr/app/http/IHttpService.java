package cn.com.easyadr.app.http;

import cn.com.easyadr.app.http.protocol.BaseResponse;
import cn.com.easyadr.http.HttpCall;
import cn.com.easyadr.http.annotation.Service;
import cn.com.easyadr.http.annotation.UrlParam;
import cn.com.easyadr.http.annotation.UrlPath;

public interface IHttpService {

    @Service("getApi")
    public HttpCall<BaseResponse> getApi();

    @Service("getApi/{path1}/{path}")
    public HttpCall<BaseResponse> getWithPathApi(@UrlPath("path") String path, @UrlPath("path1") String path1);

    @Service("getApi")
    public HttpCall<BaseResponse> getWithParamApi(@UrlParam("name") String name, @UrlParam("age") int age);

    @Service("getApi")
    public HttpCall<BaseResponse> getWithHeadersApi(@UrlParam("name") String name, @UrlParam("age") int age);

    @Service("aaa")
    public SyncRequestObject test1();
}
