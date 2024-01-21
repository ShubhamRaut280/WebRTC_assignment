package com.shubham.webrtc_videochat.utils;

public class DataModel {
    String targetUser;
    String sender;
    String data;
    dataModelType type;

    public DataModel(String targetUser, String sender, String data, dataModelType type) {
        this.targetUser = targetUser;
        this.sender = sender;
        this.data = data;
        this.type = type;
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public dataModelType getType() {
        return type;
    }

    public void setType(dataModelType type) {
        this.type = type;
    }
}
