package com.shubham.webrtc_videochat.webRTC;

import android.content.Context;

import com.google.gson.Gson;
import com.shubham.webrtc_videochat.utils.DataModel;
import com.shubham.webrtc_videochat.utils.dataModelType;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCclient {
    final String username;
    Gson gson = new Gson();
    final Context context;
    EglBase.Context eglbaseContext = EglBase.create().getEglBaseContext();
    PeerConnectionFactory peerConnectionFactory;
    PeerConnection peerConnection;
    List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private CameraVideoCapturer videoCapturer;
    private VideoSource localVideoSource;
    private AudioSource localAudioSource;
    private String localTrackId = "local_track";
    private String localStreamId = "local_stream";
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private MediaStream localStream;
    public Listener listener;
    private MediaConstraints mediaConstraints = new MediaConstraints();
    public WebRTCclient(Context context, PeerConnection.Observer observer, String username) {
        this.username = username;
        this.context = context;
        initPeerConnectionFactory();
        peerConnectionFactory = createPeerConnectionFact();
        iceServers.add(PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R").createIceServer());
        peerConnection = createPeerConnection(observer);
        localVideoSource = peerConnectionFactory.createVideoSource(false);
        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo","true"));
    }


    //Initializing peer connection
    private void initPeerConnectionFactory() {
        PeerConnectionFactory.InitializationOptions options = PeerConnectionFactory.InitializationOptions.builder(context).setFieldTrials("WebRTC-H246HighProfile/Enabled.")
                .setEnableInternalTracer(true).createInitializationOptions();
        PeerConnectionFactory.initialize(options);
    }

    private PeerConnectionFactory createPeerConnectionFact()
    {
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.disableEncryption = false;
        options.disableNetworkMonitor =false;

        return PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(eglbaseContext, true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(eglbaseContext))
                .setOptions(options).createPeerConnectionFactory();

    }

    private PeerConnection createPeerConnection(PeerConnection.Observer observer)
    {
        return peerConnectionFactory.createPeerConnection(iceServers, observer);
    }


    // Initializing ui

    public void initSurfaceViewRendere(SurfaceViewRenderer viewRenderer){
        viewRenderer.setEnableHardwareScaler(true);
        viewRenderer.setMirror(true);
        viewRenderer.init(eglbaseContext,null);
    }

    public void initLocalSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRendere(view);
        startLocalVideoStreaming(view);
    }

    private void startLocalVideoStreaming(SurfaceViewRenderer view) {
        SurfaceTextureHelper helper= SurfaceTextureHelper.create(
                Thread.currentThread().getName(), eglbaseContext
        );

        videoCapturer = getVideoCapturer();
        videoCapturer.initialize(helper,context,localVideoSource.getCapturerObserver());
        videoCapturer.startCapture(480,360,15);
        localVideoTrack = peerConnectionFactory.createVideoTrack(
                localTrackId+"_video",localVideoSource
        );
        localVideoTrack.addSink(view);

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId+"_audio",localAudioSource);
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId);
        localStream.addTrack(localVideoTrack);
        localStream.addTrack(localAudioTrack);
        peerConnection.addStream(localStream);
    }

    private CameraVideoCapturer getVideoCapturer() {
        Camera2Enumerator enumerator = new Camera2Enumerator(context);

        String[] deviceNames = enumerator.getDeviceNames();

        for (String device: deviceNames){
            if (enumerator.isFrontFacing(device)){
                return enumerator.createCapturer(device,null);
            }
        }
        throw new IllegalStateException("front facing camera not found");
    }

    public void initRemoteSurfaceView(SurfaceViewRenderer view){
        initSurfaceViewRendere(view);
    }

    // section for call and answer, etc
    public void call(String target){
        try{
            peerConnection.createOffer(new SDPobserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new SDPobserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            //its time to transfer this sdp to other peer
                            if (listener!=null){
                                listener.onTransferDataToOtherPeer(new DataModel(
                                        target,username,sessionDescription.description, dataModelType.Offer
                                ));
                            }
                        }
                    },sessionDescription);
                }
            },mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void answer(String target){
        try{
            peerConnection.createAnswer(new SDPobserver(){
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    peerConnection.setLocalDescription(new SDPobserver(){
                        @Override
                        public void onSetSuccess() {
                            super.onSetSuccess();
                            //its time to transfer this sdp to other peer
                            if (listener!=null){
                                listener.onTransferDataToOtherPeer(new DataModel(
                                        target,username,sessionDescription.description, dataModelType.Answer
                                ));
                            }
                        }
                    },sessionDescription);
                }
            },mediaConstraints);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onRemoteSessionReceived(SessionDescription sessionDescription){
        peerConnection.setRemoteDescription(new SDPobserver(),sessionDescription);
    }

    public void addIceCandidate(IceCandidate iceCandidate){
        peerConnection.addIceCandidate(iceCandidate);
    }

    public void sendIceCandidate(IceCandidate iceCandidate, String target){
        addIceCandidate(iceCandidate);
        if (listener!=null){
            listener.onTransferDataToOtherPeer(new DataModel(
                    target,username,gson.toJson(iceCandidate),dataModelType.IceCandidate
            ));
        }
    }

    public void switchCamera() {
        videoCapturer.switchCamera(null);
    }

    public void toggleVideo(Boolean shouldBeMuted){
        localVideoTrack.setEnabled(shouldBeMuted);
    }

    public void toggleAudio(Boolean shouldBeMuted){
        localAudioTrack.setEnabled(shouldBeMuted);
    }

    public void closeConnection(){
        try{

            localVideoTrack.dispose();
            videoCapturer.stopCapture();
            videoCapturer.dispose();
            peerConnection.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface Listener {
        void onTransferDataToOtherPeer(DataModel model);
    }
}
