package cn.com.easyadr.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import cn.com.easyadr.http.ssl.SSLContextFactory;

public abstract class Connection {
    public final static HostnameVerifier HOST_VERIFIER_CHECK = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return hostname.equals(session.getPeerHost());
        }
    };

    public final static HostnameVerifier HOST_VERIFIER_UNCHECK = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static HttpURLConnection getConnection(URL url) throws IOException {
        return getConnection(url, null, false);
    }

    public static HttpURLConnection getConnection(URL url, InputStream caRootPem, boolean checkHostName) throws IOException {
        HttpURLConnection conn;
        if (url.getProtocol().toLowerCase().startsWith("https") && caRootPem != null) {
            HttpsURLConnection secureConn = (HttpsURLConnection) url.openConnection();
            SSLContext sslContext = SSLContextFactory.getInstance().makeContext(null, null, caRootPem);
            secureConn.setSSLSocketFactory(sslContext.getSocketFactory());
            if (checkHostName) {
                secureConn.setHostnameVerifier(HOST_VERIFIER_CHECK);
            } else {
                secureConn.setHostnameVerifier(HOST_VERIFIER_UNCHECK);
            }
            return secureConn;
        } else {
            conn = (HttpURLConnection) url.openConnection();
            return conn;
        }
    }
}
