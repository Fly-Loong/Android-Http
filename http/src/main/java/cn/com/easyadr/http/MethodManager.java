package cn.com.easyadr.http;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.easyadr.http.annotation.Header;
import cn.com.easyadr.http.annotation.SavedPath;
import cn.com.easyadr.http.annotation.Service;
import cn.com.easyadr.http.annotation.UrlParam;
import cn.com.easyadr.http.annotation.UrlPath;

public class MethodManager<T> {
    String methodType;
    String fullUrl;
    String contentType;
    Method method;
    List<MethodParam> methodParams;

    MethodManager(Service service, String baseUrl, Method method) {
        try {
            methodType = service.method().toUpperCase();
            if (service.value().startsWith("/")) {
                this.fullUrl = baseUrl + service.value().substring(1);
            } else {
                this.fullUrl = baseUrl + service.value();
            }
            contentType = service.contentType();
            this.method = method;
            methodParams = getMethodParams();

        } catch (Exception e) {
            throw new RuntimeException("URL format is wrong");
        }

    }

    public String getRealUrl(String fullUrl, Object[] args) {
        String ret = fullUrl;

        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);
            if (methodParam.annotationName.equals("UrlPath")) {
                UrlPath an = (UrlPath) methodParam.annotation;
                String path = "{" + an.value() + "}";
                ret.replace(path, args[methodParam.argIndex] + "");
            }
        }
        return ret;
    }

    public String getUrlParams(Object[] args) {
        String ret = "";

        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);
            if (methodParam.annotationName.equals("UrlParam")) {
                UrlParam an = (UrlParam) methodParam.annotation;
                if (ret.length() > 0) {
                    ret += "&";
                }
                ret += (an.value() + "=" + args[methodParam.argIndex]);
            }
        }
        return ret;
    }

    public Map<String, String> getHeaders(Object[] args) {
        Map<String, String> headers = new HashMap<>();

        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);

            if (methodParam.annotationName.equals("Header")) {
                Header an = (Header) methodParam.annotation;
                headers.put(an.value(), args[i] + "");
            } else if (methodParam.annotationName.equals("Headers")) {
                Map<String, String> headersIn = (Map<String, String>) args[methodParam.argIndex];
                headers.putAll(headersIn);
            }
        }
        return headers;
    }


    public String getBody(Object[] args) {
        String ret = "";
        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);
            if (methodParam.annotationName.equals("Body")) {
                ret = HttpCall.jsonConvertor.object2Json(args[methodParam.argIndex]);
                break;
            }
        }
        return ret;
    }

    public String getDownloadParams(Object[] args) {
        String ret = "";
        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);
            if (methodParam.annotationName.equals("SavePath")) {
                SavedPath savedPath = (SavedPath) methodParam.annotation;
                int type = savedPath.value();
                String path = (String) args[methodParam.argIndex];
                ret = type + "\t" + path;
                break;
            }
        }
        return ret;
    }

    public List<BodyPart> getMultipartBodies(Object[] args) {
        List<BodyPart> multipartBodies = new ArrayList<>();
        for (int i = 0; i < methodParams.size(); i++) {
            MethodParam methodParam = methodParams.get(i);
            if (methodParam.annotationName.equals("BodyPart")) {
                multipartBodies = (List<BodyPart>) args[methodParam.argIndex];
                break;
            }
        }
        return multipartBodies;
    }

    private List<MethodParam> getMethodParams() {
        List<MethodParam> params = new ArrayList<>();
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] types = method.getParameterTypes();
        int inputParamsLength = types.length;
        for (int i = 0; i < inputParamsLength; i++) {
            Object inputParam = null;
            if (annotations[i].length == 1) {
                Annotation annotation = annotations[i][0];
                Class<?> anClazz = annotation.annotationType();
                String anName = anClazz.getSimpleName();
                params.add(new MethodParam(annotation, anName, i));
            }
        }
        return params;
    }

    String getMethodType() {
        return methodType;
    }

    String getFullUrl() {
        return fullUrl;
    }

    String getContentType() {
        return contentType;
    }

    private static class MethodParam {
        public MethodParam(Annotation annotation, String annotationName, int argIndex) {
            this.annotation = annotation;
            this.annotationName = annotationName;
            this.argIndex = argIndex;
        }

        Annotation annotation;
        String annotationName;
        int argIndex;

    }
}
