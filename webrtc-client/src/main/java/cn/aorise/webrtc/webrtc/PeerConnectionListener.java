package cn.aorise.webrtc.webrtc;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

/**
 * Created by 54926 on 2017/8/30.
 */

public interface PeerConnectionListener {

    void onCreateSuccess(SessionDescription sessionDescription);

    void onSetSuccess();

    void onCreateFailure(String s);

    void onSetFailure(String s);

    void onSignalingChange(PeerConnection.SignalingState signalingState);

    void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState);

    void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState);

    void onIceCandidate(IceCandidate iceCandidate);

    void onAddStream(MediaStream mediaStream);

    void onRemoveStream(MediaStream mediaStream);

    void onDataChannel(DataChannel dataChannel);

    void onRenegotiationNeeded();

}
