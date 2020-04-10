package cn.com.easyadr.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.easyadr.http.annotation.Service;

public class HttpRequester {
    private String baseUrl;
    private InputStream certFileStream;
    private Map<String, MethodManager> methodInfoMap = new HashMap<>();
    private Interceptor interceptor;
    private int connectionTimeout = 0;
    private int readTimeout = 0;
    private boolean checkHostName = false;

    public HttpRequester setJsonConvertor(JsonConvertor jsonConvertor) {
        HttpCall.jsonConvertor = jsonConvertor;
        return this;
    }

    public HttpRequester setBaseUrl(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl;
        } else {
            this.baseUrl = baseUrl + "/";
        }
        return this;
    }

    public HttpRequester setCertStream(InputStream certFileStream) {
        this.certFileStream = certFileStream;
        return this;
    }

    public HttpRequester setCertString(String certFileString) {
        this.certFileStream = new ByteArrayInputStream(certFileString.getBytes());  ;
        return this;
    }

    public HttpRequester setCheckHostName(boolean checkHostName) {
        this.checkHostName = checkHostName;
        return this;
    }

    public HttpRequester setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public HttpRequester setConnectionTimeout(int timeout) {
        this.connectionTimeout = timeout;
        return this;
    }

    public HttpRequester setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    public void release() {
        if(certFileStream != null){
            try {
                certFileStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> service) {
        analysisService(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return handleInvoke(proxy, method, args);
                    }
                });
    }

    private void analysisService(Class<?> serviceClass) {
        for (Method method : serviceClass.getDeclaredMethods()) {
            Service service = method.getAnnotation(Service.class);
            if (service != null) {
                String methodName = getMethodName(method);
                methodInfoMap.put(methodName, new MethodManager(service, baseUrl, method));
            }
        }
    }

    private Object handleInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = getMethodName(method);
        MethodManager methodManager = methodInfoMap.get(methodName);

        String fullUrl = methodManager.fullUrl;
        if (fullUrl.indexOf("{") > 0) {
            fullUrl = methodManager.getRealUrl(fullUrl, args);
        }

        String params = methodManager.getUrlParams(args);
        if (params.length() > 0) {
            fullUrl = fullUrl + "?" + params;
        }

        Map<String, String> headers = methodManager.getHeaders(args);
        String body = methodManager.getBody(args);
        List<BodyPart> multipartBodies = methodManager.getMultipartBodies(args);
        if (multipartBodies.size() > 0) {
            headers.put("Content-Type", "multipart/form-data; boundary=" + Consts.BOUNDARY);
        } else {
            headers.put("Content-Type", methodManager.contentType);
        }

        Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            //return is not HttpCall<T>, and it is sync call
            return doHttpRequestSync(method, args);
        }

        Class<?> retClazz = method.getReturnType();
        HttpCall ret = (HttpCall) retClazz.newInstance();
        ret.httpMethod = methodManager.methodType;
        ret.url = fullUrl;
        ret.connectionTimeout = connectionTimeout;
        ret.readTimeout = readTimeout;
        ret.headers = headers;
        ret.body = body;
        ret.interceptor = interceptor;
        ret.downloadParams = methodManager.getDownloadParams(args);
        ret.responseBodyClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        ret.multipartBodies = multipartBodies;
        ret.caRootPem = certFileStream;
        ret.checkHostName = checkHostName;
        return ret;
    }

    private String getMethodName(Method method) {
        String name = method.getName();
        Class<?>[] params = method.getParameterTypes();

        for (Class<?> clazz : params) {
            name += clazz.getSimpleName();
        }
        return name;
    }

    private Object doHttpRequestSync(Method method, Object[] args) {
        try {
            Class<?> retClazz = method.getReturnType();
            SyncRequestResponse requestResponse = (SyncRequestResponse) retClazz.newInstance();
            requestResponse.setHttpCode(200);
            requestResponse.setBody("OK");
            return requestResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
