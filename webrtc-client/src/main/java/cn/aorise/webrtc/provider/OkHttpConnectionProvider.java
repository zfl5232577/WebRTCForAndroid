package cn.aorise.webrtc.provider;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;

public class OkHttpConnectionProvider implements ConnectionProvider {

    private static final String TAG = OkHttpConnectionProvider.class.getSimpleName();

    private final String mUri;
    private final Map<String, String> mConnectHttpHeaders;
    private final OkHttpClient mOkHttpClient;

    private final List<FlowableEmitter<? super LifecycleEvent>> mLifecycleEmitters;
    private final List<FlowableEmitter<? super String>> mMessagesEmitters;

    private WebSocket webSocket;

    private final Object mLifecycleLock = new Object();
    private boolean haveConnection = false;


    public OkHttpConnectionProvider(String uri, Map<String, String> connectHttpHeaders, OkHttpClient okHttpClient) {
        mUri = uri;
        mConnectHttpHeaders = connectHttpHeaders != null ? connectHttpHeaders : new HashMap<>();
        mLifecycleEmitters = new CopyOnWriteArrayList<>();
        mMessagesEmitters = new ArrayList<>();
        mOkHttpClient = okHttpClient;
    }

    @Override
    public Flowable<String> messages() {
        Flowable<String> flowable = Flowable.<String>create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> e) throws Exception {
                mMessagesEmitters.add(e);
                Log.e(TAG, "mMessagesEmitters: mMessagesEmitters.size()" + "================" + mMessagesEmitters.size());
            }
        }, BackpressureStrategy.BUFFER)
                .doOnCancel(() -> {
                    Iterator<FlowableEmitter<? super String>> iterator = mMessagesEmitters.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().isCancelled()) {
                            iterator.remove();
                        }
                    }
                    if (mMessagesEmitters.size() < 1) {
                        webSocket.close(1000, "");
                        webSocket = null;
                    }
                });
        return flowable;
    }

    private void createWebSocketConnection() {
        Request.Builder requestBuilder = new Request.Builder().url(mUri);
        addConnectionHeadersToBuilder(requestBuilder, mConnectHttpHeaders);
        webSocket = mOkHttpClient.newWebSocket(requestBuilder.build(),
                new WebSocketListener() {
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        haveConnection = true;
                        LifecycleEvent openEvent = new LifecycleEvent(LifecycleEvent.Type.OPENED);
                        TreeMap<String, String> headersAsMap = headersAsMap(response);
                        openEvent.setHandshakeResponseHeaders(headersAsMap);
                        emitLifecycleEvent(openEvent);
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        emitMessage(text);
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, ByteString bytes) {
                        emitMessage(bytes.utf8());
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        haveConnection = false;
                        webSocket.close(1000, null);
                        emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.CLOSED));

                    }

                    @Override
                    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                        haveConnection = false;
                        webSocket.close(1000, null);
                        emitLifecycleEvent(new LifecycleEvent(LifecycleEvent.Type.ERROR, new Exception(t)));
                    }

                    @Override
                    public void onClosing(final WebSocket webSocket, final int code, final String reason) {
                        webSocket.close(1000, null);
                        webSocket.cancel();
                    }
                }
        );

    }

    @Override
    public void connect() {
        if (!haveConnection) {
            createWebSocketConnection();
        }else {
            ((RealWebSocket) webSocket).connect(mOkHttpClient);
        }
    }

    @Override
    public void reconnect() {
        if (!haveConnection) {
            createWebSocketConnection();
        }else {
            ((RealWebSocket) webSocket).connect(mOkHttpClient);
        }
    }

    @Override
    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "active close");
            webSocket.cancel();
        }
    }

    @Override
    public Flowable<Void> send(String stompMessage) {
        return Flowable.create(subscriber -> {
            if (webSocket == null) {
                subscriber.onError(new IllegalStateException("Not connected yet"));
            } else {
                webSocket.send(stompMessage);
                subscriber.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<LifecycleEvent> getLifecycleReceiver() {
        return Flowable.<LifecycleEvent>create(lifecycleEvent -> {
            synchronized (mLifecycleLock) {
                mLifecycleEmitters.add(lifecycleEvent);
                Log.e(TAG, "getLifecycleReceiver: mLifecycleEmitters.size()" + "================" + mLifecycleEmitters.size());
            }
        }, BackpressureStrategy.BUFFER)
                .doOnCancel(() -> {
                    synchronized (mLifecycleLock) {
                        Iterator<FlowableEmitter<? super LifecycleEvent>> iterator = mLifecycleEmitters.iterator();
                        while (iterator.hasNext()) {
                            if (iterator.next().isCancelled()) {
                                iterator.remove();
                            }
                        }
                    }
                });
    }

    private TreeMap<String, String> headersAsMap(Response response) {
        TreeMap<String, String> headersAsMap = new TreeMap<>();
        Headers headers = response.headers();
        for (String key : headers.names()) {
            headersAsMap.put(key, headers.get(key));
        }
        return headersAsMap;
    }

    private void addConnectionHeadersToBuilder(Request.Builder requestBuilder, Map<String, String> mConnectHttpHeaders) {
        for (Map.Entry<String, String> headerEntry : mConnectHttpHeaders.entrySet()) {
            requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    private void emitLifecycleEvent(LifecycleEvent lifecycleEvent) {
        synchronized (mLifecycleLock) {
            for (FlowableEmitter<? super LifecycleEvent> subscriber : mLifecycleEmitters) {
                subscriber.onNext(lifecycleEvent);
            }
        }
    }

    private void emitMessage(String stompMessage) {
        for (FlowableEmitter<? super String> subscriber : mMessagesEmitters) {
            subscriber.onNext(stompMessage);
        }
    }
}
