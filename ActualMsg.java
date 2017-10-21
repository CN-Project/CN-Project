import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class ActualMsg {

    /**
     * Define every part of ActualMsg, HashMap used to get message type.
     */
    protected byte[] messageLength;

    protected byte messageType;

    protected byte[] messagePayload;

    protected Map<Byte, String> messageTypeMap;


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
    public void setMssagegType(byte type) {
        this.messageType = type;
    }

    public void setMessageLength(byte[] messageLength) {
        this.messageLength = messageLength;
    }

    public void setMessagePayload(byte[] payload) {
        this.messagePayload = payload;
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
}
