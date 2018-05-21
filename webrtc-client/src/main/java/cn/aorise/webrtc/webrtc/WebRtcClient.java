package cn.aorise.webrtc.webrtc;

import android.content.Context;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.LinkedList;

import cn.aorise.webrtc.api.API;
import cn.aorise.webrtc.api.Constant;
import cn.aorise.webrtc.stomp.StompClient;

/**
 * Created by 54926 on 2017/8/30.
 */

public class WebRtcClient {
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private PeerConnectionFactory mPeerConnectionFactory;
    private PeerConnectionParameters mParameters;
    private PeerConnectionObserver mPeerConnectionObserver;
    private MediaConstraints mConstraints = new MediaConstraints();
    private MediaConstraints mSdpConstraints = new MediaConstraints();
    private PeerConnection mPeerConnection;
    private VideoSource mVideoSource;
    private boolean videoSourceStopped = false;
    private LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private LinkedList<IceCandidate> queuedLocalCandidates;
    private PeerConnectionFactory.Options options;

    private static final String FIELD_TRIAL_AUTOMATIC_RESIZE =
            "WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/";


    public WebRtcClient(Context context, EglBase.Context renderEGLContext, PeerConnectionParameters peerConnectionParameters, PeerConnectionListener peerConnectionListener) {
        mPeerConnectionObserver = new PeerConnectionObserver(peerConnectionListener);
        mParameters = peerConnectionParameters;
        //PeerConnectionFactory.initializeInternalTracer();
        // Initialize field trials
        // PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_AUTOMATIC_RESIZE);
        PeerConnectionFactory.initializeAndroidGlobals(context, true, true,
                peerConnectionParameters.videoCodecHwAcceleration);
        options = new PeerConnectionFactory.Options();
        mPeerConnectionFactory = new PeerConnectionFactory(options);
        mConstraints.optional.add(
                new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));

        mSdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", "true"));
        mSdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        createPeerConnection(renderEGLContext);
        queuedRemoteCandidates = new LinkedList<>();
        queuedLocalCandidates = new LinkedList<>();
    }

    public void addRemoteIceCandidate(IceCandidate candidate) {
        if (mPeerConnection != null) {
            if (queuedRemoteCandidates != null) {
                queuedRemoteCandidates.add(candidate);
            } else {
                mPeerConnection.addIceCandidate(candidate);
            }
        }
    }

    public void addLocalIceCandidate(IceCandidate iceCandidate) {
        if (queuedLocalCandidates != null) {
            queuedLocalCandidates.add(iceCandidate);
        }
    }

    public LinkedList<IceCandidate> getQueuedLocalCandidates() {
        return queuedLocalCandidates;
    }

    public void createPeerConnection(EglBase.Context renderEGLContext) {
        mPeerConnectionFactory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
        iceServers.add(new PeerConnection.IceServer(API.STUN_URL));
        iceServers.add(new PeerConnection.IceServer(API.TURN_URL, API.TURN_ACCOUNT, API.TURN_PASSWORD));
        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        mPeerConnection = mPeerConnectionFactory.createPeerConnection(
                rtcConfig, mConstraints, mPeerConnectionObserver);
    }

    public PeerConnection getmPeerConnection() {
        return mPeerConnection;
    }

    public void drainCandidate() {
        if (queuedRemoteCandidates != null) {
            for (IceCandidate candidate : queuedRemoteCandidates) {
                mPeerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    public void createOfferSdp() {
        if (mPeerConnection != null) {
            mPeerConnection.createOffer(mPeerConnectionObserver, mSdpConstraints);
        }
    }

    public void createAnswerSdp() {
        if (mPeerConnection != null) {
            mPeerConnection.createAnswer(mPeerConnectionObserver, mSdpConstraints);
        }
    }

    public void setRemoteDescription(SessionDescription sessionDescription) {
        if (mPeerConnection != null) {
            if (mPeerConnection.getRemoteDescription() == null) {
                mPeerConnection.setRemoteDescription(mPeerConnectionObserver, sessionDescription);
            }
        }
    }

    public void setLocalDescription(SessionDescription sessionDescription) {
        if (mPeerConnection != null) {
            mPeerConnection.setLocalDescription(mPeerConnectionObserver, sessionDescription);
        }
    }

    public MediaStream createLocalStream() {
        if (mPeerConnectionFactory != null) {
            return mPeerConnectionFactory.createLocalMediaStream("ARDAMS");
        }
        return null;
    }

    public void setLocalStream(MediaStream mediaStream) {
        if (mPeerConnection != null) {
            mPeerConnection.addStream(mediaStream);
        }
    }

    public void startVideoSource() {
        if (mVideoSource != null && videoSourceStopped) {
            mVideoSource.restart();
            videoSourceStopped = false;
        }
    }

    public boolean isVideoSourceStopped(){
        return videoSourceStopped;
    }

    public void stopVideoSource() {
        if (mVideoSource != null && !videoSourceStopped) {
            mVideoSource.stop();
            videoSourceStopped = true;
        }
    }

    public VideoSource createVideoSource(VideoCapturer videoCapturer, MediaConstraints videoConstraints) {
        mVideoSource = mPeerConnectionFactory.createVideoSource(videoCapturer, videoConstraints);
        return mVideoSource;
    }

    public VideoTrack createVideoTrack(VideoSource videoSource) {
        VideoTrack videoTrack = mPeerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.setEnabled(true);
        return videoTrack;
    }

    public AudioTrack createAudioTrack(MediaConstraints audioConstraints) {
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(audioConstraints);
        return mPeerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
    }

    public void disposePeerConnection() {
        if (mPeerConnection != null) {
            mPeerConnection.dispose();
            mPeerConnection = null;
        }
        if (mVideoSource != null) {
            mVideoSource.dispose();
            mVideoSource = null;
        }
        if (mPeerConnectionFactory != null) {
            mPeerConnectionFactory.dispose();
            mPeerConnectionFactory = null;
        }
        if (mPeerConnectionObserver != null) {
            mPeerConnectionObserver = null;
        }
        options = null;
        //PeerConnectionFactory.stopInternalTracingCapture();
        //PeerConnectionFactory.shutdownInternalTracer();
    }

    public void sendSignal(String destination, String data) {
        StompClient.getInstance().send(destination, data).subscribe();
    }

}
