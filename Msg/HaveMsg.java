package Msg;

import java.util.Arrays;

/**
 * Created by jiantaozhang on 2017/10/21.
 */
public class HaveMsg extends ActualMsg{

    public HaveMsg() {
        super((byte) 4);
    }

    public HaveMsg(byte[] payload) {
        super((byte) 4);
        setMessagePayload(payload);
    }
}
