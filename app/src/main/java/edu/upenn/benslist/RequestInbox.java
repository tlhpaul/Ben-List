package edu.upenn.benslist;


import java.io.Serializable;
import java.util.Map;

/**
 * Created by BearBurg on 2017/11/7.
 */

public class RequestInbox implements Serializable{

    String productID;
    protected String senderID;
    protected String receiverID;
    protected  Map<String, String> timeStamp;
    protected String productName;
    protected  String type;


    public RequestInbox(){
        this.productID = "";
        this.senderID = "";
        this.receiverID = "";
        this.productName = "";
        this.timeStamp = null;
        this.type = "";
    }


    public RequestInbox(String productID, String senderID, String receiverID, String productName, Map<String, String> timeStamp) {
        this.productID = productID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.productName = productName;
        this.timeStamp = timeStamp;
        this.type = "request";
    }

    public String getProductID() {
        return productID;
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

    public String getType() {
        return type;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setTimeStamp(Map<String, String> timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setType(String type) {
        this.type = type;
    }


}
