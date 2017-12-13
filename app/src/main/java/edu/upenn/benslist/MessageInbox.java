package edu.upenn.benslist;

import java.util.Map;

/**
 * Created by BearBurg on 2017/11/7.
 */

public class MessageInbox {
    protected String message;
    protected String senderID;
    protected String receiverID;
    protected  Map<String, String> timeStamp;
    protected String type = "message";

    public MessageInbox(){
        this.message = "";
        this.senderID = "";
        this.receiverID = "";
        this.timeStamp = null;
        this.type = "message";
    }

    public MessageInbox(String message, String senderID, String receiverID,
                        Map<String, String> timeStamp) {
        this.message = message;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public Map<String, String> getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Map<String, String> timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
