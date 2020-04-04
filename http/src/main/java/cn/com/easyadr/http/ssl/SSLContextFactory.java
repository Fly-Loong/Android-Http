package cn.com.easyadr.http.ssl;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLContextFactory {

    private static SSLContextFactory theInstance = null;

    private SSLContextFactory() {
    }

    public static SSLContextFactory getInstance() {
        if (theInstance == null) {
            theInstance = new SSLContextFactory();
        }
        return theInstance;
    }

    public SSLContext makeContext(File clientCertFile, String clientCertPassword, InputStream caCertStream) throws IOException {
        try {
            final KeyStore trustStore = loadPEMTrustStore(caCertStream);
            TrustManager[] trustManagers = {new CustomTrustManager(trustStore)};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
            return sslContext;
        } catch (Exception e) {
            throw new IOException("generate SSLContext error");
        }
    }

    private KeyStore loadPEMTrustStore(InputStream caCertStream) throws Exception {
        byte[] der = loadPemCertificate(caCertStream);
        ByteArrayInputStream derInputStream = new ByteArrayInputStream(der);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
        String alias = cert.getSubjectX500Principal().getName();

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null);
        trustStore.setCertificateEntry(alias, cert);
        return trustStore;
    }

    private KeyStore loadPKCS12KeyStore(File certificateFile, String clientCertPassword) throws Exception {
        KeyStore keyStore = null;
        FileInputStream fis = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            fis = new FileInputStream(certificateFile);
            keyStore.load(fis, clientCertPassword.toCharArray());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                // ignore
            }
        }
        return keyStore;
    }

    byte[] loadPemCertificate(InputStream certificateStream) throws IOException {

        byte[] der = null;
        BufferedReader br = null;

        try {
            StringBuilder buf = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(certificateStream));

            String line = br.readLine();
            while (line != null) {
                if (!line.startsWith("--")) {
                    buf.append(line);
                }
                line = br.readLine();
            }

            String pem = buf.toString();
            der = Base64.decode(pem, Base64.DEFAULT);

        } finally {
            if (br != null) {
                br.close();
            }
        }
        return der;
    }
}
