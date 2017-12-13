package edu.upenn.benslist;

import java.util.Map;

/**
 * Created by BearBurg on 2017/11/7.
 */

public class Inbox {
    enum TYPE{
        MESSAGE,REQUEST;
    }



    protected String senderID;
    protected String receiverID;


    protected  Map<String, String> timeStamp;
    protected TYPE type;

    public Inbox(){
        this.senderID = "";
        this.receiverID = "";
        this.timeStamp = null;
    }

    public Inbox(String senderID, String receiverID, Map<String, String> timeStamp){
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.timeStamp = timeStamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public Map<String, String> getTimeStamp() {
        return timeStamp;
    }
}
