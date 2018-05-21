package cn.aorise.webrtc.signal;

import cn.aorise.webrtc.provider.LifecycleEvent;
import cn.aorise.webrtc.stomp.StompMessage;

/**
 * Created by 54926 on 2017/8/30.
 */
public interface SignalCallBack {
    void onOpen(LifecycleEvent lifecycleEvent);

    void onMessage(StompMessage stompMessage);

    void onPingMessage();

    void onConnected();

    void onClosed(LifecycleEvent lifecycleEvent);

    void onError(LifecycleEvent lifecycleEvent);
}
