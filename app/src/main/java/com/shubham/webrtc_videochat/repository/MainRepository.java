package com.shubham.webrtc_videochat.repository;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.shubham.webrtc_videochat.remote.FirebaseClient;
import com.shubham.webrtc_videochat.utils.DataModel;
import com.shubham.webrtc_videochat.utils.ErrorCallback;
import com.shubham.webrtc_videochat.utils.NewEventCallback;
import com.shubham.webrtc_videochat.utils.SuccessCallback;
import com.shubham.webrtc_videochat.utils.dataModelType;
import com.shubham.webrtc_videochat.webRTC.PeerConnectionObserver;
import com.shubham.webrtc_videochat.webRTC.WebRTCclient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;


public class MainRepository implements WebRTCclient.Listener {

    public Listener listener;
    private final Gson gson = new Gson();
    private final FirebaseClient firebaseClient;

    private WebRTCclient webRTCClient;

    private String currentUsername;

    private SurfaceViewRenderer remoteView;

    private String target;
    private void updateCurrentUsername(String username){
        this.currentUsername = username;
    }

    private MainRepository(){
        this.firebaseClient = new FirebaseClient();
    }

    private static MainRepository instance;
    public static MainRepository getInstance(){
        if (instance == null){
            instance = new MainRepository();
        }
        return instance;
    }

    public void login(String username, Context context, SuccessCallback callBack){
        firebaseClient.login(username,()->{
            updateCurrentUsername(username);
            this.webRTCClient = new WebRTCclient(context,new PeerConnectionObserver(){
                @Override
                public void onAddStream(MediaStream mediaStream) {
                    super.onAddStream(mediaStream);
                    try{
                        mediaStream.videoTracks.get(0).addSink(remoteView);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                    Log.d("TAG", "onConnectionChange: "+newState);
                    super.onConnectionChange(newState);
                    if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                        listener.webrtcConnected();
                    }

                    if (newState == PeerConnection.PeerConnectionState.CLOSED ||
                            newState == PeerConnection.PeerConnectionState.DISCONNECTED ){
                        if (listener!=null){
                            listener.webrtcClosed();
                        }
                    }
                }

                @Override
                public void onIceCandidate(IceCandidate iceCandidate) {
                    super.onIceCandidate(iceCandidate);
                    webRTCClient.sendIceCandidate(iceCandidate,target);
                }
            },username);
            webRTCClient.listener = this;
            callBack.onSuccess();
        });
    }

    public void initLocalView(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    public void startCall(String target){
        webRTCClient.call(target);
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }
    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }
    public void sendCallRequest(String target, ErrorCallback errorCallBack){
        firebaseClient.sendMessageTootherUser(
                new DataModel(target,currentUsername,null, dataModelType.StartCall),errorCallBack
        );
    }

    public void endCall(){
        webRTCClient.closeConnection();
    }

    public void subscribeForLatestEvent(NewEventCallback callBack){
        firebaseClient.observeIncomingEventes(model -> {
            switch (model.getType()){

                case Offer:
                    this.target = model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.OFFER,model.getData()
                    ));
                    webRTCClient.answer(model.getSender());
                    break;
                case Answer:
                    this.target = model.getSender();
                    webRTCClient.onRemoteSessionReceived(new SessionDescription(
                            SessionDescription.Type.ANSWER,model.getData()
                    ));
                    break;
                case IceCandidate:
                    try{
                        IceCandidate candidate = gson.fromJson(model.getData(),IceCandidate.class);
                        webRTCClient.addIceCandidate(candidate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case StartCall:
                    this.target = model.getSender();
                    callBack.onNewEventReceived(model);
                    break;
            }

        });
    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        firebaseClient.sendMessageTootherUser(model,()->{});
    }

    public interface Listener{
        void webrtcConnected();
        void webrtcClosed();
    }
}