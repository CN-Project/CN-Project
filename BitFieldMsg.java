/**
 * Created by xiyaoma on 10/20/17.
 */
public class BitFieldMsg {

    byte[] messageLength;
    byte[] messageType;
    byte[] messagePayload;

    public BitFieldMsg(){
        this.messageLength = new byte[4];
        this.messageType = new byte[1];
    }

    public void setMsgtype(String str){
        byte[] MsgType = str.getBytes();
        if(MsgType.length != 1){
            System.out.println("Invalid Message type, please set a valid one.");
        }else {
            this.messageType = MsgType;
        }
    }

    public void setMsgLength(){
        String length = String.valueOf(this.messageType.length + this.messagePayload.length);
        this.messageLength = length.getBytes();
    }

    public void setMsgPayload(){

    }
}
