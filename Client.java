/**
 * Created by xiyaoma on 10/19/17.
 */
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import Msg.*;

public class Client extends Thread{

    private Socket requestSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String MsgType;

    private String clientPeerId;
    private String serverPeerID;
    private String serverPeerAddr;
    private String listeningPort;
    private boolean hasFileOrNot;

    public Peer clientPeer;
    public Peer serverPeer;

    private HandShakeMsg sentHandShakeMsg = new HandShakeMsg(); // HandShake Msg send to the server
    private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg(); // HandShake Msg received from the server
    private ActualMsg haveMag = new HaveMsg();
    private ActualMsg sentActualMsg; //Actual Msg send to the server
    private ActualMsg receivedActualMsg; //Actual Msg received from the server
    private byte[] indexOfPiece;

    public Client(Peer clientPeer, Peer serverPeer){
        this.clientPeer = clientPeer;
        this.serverPeer = serverPeer;
        this.clientPeerId = clientPeer.getPeerId();
        this.serverPeerID = serverPeer.getPeerId();
        this.serverPeerAddr = serverPeer.getHostName();
        this.hasFileOrNot = clientPeer.getHasFileOrNot();
//        this.listeningPort = serverPeer.getListeningPort();
        this.listeningPort = "8000";
        if(this.hasFileOrNot){
            //set all bits of bitfield 1
            clientPeer.setBitFieldSelfAllOne();
        }
    }

    public void run(){
        try{
            //initialize Socket of client
            requestSocket = new Socket(serverPeerAddr, Integer.parseInt(listeningPort));
            System.out.println("{Client} connect to localhost at port 8000");
            //initialize outputstream and inputstream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            //1. send handshake to server
            sentHandShakeMsg.setPeerID(clientPeerId);
            System.out.println("{Client} Send handshake message from Client " + this.clientPeerId + " to Server " + this.serverPeerID) ;
            sendHandShakeMsg(sentHandShakeMsg);
            System.out.println("{Client} Waiting Reply Handshake...");
            //read socket in
            Object readObject = in.readObject();
            System.out.println("{Client} Receive: " + readObject.getClass().getName());
            //if received Msg is handshake
            if(readObject.getClass().getName() == "Msg.HandShakeMsg"){
                receivedHandShakeMsg = (HandShakeMsg) readObject;
                System.out.println("{Client} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader() + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                if(receivedHandShakeMsg.getPeerID() == this.serverPeerID){
                    System.out.println("{Client} HandShake succeed! ");
                }
            }

            //2. send bitfield Msg to server
            sentActualMsg = new BitFieldMsg(clientPeer.getNumOfPiece());
            System.out.println("{Client} set the bitfield of BitfieldMsg.");
            sentActualMsg.setMessagePayload(sentActualMsg.booleanArray2byteArray(clientPeer.getBitFieldSelf()));
            System.out.println("{Client} sent bitfieldMsg from Client " + this.clientPeerId + "to Client " + this.serverPeerID);
            sendActualMsg(sentActualMsg);

//            // enter while loop and wait various kinds of Msg from neighbors
//            while(true){
//                //read socket in
//                readObject = in.readObject();
//                System.out.println("{Client} Receive: " + readObject.getClass().getName());
//                MsgType = readObject.getClass().getName();
//            }


        }
        catch (ConnectException e)
        {
            System.err.println("Connection refused. You need to initiate a server first.");
        }
        catch ( ClassNotFoundException e )
        {
            System.err.println("Class not found");
        }
        catch(UnknownHostException unknownHost)
        {
            System.err.println("You are trying to connect to an unknown host!");
        }
        catch(IOException ioException)
        {
            ioException.printStackTrace();
        } finally {
            //close TCP connection
            try {
                in.close();
                out.close();
                requestSocket.close();
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

//    public static void main(String args[]){
//        Client client = new Client();
//        client.run();
//    }

    /***
     * send message via socket
     * @param message
     */
    public void sendMsg(String message){
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("{Client} Send message: " + message);
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    /***
     * send HandShakeMessage via socket
     * @param handShakeMsg
     */
    public void sendHandShakeMsg(HandShakeMsg handShakeMsg){
        try {
            out.writeObject(handShakeMsg);
            out.flush();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    /***
     * send Actual Msg include 8 different kinds of Msgs via socket
     * @param actualMsg
     */
    public void sendActualMsg(ActualMsg actualMsg){
        try {
            out.writeObject(actualMsg);
            out.flush();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}

