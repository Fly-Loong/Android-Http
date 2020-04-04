package cn.com.easyadr.http;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

public class BodyPart {
    private String fileName;
    private String data;

    public void write(OutputStream out) throws IOException {
        out.write((Consts.PREFIX + Consts.BOUNDARY + Consts.NEW_LINE).getBytes());
        if (!TextUtils.isEmpty(data)) {
            out.write(data.getBytes());
        } else if (!TextUtils.isEmpty(fileName)) {
            File file = new File(fileName);
            InputStream inputStream = new FileInputStream(file);
            String filePrams = "file";
            String fileName = file.getName();
            String info = "Content-Disposition: form-data; " + "name=\"" + URLEncoder.encode(filePrams, "utf-8") + "\"" + "; filename=\"" + URLEncoder.encode(fileName, "utf-8") + "\"" + Consts.NEW_LINE;
            out.write(info.getBytes());
            out.write(Consts.NEW_LINE.getBytes());

            byte[] buffer = new byte[1024 * 1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        out.write(Consts.NEW_LINE.getBytes());
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
