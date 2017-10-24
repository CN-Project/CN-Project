package Msg;

import java.util.*;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class PieceMsg extends ActualMsg {

    public PieceMsg() {
        super((byte) 7);
    }

    /***
     * parse 4-byte piece index from HaveMsg
     * @return  4-byte piece index
     */
    public byte[] parseIndexFromHaveMsg(){
        return Arrays.copyOfRange(this.messagePayload, 0, 4);
    }
}
