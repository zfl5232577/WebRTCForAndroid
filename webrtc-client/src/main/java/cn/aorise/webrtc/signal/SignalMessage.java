package cn.aorise.webrtc.signal;

/**
 * Created by 54926 on 2017/9/4.
 */

public class SignalMessage {

    private String sender;
    private String recipient;
    private String data;
    private String type;
    private String senderName;
    private String senderImgurl;

    public SignalMessage(String sender, String recipient, String data, String type, String senderName, String senderImgurl) {
        this.sender = sender;
        this.recipient = recipient;
        this.data = data;
        this.type = type;
        this.senderName = senderName;
        this.senderImgurl = senderImgurl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImgurl() {
        return senderImgurl;
    }

    public void setSenderImgurl(String senderImgurl) {
        this.senderImgurl = senderImgurl;
    }
}
