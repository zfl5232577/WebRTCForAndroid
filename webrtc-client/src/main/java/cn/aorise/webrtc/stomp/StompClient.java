package cn.aorise.webrtc.stomp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.aorise.webrtc.api.API;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.common.Mlog;
import cn.aorise.webrtc.provider.ConnectionProvider;
import cn.aorise.webrtc.provider.LifecycleEvent;
import cn.aorise.webrtc.signal.SignalCallBack;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.WebSocket;

public class StompClient {

    private static final String TAG = StompClient.class.getSimpleName();

    public static final String SUPPORTED_VERSIONS = "1.1,1.0";
    public static final String DEFAULT_ACK = "auto";

    private static StompClient instance;
    //private static String url = "wss://sphj.zhihuixupu.com:443/ws/signaling";

    private Disposable mMessagesDisposable;
    private Disposable mLifecycleDisposable;
    private Disposable mtTopicMessageDisposable;
    private Disposable mtTopicErrorDisposable;
    private Disposable mtTopicPingDisposable;
    private Map<String, Set<FlowableEmitter<? super StompMessage>>> mEmitters = new ConcurrentHashMap<>();
    private List<ConnectableFlowable<Void>> mWaitConnectionFlowables;
    private ConnectionProvider mConnectionProvider;
    private HashMap<String, String> mTopics;
    private SignalCallBack signalCallBack;
    private boolean mConnected;
    private boolean isConnecting;

