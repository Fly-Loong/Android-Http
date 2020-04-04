package cn.com.easyadr.http;

import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpCall<ResponseT> {
    private static final String TAG = "HttpCall";

    static JsonConvertor jsonConvertor;
    private HttpCallListenter<ResponseT> listenter;

    Interceptor interceptor;
    Class<ResponseT> responseBodyClass;
    String httpMethod;
    String url;
    Map<String, String> headers;
    String body;
    String downloadParams;
    List<BodyPart> multipartBodies;
    int connectionTimeout;
    int readTimeout;
    InputStream caRootPem;
    boolean checkHostName;

    public void run(HttpCallListenter<ResponseT> listenter1) {
        this.listenter = listenter1;
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        doWork();
                    } catch (Exception e) {
                        listenter.onHttpResult(-1, e.getMessage(), null);
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doWork() throws Exception {
        if (interceptor != null) {
            interceptor.preprocesseHeaders(url, headers);
        }
        sendRequest(httpMethod);
    }

    public void sendRequest(String method) throws Exception {
        HttpURLConnection conn = Connection.getConnection(new URL(url), caRootPem, checkHostName);
        conn.setRequestMethod(method);
        if (connectionTimeout > 0) {
            conn.setConnectTimeout(connectionTimeout);
        }

        if (readTimeout > 0) {
            conn.setReadTimeout(readTimeout);
        }

        if (headers != null && !headers.isEmpty()) {
            Set<Map.Entry<String, String>> entrys = headers.entrySet();
            for (Map.Entry<String, String> entry : entrys) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (!method.equals("GET")) {
            if (!TextUtils.isEmpty(body)) {
                OutputStream out = conn.getOutputStream();
                out.write(body.getBytes("UTF-8"));
                out.close();
            } else if (multipartBodies.size() > 0) {
                OutputStream out = conn.getOutputStream();
                for (BodyPart bodyPart : multipartBodies) {
                    bodyPart.write(out);
                }
                out.write((Consts.PREFIX + Consts.BOUNDARY + Consts.PREFIX + Consts.NEW_LINE).getBytes());
                out.close();
            }
        } else {
            if (!TextUtils.isEmpty(downloadParams)) {
                String[] params = downloadParams.split("\t");
                if (params[0].equals("1")) {
                    File file = new File(params[1]);
                    long size = file.length();
                    if (size > 0) {
                        conn.setRequestProperty("range", "bytes=" + size + "-");
                    }
                }
            }
        }
        handleResponse(conn);
    }

    private String readString(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return new String(outSteam.toByteArray(), "UTF-8");
    }

    private void handleResponse(HttpURLConnection conn) throws Exception {
        InputStream is = null;
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            is = conn.getInputStream();
        } else {
            is = conn.getErrorStream();
        }
        // to get correct error message, if we can get response from server, we should not throw exception,
        try {
            if (TextUtils.isEmpty(downloadParams)) {
                String responseBody = readString(is);
                if (responseCode == 200) {
                    listenter.onHttpResult(200, responseBody, jsonConvertor.json2Object(responseBody, responseBodyClass));
                } else {
                    listenter.onHttpResult(responseCode, responseBody, null);
                }
            } else {
                if (responseCode == 200) {
                    try {
                        String[] params = downloadParams.split("\t");
                        if (params[0].equals("0")) {
                            File file = new File(params[1]);
                            file.delete();
                        }

                        RandomAccessFile randomAccessFile = new RandomAccessFile(new File(params[1]), "rw");
                        randomAccessFile.seek(randomAccessFile.length());
                        byte[] buffer = new byte[1024 * 1024];
                        int len = 0;
                        while ((len = is.read(buffer)) != -1) {
                            randomAccessFile.write(buffer, 0, len);
                        }
                        randomAccessFile.close();
                    } catch (Exception e) {
                        listenter.onHttpResult(-1, e.getMessage(), null);
                        e.printStackTrace();
                    }
                    listenter.onHttpResult(200, null, null);
                } else {
                    listenter.onHttpResult(responseCode, null, null);
                }
            }
            is.close();
            conn.disconnect();
        } catch (Exception e) {
            listenter.onHttpResult(-1, e.getMessage(), null);
            ;
        }
    }
}
