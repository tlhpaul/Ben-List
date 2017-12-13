package edu.upenn.benslist;

import java.util.Map;

/**
 * Created by paulhsu on 03/11/2017.
 */

public class Transaction {
    private String buyerID;
    private String sellerID;
    private String productID;
    private Map<String, String> timeStamp;
    private long time;
    private String rating;
    private String transactionID;

    public Transaction(){
        this.buyerID = "";
        this.sellerID = "";
        this.productID = "";
        this.timeStamp = null;
    }

    public Transaction(String buyerID, String sellerID, String productID, Map<String, String> timeStamp){
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.productID = productID;
        this.timeStamp = timeStamp;
    }

    public Transaction(String buyerID, String sellerID, String productID, long time, String rating){
        this.buyerID = buyerID;
        this.sellerID = sellerID;
        this.productID = productID;
        this.time = time;
        this.rating = rating;
    }

    public void setBuyerID(String buyerID){
        this.buyerID = buyerID;
    }

    public void setSellerID(String sellerID){
        this.sellerID = sellerID;
    }

    public void setProductID(String productID){
        this.productID = productID;
    }

    public void setTimeStamp(Map<String, String> timeStamp) { this.timeStamp = timeStamp; }

    public String getBuyerID(){
        return buyerID;
    }

    public String getSellerID(){
        return sellerID;
    }

    public String getProductID(){
        return productID;
    }

    public Map<String, String> getTimeStamp() {
        return timeStamp;
    }

    public long getTime() { return time; }

    public String getRating() { return rating; }

    public void setTransactionID(String transactionID) { this.transactionID = transactionID; }

    public String getTransactionID() { return transactionID; }
}