    public static StompClient getInstance() {
        synchronized (StompClient.class) {
            if (instance == null) {
                HashMap<String,String> connectHttpHeaders = new HashMap<>();
                connectHttpHeaders.put("token",API.TOKEN);
                connectHttpHeaders.put("username", Constant.LoginInfo.user.getUserName());
                connectHttpHeaders.put("device-type", Constant.DeviceType.MOBILE);
                try {
                    connectHttpHeaders.put("name", URLEncoder.encode(Constant.LoginInfo.user.getNickName(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                instance = Stomp.over(WebSocket.class, API.SIGNAL_URL,connectHttpHeaders,"");
            }
        }
        return instance;
    }

    public StompClient(ConnectionProvider connectionProvider) {
        mConnectionProvider = connectionProvider;
        mWaitConnectionFlowables = new CopyOnWriteArrayList<>();
        mtTopicMessageDisposable = topic("/user/topic/message")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onMessage(topicMessage);
                    }
                });

        mtTopicErrorDisposable = topic("/user/topic/error")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onMessage(topicMessage);
                    }
                });

        mtTopicPingDisposable = topic("/user/topic/ping")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    if (signalCallBack != null) {
                        signalCallBack.onPingMessage();
                    }
                });
        mLifecycleDisposable = mConnectionProvider.getLifecycleReceiver()
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            connectOpen();
                            if (signalCallBack != null) {
                                signalCallBack.onOpen(lifecycleEvent);
                            }
                            break;
                        case CLOSED:
                            mConnected = false;
                            isConnecting = false;
                            if (signalCallBack != null) {
                                signalCallBack.onClosed(lifecycleEvent);
                            }
                            break;
                        case ERROR:
                            mConnected = false;
                            isConnecting = false;
                            if (signalCallBack != null) {
                                signalCallBack.onError(lifecycleEvent);
                            }
                            break;
                    }
                });
        mMessagesDisposable = mConnectionProvider.messages()
                .map(StompMessage::from)
                .subscribe(stompMessage -> {
                    if (stompMessage.getStompCommand().equals(StompCommand.CONNECTED)) {
                        connected();
                        if (signalCallBack != null) {
                            signalCallBack.onConnected();
                        }
                    }
                    callSubscribers(stompMessage);
                });
    }

    /**
     * If already connected and reconnect=false - nope
     */
    public void connect(SignalCallBack signalCallBack) {
        Mlog.e(TAG, "connect: " );
        if (mConnected || isConnecting) {
            return;
        }
        this.signalCallBack = signalCallBack;
        isConnecting = true;
        mConnectionProvider.connect();
    }

    public void reconnect(){
        Mlog.e(TAG, "reconnect: " );
        if (mConnected || isConnecting) {
            return;
        }
        isConnecting = true;
        mConnectionProvider.reconnect();
    }

    private void connected() {
        mConnected = true;
        isConnecting = false;
        for (ConnectableFlowable<Void> flowable : mWaitConnectionFlowables) {
            flowable.connect();
        }
        mWaitConnectionFlowables.clear();
    }

    private void connectOpen() {
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader(StompHeader.VERSION, SUPPORTED_VERSIONS));
        mConnectionProvider.send(new StompMessage(StompCommand.CONNECT, headers, null).compile())
                .subscribe();
    }

    public Flowable<Void> send(String destination) {
        return send(new StompMessage(
                StompCommand.SEND,
                Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
                null));
    }

    public Flowable<Void> send(String destination, String data) {
        return send(new StompMessage(
                StompCommand.SEND,
                Collections.singletonList(new StompHeader(StompHeader.DESTINATION, destination)),
                data));
    }

    public Flowable<Void> send(StompMessage stompMessage) {
        Flowable<Void> flowable = mConnectionProvider.send(stompMessage.compile());
        if (!mConnected) {
            ConnectableFlowable<Void> deffered = flowable.publish();
            mWaitConnectionFlowables.add(deffered);
            return deffered;
        } else {
            return flowable;
        }
    }

    private void callSubscribers(StompMessage stompMessage) {
        String messageDestination = stompMessage.findHeader(StompHeader.DESTINATION);
        for (String dest : mEmitters.keySet()) {
            if (dest.equals(messageDestination)) {
                for (FlowableEmitter<? super StompMessage> subscriber : mEmitters.get(dest)) {
                    subscriber.onNext(stompMessage);
                }
                return;
            }
        }
    }

    public Flowable<LifecycleEvent> lifecycle() {
        return mConnectionProvider.getLifecycleReceiver();
    }

    public void disconnect() {
        if (mtTopicMessageDisposable!=null){
            mtTopicMessageDisposable.dispose();
            mtTopicMessageDisposable =null;
        }
        if (mtTopicErrorDisposable!=null){
            mtTopicErrorDisposable.dispose();
            mtTopicErrorDisposable =null;
        }
        if (mtTopicPingDisposable!=null){
            mtTopicPingDisposable.dispose();
            mtTopicPingDisposable =null;
        }
        if (mMessagesDisposable != null) {
            mMessagesDisposable.dispose();
            mMessagesDisposable = null;
        }
        if (mLifecycleDisposable != null) {
            mLifecycleDisposable.dispose();
            mLifecycleDisposable = null;
        }
        if (mConnectionProvider != null) {
            mConnectionProvider.close();
            mConnectionProvider = null;
        }
        mConnected = false;
        isConnecting = false;
        instance = null;
    }

    public Flowable<StompMessage> topic(String destinationPath) {
        return topic(destinationPath, null);
    }

    public Flowable<StompMessage> topic(String destinationPath, List<StompHeader> headerList) {
        return Flowable.<StompMessage>create(emitter -> {
            Set<FlowableEmitter<? super StompMessage>> emittersSet = mEmitters.get(destinationPath);
            if (emittersSet == null) {
                emittersSet = new HashSet<>();
                mEmitters.put(destinationPath, emittersSet);
                subscribePath(destinationPath, headerList).subscribe();
            }
            emittersSet.add(emitter);
        }, BackpressureStrategy.BUFFER)
                .doOnCancel(() -> {
                    Iterator<String> mapIterator = mEmitters.keySet().iterator();
                    while (mapIterator.hasNext()) {
                        String destinationUrl = mapIterator.next();
                        Set<FlowableEmitter<? super StompMessage>> set = mEmitters.get(destinationUrl);
                        Iterator<FlowableEmitter<? super StompMessage>> setIterator = set.iterator();
                        while (setIterator.hasNext()) {
                            FlowableEmitter<? super StompMessage> subscriber = setIterator.next();
                            if (subscriber.isCancelled()) {
                                setIterator.remove();
                                if (set.size() < 1) {
                                    mapIterator.remove();
                                    unsubscribePath(destinationUrl).subscribe();
                                }
                            }
                        }
                    }
                });
    }

    private Flowable<Void> subscribePath(String destinationPath, List<StompHeader> headerList) {
        if (destinationPath == null) return Flowable.empty();
        String topicId = UUID.randomUUID().toString();

        if (mTopics == null) {
            mTopics = new HashMap<>();
        }
        mTopics.put(destinationPath, topicId);
        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader(StompHeader.ID, topicId));
        headers.add(new StompHeader(StompHeader.DESTINATION, destinationPath));
        headers.add(new StompHeader(StompHeader.ACK, DEFAULT_ACK));
        if (headerList != null) headers.addAll(headerList);
        return send(new StompMessage(StompCommand.SUBSCRIBE,
                headers, null));
    }


    private Flowable<Void> unsubscribePath(String dest) {
        String topicId = mTopics.get(dest);
        return send(new StompMessage(StompCommand.UNSUBSCRIBE,
                Collections.singletonList(new StompHeader(StompHeader.ID, topicId)), null));
    }

    public boolean isConnected() {
        return mConnected;
    }
}
