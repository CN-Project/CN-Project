package Msg;

import Msg.ActualMsg;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class BitField extends ActualMsg {

    /**
     * This Msg.BitField class extends Msg.ActualMsg, only define the message type to 5.
     */
    public BitField() {
        super((byte) 5);
    }

    /**
     * Get the Msg.BitField payload information, return a boolean for the list of pieces.
     */
    public boolean[] getBitFieldPayload() {
        byte[] payload = getMessagePayload();
        if (payload == null || payload.length == 0) {
            throw new IndexOutOfBoundsException("Payload not found, need to set payload first");
        }

        int bytelen = payload.length;
        int k = 0;
        boolean[] bitfieldInfo = new boolean[bytelen * 8];
        for (int i = 0; i < bytelen; i++) {
            byte b = payload[i];
            for (int j = 7; j >= 0; j--) {
                if (((b >> j) & 0x01) == 1) {
                    bitfieldInfo[k++] = true;
                } else {
                    bitfieldInfo[k++] = false;
                }
            }
        }
        return bitfieldInfo;
    }


}
