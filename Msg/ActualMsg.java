package Msg;
import java.io.Serializable;
import java.util.*;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class ActualMsg implements Serializable{

    /**
     * Define every part of ActualMsg, HashMap used to get message type.
     */
    protected byte[] messageLength;

    protected byte messageType;

    protected byte[] messagePayload;

    protected Map<Byte, String> messageTypeMap;

    public ActualMsg(){}

    public ActualMsg(byte messageType) {

        messageLength = new byte[4];
        this.messageType = messageType;
        messagePayload = null;

        // Store all message type in a map, can be recalled easily.
        messageTypeMap = new HashMap<>();
        messageTypeMap.put((byte) 0, "Choke");
        messageTypeMap.put((byte) 1, "Unchoke");
        messageTypeMap.put((byte) 2, "Interested");
        messageTypeMap.put((byte) 3, "NotInterested");
        messageTypeMap.put((byte) 4, "Have");
        messageTypeMap.put((byte) 5, "BitField");
        messageTypeMap.put((byte) 6, "Request");
        messageTypeMap.put((byte) 7, "Piece");
    }

    /**
     * Functions used to initialize ActualMsg.
     */
    //need to be forbidden for safety!----------------------------------
    public void setMssagegType(byte type) {
        this.messageType = type;
    }

    private void setMessageLength() {
        String length = String.valueOf(1 + this.messagePayload.length);
        this.messageLength = length.getBytes();
    }

    public void setMessagePayload(byte[] payload) {
        if(payload == null){
            throw new IndexOutOfBoundsException("Payload is null, please give a valid payload.");
        }
        this.messagePayload = payload;
        setMessageLength();
    }


    /**
     * Functions used to get message information
     */
    public String getMessageType() {
        return messageTypeMap.get(this.messageType);
    }

    public String getTypeMapInfo(byte b) {
        return messageTypeMap.get(b);
    }

    public byte[] getMessageLength() {
        return this.messageLength;
    }

    public byte[] getMessagePayload() {
        return this.messagePayload;
    }
    public byte[] booleanArray2byteArray(boolean[] bitFiled) {
        return null;
    }
    public boolean[] byteArray2booleanArray(byte[] bytes){
        return null;
    }

    /***
     * extract index from Msg, only three Msg have index field, HaveMsg, PieceMsg, RequestedMsg
     * @return
     */
    public byte[] parseIndexFromPieceMsg(){ // Xiyao Ma
        if(this.getMessageType().equals("Have") || this.getMessageType().equals("Request") || this.getMessageType().equals("Piece")){
            return Arrays.copyOfRange(this.messagePayload, 0, 4);
        }
        System.out.println("Wrong Msg Type, don't has index filed. Msg must be one of following Msgs: HaveMsg, RequestMsg, PieceMsg. ");
        return null;
    }
}
