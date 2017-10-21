import java.io.Serializable;

/**
 * Created by xiyaoma on 10/19/17.
 */
public class HandShakeMsg implements Serializable{

    /** The 18 bytes handshake header. */
    private byte[] handShakeHeader;
    /** The 10 bytes zero bits. */
    private byte[] zeroBits;
    /** The 4bytes peer id. */
    private byte[] peerID;

    public HandShakeMsg(){

        this.handShakeHeader = new byte[18];

        if (Parameters.HandShakeMsg.getBytes().length != 18){
            System.out.println("invalid ");
        }else {
            this.handShakeHeader = Parameters.HandShakeMsg.getBytes();
        }
        this.zeroBits = new byte[10];
        this.peerID = new byte[4];
    }

    public void setPeerID(String str){
        /***
         * set peerID field of handshakeMsg
         */
        byte[] PeerID = str.getBytes();
        if(peerID.length != 4){
            System.out.println("Invalid PeerID");//error
        }else {
            this.peerID = PeerID;
        }
    }

    public String getPeerID(){
        /***
         * get peerID field of handshakeMsg
         */
        String peerID = new String(this.peerID);
        return peerID;
    }

    public String getHandShakeHeader(){
        /***
         * get header field of handshakeMsg
         */
        String handShakeHeader = new String(this.handShakeHeader);
        return handShakeHeader;
    }
}
