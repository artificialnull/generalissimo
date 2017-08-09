package com.gabdeg.generalissimo;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Networker {

    static CookieManager cookieManager = new CookieManager();

    public Networker() {
        CookieHandler.setDefault(cookieManager);
    }

    public InputStream get(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            urlConnection.setRequestProperty(
                    "Cookie", TextUtils.join(
                            ";", cookieManager.getCookieStore().getCookies()
                    )
            );
            Log.v("GET", "There exist >0 cookies");
        }
        return urlConnection.getInputStream();
    }

    public String getAsString(String urlStr) throws IOException {
        InputStream inp = get(urlStr);
        Scanner s = new java.util.Scanner(inp).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String postAsString(String urlStr, String paramStr) throws IOException {
        InputStream inp = post(urlStr, paramStr);
        Scanner s = new java.util.Scanner(inp).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public InputStream post(String urlStr, String paramStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);

        OutputStream outp = urlConnection.getOutputStream();
        outp.write(URLEncoder.encode(paramStr, "UTF-8")
                .replace("%3D", "=")
                .replace("%26", "&")
                .getBytes(Charset.forName("UTF-8")));
        Log.v("PASSWORD_ENCODED", URLEncoder.encode(paramStr, "UTF-8"));

        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        List<String> cookieHeader = headerFields.get("Set-Cookie");

        if (cookieHeader != null) {
            for (String cookie : cookieHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                Log.v("POST", "Cookie: " + cookie);
            }
        }

        return urlConnection.getInputStream();
    }

    public List<HttpCookie> getCookies() {
        return cookieManager.getCookieStore().getCookies();
    }
}
