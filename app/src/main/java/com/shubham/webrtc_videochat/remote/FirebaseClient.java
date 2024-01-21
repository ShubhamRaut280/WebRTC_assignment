package com.shubham.webrtc_videochat.remote;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.shubham.webrtc_videochat.utils.ErrorCallback;
import com.shubham.webrtc_videochat.utils.NewEventCallback;
import com.shubham.webrtc_videochat.utils.SuccessCallback;
import com.shubham.webrtc_videochat.utils.DataModel;

import java.util.Objects;

public class FirebaseClient {

    final Gson gson = new Gson();
    String Currentusername;
    final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    static  final String LATEST_EVENT_FIELD_NAME = "Latest_event";


    public void login(String username, SuccessCallback callback)
    {
        dbRef.child(username).setValue("").addOnSuccessListener(task->{
           Currentusername = username;
           callback.onSuccess();
        });
    }

    public void sendMessageTootherUser(DataModel dataModel, ErrorCallback callback)
    {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if target user found
                if(snapshot.child(dataModel.getTargetUser()).exists())
                {
                    // send signal to other user
                    dbRef.child(dataModel.getTargetUser()).child(LATEST_EVENT_FIELD_NAME)
                            .setValue(gson.toJson(dataModel));
                    // data sent by serializing into json
                }
                else
                     callback.onError();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError();
            }
        });
    }

    public void observeIncomingEventes(NewEventCallback callback)
    {
        dbRef.child(Currentusername).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String data = Objects.requireNonNull(snapshot.getValue().toString());
                    DataModel model = gson.fromJson(data, DataModel.class);
                    callback.onNewEventReceived(model);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
