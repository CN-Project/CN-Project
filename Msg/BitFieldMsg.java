package Msg;
import java.util.*;
import Msg.ActualMsg;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class BitFieldMsg extends ActualMsg {

    /**
     * This Msg.BitFieldMsg class extends Msg.ActualMsg, only define the message type to 5.
     */
    public BitFieldMsg(int numOfPiece) {
        super((byte) 5);
        this.messagePayload = new byte[(int) Math.ceil(numOfPiece / 8)];
    }

    /***
     * convert boolean array to byte array
     * @param bitFiled
     * @return
     */
    public byte[] booleanArray2byteArray(boolean[] input) {
        byte[] toReturn = new byte[input.length / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (input[entry * 8 + bit]) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }

        return toReturn;
    }

    /***
     * convert byte array to boolean array
     * @param bytes
     * @return boolean array
     */
    public boolean[] byteArray2booleanArray(byte[] bytes) {
        boolean[] result = new boolean[Byte.SIZE * bytes.length];
        int offset = 0;
        for (byte b : bytes) {
            for (int i=0; i<Byte.SIZE; i++){
                result[i+offset] = (b >> i & 0x1) != 0x0;
            }
            offset+=Byte.SIZE;
        }
        return result;
    }
}
