package edu.buffalo.cse.cse486586.groupmessenger2;

public class Message   {
    // constructor of
    String messageType = null, message = null, sender = null, receiver = null, messageID = null, failedPort=null;
    int proposedNumber = -1;
    int sequenceNumber = -1;
    boolean isDeliverable;

    /*
    Types of messages
    1) Multicast message to each process
    2) Proposed message (Maintain a proposed queue)
    3) Each process replies with a proposed sequence number
    4) Choose the sequence number as the biggest number and then multicast the message with that sequence number
        (change the queue, using the comparator)
 */


    // constructor of multicast message
    public Message(String type, String actualMessage, String senderId, String receiverId, String msgId){
        messageType = type; //type should be "MULTICASTS
        message = actualMessage;
        sender = senderId;
        receiver = receiverId;
        messageID = msgId;
    }

    // constructor of proposed message
    public Message(String type, String actualMessage, String senderId, String receiverId, int proposed) {
        messageType = type; //type should be "PROPOSED"
        message = actualMessage;
        sender = senderId;
        receiver = receiverId;
        proposedNumber = proposed;
    }

    // constructor of Sequence Message
    public Message(String type, String actualMessage, String senderId, String receiverId, int sequence, String msgId) {
        messageType = type; //type should be "SEQUENCE"
        message = actualMessage;
        sender = senderId;
        receiver = receiverId;
        sequenceNumber = sequence;
        messageID = msgId;
    }

    // actual message to store in the queue
    public Message(String type, String actualMessage, String senderId, String receiverId, boolean deliveryStatus, String msgId, int proposed) {
        messageType = type; //type should be "SEQUENCE"
        message = actualMessage;
        sender = senderId;
        receiver = receiverId;
        isDeliverable = deliveryStatus;
        messageID = msgId;
        sequenceNumber = proposed;
    }

    public Message(String type, String fp) {
        messageType = "FAILED";
        failedPort = fp;
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();
        if(this.messageType != null) {
            sb.append("messageType:" + this.messageType);
            //sb.append(this.messageType);
            sb.append(";");
        }
        if(this.sender != null) {
            sb.append("sender:" + this.sender);
            //sb.append(this.sender);
            sb.append(";");
        }
        if(this.message != null) {

            sb.append("message:" + this.message);
            //sb.append(this.message);
            sb.append(";");
        }
        if(this.receiver != null) {
            sb.append("receiver:" + this.receiver);
            //sb.append(this.receiver);
            sb.append(";");
        }
        if(this.proposedNumber != -1) {
            sb.append("proposedNumber:" + String.valueOf(this.proposedNumber));
            //sb.append(String.valueOf(this.proposedNumber));
            sb.append(";");
        }
        if(this.sequenceNumber != -1) {
            sb.append("sequenceNumber:" + String.valueOf(this.sequenceNumber));
            //sb.append(String.valueOf(this.sequenceNumber));
            sb.append(";");
        }
        if(this.messageID != null) {
            sb.append("messageID:" + String.valueOf(this.messageID));
            //sb.append(String.valueOf(this.messageID));
            sb.append(";");
        }
        if(this.failedPort != null) {
            sb.append("failedPort:" + this.failedPort);
            sb.append(";");
        }
        return sb.toString();
    }
}