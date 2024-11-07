package org.example.network;

import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NetworkEngine {

    private static final Logger log = LoggerFactory.getLogger(NetworkEngine.class);

    private static final String HOST_FILEZ = "filez.com";
    private final OkHttpClient client;
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    ThreadLocal<String> threadLocal = new ThreadLocal();
    private static String sessionId = "";
    public String getSessionId() {
        String session = threadLocal.get();
        if(TextUtils.isEmpty(session)) {
//            return "3789afaf0c504df29e4f2be822e7bc01_1_1_app1";
        }
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        log.info("session更新 sessionId：{}",sessionId);
        threadLocal.set(sessionId);
        this.sessionId = sessionId;
    }

    private static class Holder {
        private static final NetworkEngine sInstance = new NetworkEngine();
    }

    private NetworkEngine(){
        client = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                        String host = httpUrl.host();
                        if (host.endsWith(HOST_FILEZ)) {
                            host = HOST_FILEZ;
                        }
                        cookieStore.put(host, list);//保存一级域名的cookie，保证上传等二级三级域名能共用。
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                        String host = httpUrl.host();
                        if (httpUrl.host().endsWith(HOST_FILEZ)) {
                            host = HOST_FILEZ;
                        }

                        List<Cookie> newCookies = new ArrayList<>();
                        Cookie newCookie = new Cookie.Builder().domain(host).name("X-LENOVO-SESS-ID").value(getSessionId()).build();
                        newCookies.add(newCookie);
                        log.info("当前session: {} size:{}",newCookies.toArray(),newCookies.size());
                        return newCookies != null ? newCookies : new ArrayList<Cookie>();
                    }
                })
                .hostnameVerifier(new SSLUtil.AnyHostnameVerifier())
                .sslSocketFactory(SSLUtil.getSSLSocketFactory(), new SSLUtil.AnyTrustManager())
                .connectTimeout(60, TimeUnit.SECONDS)  // 设置连接超时
                .readTimeout(60, TimeUnit.SECONDS)     // 设置读取超时
                .writeTimeout(60, TimeUnit.SECONDS)    // 设置写入超时
                .build();
    }

    private boolean isText(MediaType mediaType) {
        if(mediaType == null || mediaType ==  MediaType.parse("application/octet-stream")) {
            return false;
        }
        return true;
    }

    public static NetworkEngine getInstance() {
        return Holder.sInstance;
    }

    public OkHttpClient getHttpClient(){
        return client;
    }

    private String getResponseText(Response response) {

        String str = "Empty!";

        try {
            ResponseBody body = response.body();
            if (body != null && body.contentLength() != 0) {
                BufferedSource source = body.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    @SuppressWarnings("CharsetObjectCanBeUsed") Charset charset = mediaType.charset(
                            Charset.forName("UTF-8"));
                    if (charset != null) {
                        str = buffer.clone().readString(charset);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

}
