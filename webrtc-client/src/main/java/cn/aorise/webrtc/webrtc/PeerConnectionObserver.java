package cn.aorise.webrtc.webrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Created by 54926 on 2017/8/30.
 */

public class PeerConnectionObserver implements SdpObserver, PeerConnection.Observer {
    private PeerConnectionListener mPeerConnectionListener;

    public PeerConnectionObserver(PeerConnectionListener peerConnectionListener) {
        mPeerConnectionListener = peerConnectionListener;
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        mPeerConnectionListener.onCreateSuccess(sessionDescription);
    }

    @Override
    public void onSetSuccess() {
        mPeerConnectionListener.onSetSuccess();
    }

    @Override
    public void onCreateFailure(String s) {
        mPeerConnectionListener.onCreateFailure(s);
    }

    @Override
    public void onSetFailure(String s) {
        mPeerConnectionListener.onSetFailure(s);
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        mPeerConnectionListener.onSignalingChange(signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        mPeerConnectionListener.onIceConnectionChange(iceConnectionState);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
       mPeerConnectionListener.onIceGatheringChange(iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
       mPeerConnectionListener.onIceCandidate(iceCandidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        Log.e("BaseCallActivity", "onIceCandidatesRemoved: " );
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
       mPeerConnectionListener.onAddStream(mediaStream);
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        mPeerConnectionListener.onRemoveStream(mediaStream);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        mPeerConnectionListener.onDataChannel(dataChannel);
    }

    @Override
    public void onRenegotiationNeeded() {
        mPeerConnectionListener.onRenegotiationNeeded();
    }

    @Override
    public void onAddTrack(RtpReceiver receiver, MediaStream[] mediaStreams) {

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {

    }
}
