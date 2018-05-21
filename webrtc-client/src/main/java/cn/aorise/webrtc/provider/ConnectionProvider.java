package cn.aorise.webrtc.provider;

import io.reactivex.Flowable;

public interface ConnectionProvider {

    /**
     * Subscribe this for receive stomp messages
     */
    Flowable<String> messages();

    /**
     * Sending stomp messages via you ConnectionProvider.
     * onError if not connected or error detected will be called, or onCompleted id sending started
     * TODO: send messages with ACK
     */
    Flowable<Void> send(String stompMessage);

    /**
     * Subscribe this for receive #LifecycleEvent events
     */
    Flowable<LifecycleEvent> getLifecycleReceiver();

    void close();

    void connect();

    void reconnect();
}
