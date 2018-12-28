package cn.aorise.webrtc.webrtc;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;

import java.io.File;
import java.util.LinkedList;

import cn.aorise.webrtc.chat.ChatClient;

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
//         PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_AUTOMATIC_RESIZE);
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
//                .setFieldTrials(fieldTrials)
                .setEnableInternalTracer(true)
                .createInitializationOptions());
        options = new PeerConnectionFactory.Options();
        mPeerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .createPeerConnectionFactory();

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
                synchronized (queuedRemoteCandidates) {
                    if (queuedRemoteCandidates != null) {
                        queuedRemoteCandidates.add(candidate);
                    }
                }
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
        iceServers.addAll(ChatClient.getInstance().getIceServers());
//        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun01.sipphone.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.ekiga.net"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.fwdnet.net"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.ideasip.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.iptel.org"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.rixtelecom.se"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.schlund.de"));
//        iceServers.add(new PeerConnection.IceServer("stun:stunserver.org"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.softjoys.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.voiparound.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.voipbuster.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.voipstunt.com"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.voxgratia.org"));
//        iceServers.add(new PeerConnection.IceServer("stun:stun.xten.com"));
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
                rtcConfig, mPeerConnectionObserver);
    }

    public PeerConnection getmPeerConnection() {
        return mPeerConnection;
    }

    public void drainCandidate() {
        synchronized (queuedRemoteCandidates) {
            if (queuedRemoteCandidates != null) {
                Log.e("BaseCallActivity", "drainCandidate: " + Thread.currentThread().getName());
                for (IceCandidate candidate : queuedRemoteCandidates) {
                    mPeerConnection.addIceCandidate(candidate);
                }
                queuedRemoteCandidates = null;
            }
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
            mVideoSource.getCapturerObserver().onCapturerStopped();
            videoSourceStopped = false;
        }
    }

    public boolean isVideoSourceStopped() {
        return videoSourceStopped;
    }

    public void stopVideoSource() {
        if (mVideoSource != null && !videoSourceStopped) {
            mVideoSource.getCapturerObserver().onCapturerStarted(true);
            videoSourceStopped = true;
        }
    }

    public VideoSource createVideoSource(VideoCapturer videoCapturer, MediaConstraints videoConstraints) {
        mVideoSource = mPeerConnectionFactory.createVideoSource(true);
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

//    private static String getFieldTrials(PeerConnectionParameters peerConnectionParameters) {
//        String fieldTrials = "";
//        if (peerConnectionParameters.videoFlexfecEnabled) {
//            fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
//            Log.d(TAG, "Enable FlexFEC field trial.");
//        }
//        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
//        if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
//            fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
//            Log.d(TAG, "Disable WebRTC AGC field trial.");
//        }
//        return fieldTrials;
//    }

}
