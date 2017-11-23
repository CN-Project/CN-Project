package Msg;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class RequestMsg extends ActualMsg{
    public RequestMsg(byte[] payload) {
        super((byte) 6);
        setMessagePayload(payload);
    }
}
