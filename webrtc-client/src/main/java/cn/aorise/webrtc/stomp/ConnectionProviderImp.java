package cn.aorise.webrtc.stomp;

import io.reactivex.Completable;
import io.reactivex.Observable;


public interface ConnectionProviderImp {

    /**
     * Subscribe this for receive stomp messages
     */
    Observable<String> messages();

    /**
     * Sending stomp messages via you ConnectionProviderImp.
     * onError if not connected or error detected will be called, or onCompleted id sending started
     * TODO: send messages with ACK
     */
    Completable send(String stompMessage);

    /**
     * Subscribe this for receive #LifecycleEvent events
     */
    Observable<LifecycleEvent> lifecycle();

    /**
     * Disconnects from server. This is basically a Callable.
     * Automatically emits Lifecycle.CLOSE
     */
    Completable disconnect();

    Completable setHeartbeat(int ms);
}
