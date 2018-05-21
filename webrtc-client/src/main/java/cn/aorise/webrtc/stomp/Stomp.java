package cn.aorise.webrtc.stomp;

import android.text.TextUtils;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import cn.aorise.webrtc.provider.ConnectionProvider;
import cn.aorise.webrtc.provider.OkHttpConnectionProvider;
import cn.aorise.webrtc.provider.WebSocketsConnectionProvider;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Supported overlays:
 * - org.java_websocket.WebSocket ('org.java-websocket:Java-WebSocket:1.3.0')
 * - okhttp3.WebSocket ('com.squareup.okhttp3:okhttp:3.8.0')
 * <p>
 * You can add own relay, just implement ConnectionProvider for you stomp transport,
 * such as web socket.
 * <p>
 */
public class Stomp {

    public static StompClient over(Class clazz, String uri, String cookie) {
        return over(clazz, uri, null, null, cookie);
    }

    /**
     * @param clazz              class for using as transport
     * @param uri                URI to connect
     * @param connectHttpHeaders HTTP headers, will be passed with handshake query, may be null
     * @return StompClient for receiving and sending messages. Call #StompClient.connect
     */
    public static StompClient over(Class clazz, String uri, Map<String, String> connectHttpHeaders, String cookie) {
        return over(clazz, uri, connectHttpHeaders, null, cookie);
    }

    /**
     * {@code webSocketClient} can accept the following type of clients:
     * <ul>
     * <li>{@code org.java_websocket.WebSocket}: cannot accept an existing client</li>
     * <li>{@code okhttp3.WebSocket}: can accept a non-null instance of {@code okhttp3.OkHttpClient}</li>
     * </ul>
     *
     * @param clazz              class for using as transport
     * @param uri                URI to connect
     * @param connectHttpHeaders HTTP headers, will be passed with handshake query, may be null
     * @param webSocketClient    Existing client that will be used to open the WebSocket connection, may be null to use default client
     * @return StompClient for receiving and sending messages. Call #StompClient.connect
     */
    public static StompClient over(Class clazz, String uri, Map<String, String> connectHttpHeaders, Object webSocketClient, String cookie) {
        try {
            if (Class.forName("org.java_websocket.WebSocket") != null && clazz == WebSocket.class) {

                if (webSocketClient != null) {
                    throw new IllegalArgumentException("You cannot pass a webSocketClient with 'org.java_websocket.WebSocket'. use null instead.");
                }

                return createStompClient(new WebSocketsConnectionProvider(uri, connectHttpHeaders, cookie));
            }
        } catch (ClassNotFoundException e) {
        }

        try {
            if (Class.forName("okhttp3.WebSocket") != null && clazz == okhttp3.WebSocket.class) {
                OkHttpClient okHttpClient = getOkHttpClient(webSocketClient, cookie);

                return createStompClient(new OkHttpConnectionProvider(uri, connectHttpHeaders, okHttpClient));
            }
        } catch (ClassNotFoundException e) {
        }


        throw new RuntimeException("Not supported overlay transport: " + clazz.getName());
    }

    private static StompClient createStompClient(ConnectionProvider connectionProvider) {
        return new StompClient(connectionProvider);
    }

    private static OkHttpClient getOkHttpClient(Object webSocketClient, String cookie) {
        if (webSocketClient != null) {
            if (webSocketClient instanceof OkHttpClient) {
                return (OkHttpClient) webSocketClient;
            } else {
                throw new IllegalArgumentException("You must pass a non-null instance of an 'okhttp3.OkHttpClient'. Or pass null to use a default websocket client.");
            }
        } else {
            X509TrustManager manager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            TrustManager[] trustAllCerts = new TrustManager[]{manager};
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLSocketFactory factory = sc.getSocketFactory();

                Interceptor mSessionInterceptor = new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request.Builder builder = originalRequest.newBuilder();
                        if (!TextUtils.isEmpty(cookie)) {
                            builder.header("cookie", cookie);
                        }
                        Request authorised = builder.build();
                        return chain.proceed(authorised);
                    }
                };
                // default http client
                return new OkHttpClient.Builder()
                        .readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .pingInterval(25, TimeUnit.SECONDS)
                        .sslSocketFactory(factory, manager)
                        .addInterceptor(mSessionInterceptor)
                        .hostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        })
                        .build();
            } catch (Exception e) {

            }

        }
        return null;
    }
}
