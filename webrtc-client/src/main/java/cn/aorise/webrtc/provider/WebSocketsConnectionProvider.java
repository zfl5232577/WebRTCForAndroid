package cn.aorise.webrtc.provider;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

/**
 * Created by 54926 on 2017/10/20.
 */
public class WebSocketsConnectionProvider implements ConnectionProvider {

    private static final String TAG = WebSocketsConnectionProvider.class.getSimpleName();

    private final String mUri;
    private final Map<String, String> mConnectHttpHeaders;

    private final List<FlowableEmitter<? super LifecycleEvent>> mLifecycleEmitters;
    private final List<FlowableEmitter<? super String>> mMessagesEmitters;

    private WebSocketClient mWebSocketClient;
    private boolean haveConnection;
    private TreeMap<String, String> mServerHandshakeHeaders;
    private String cookie;

    private final Object mLifecycleLock = new Object();

    /**
     * Support UIR scheme ws://host:port/path
     *
     * @param connectHttpHeaders may be null
     */
    public WebSocketsConnectionProvider(String uri, Map<String, String> connectHttpHeaders, String cookie) {
        mUri = uri;
        mConnectHttpHeaders = connectHttpHeaders != null ? connectHttpHeaders : new HashMap<>();
        mLifecycleEmitters = new CopyOnWriteArrayList<>();
        mMessagesEmitters = new ArrayList<>();
        this.cookie = cookie;
    }

    @Override
    public Flowable<String> messages() {
        Flowable<String> flowable = Flowable.<String>create(mMessagesEmitters::add, BackpressureStrategy.BUFFER)
                .doOnCancel(() -> {
                    Iterator<FlowableEmitter<? super String>> iterator = mMessagesEmitters.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().isCancelled()) iterator.remove();
                    }

                    if (mMessagesEmitters.size() < 1) {
                        mWebSocketClient.close();
                    }
                });
        return flowable;
    }

    private void createWebSocketConnection() {
        if (haveConnection) {
            throw new IllegalStateException("Already have connection to web socket");
        }
        mConnectHttpHeaders.put("cookie", cookie);
        mWebSocketClient = new WebSocketClient(URI.create(mUri), new Draft_6455(), mConnectHttpHeaders, 60000) {
            @Override
            public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
                mServerHandshakeHeaders = new TreeMap<>();
                Iterator<String> keys = response.iterateHttpFields();
                while (keys.hasNext()) {
                    String key = keys.next();
                    mServerHandshakeHeaders.put(key, response.getFieldValue(key));
                }
            }

            @Override
            public void onOpen(ServerHandshake handshakeData) {
                LifecycleEvent openEvent = new LifecycleEvent(LifecycleEvent.Type.OPENED);
                openEvent.setHandshakeResponseHeaders(mServerHandshakeHeaders);
                emitLifecycleEvent(openEvent);
            }

            @Override
            public void onMessage(String message) {
                emitMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                haveConnection = false;
                mWebSocketClient=null;
                emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.CLOSED));
            }

            @Override
            public void onError(Exception exception) {
                haveConnection = false;
                mWebSocketClient=null;
                emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.ERROR, exception));
            }
        };

        if (mUri.startsWith("wss")) {
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
                mWebSocketClient.setSocket(factory.createSocket());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mWebSocketClient.connect();
        haveConnection = true;
    }

    @Override
    public Flowable<Void> send(String stompMessage) {
        return Flowable.create(emitter -> {
            if (mWebSocketClient == null) {
                emitter.onError(new IllegalStateException("Not connected yet"));
            } else {
                mWebSocketClient.send(stompMessage);
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    private void emitLifecycleEvent(LifecycleEvent lifecycleEvent) {
        synchronized (mLifecycleLock) {
            for (FlowableEmitter<? super LifecycleEvent> emitter : mLifecycleEmitters) {
                emitter.onNext(lifecycleEvent);
            }
        }
    }

    private void emitMessage(String stompMessage) {
        for (FlowableEmitter<? super String> emitter : mMessagesEmitters) {
            emitter.onNext(stompMessage);
        }
    }

    @Override
    public Flowable<LifecycleEvent> getLifecycleReceiver() {
        return Flowable.<LifecycleEvent>create(lifecycleEvent -> {
            synchronized (mLifecycleLock) {
                mLifecycleEmitters.add(lifecycleEvent);
            }
        }, BackpressureStrategy.BUFFER)
                .doOnCancel(() -> {
                    synchronized (mLifecycleLock) {
                        Iterator<FlowableEmitter<? super LifecycleEvent>> iterator = mLifecycleEmitters.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().isCancelled()) iterator.remove();
                        }
                    }
                });
    }

    @Override
    public void close() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
        }
    }

    @Override
    public void connect() {
        if (mWebSocketClient == null) {
            createWebSocketConnection();
        }
    }

    @Override
    public void reconnect() {
        if (mWebSocketClient == null) {
            createWebSocketConnection();
        }
    }
}
