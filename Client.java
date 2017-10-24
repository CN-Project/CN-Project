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

    private String peerID;
    private String serverPeerID;
    private String serverPeerAddr;
    private String listeningPort;
    private boolean hasFileOrNot;
    private int numOfPiece;

    public Peer curPeer;
    public Peer serverPeer;

    private HandShakeMsg sentHandShakeMsg = new HandShakeMsg(); // HandShake Msg send to the server
    private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg(); // HandShake Msg received from the server
    private ActualMsg haveMag = new HaveMsg();
    private ActualMsg sentActualMsg; //Actual Msg send to the server
    private ActualMsg receivedActualMsg; //Actual Msg received from the server


    public Client(Peer curPeer, Peer serverPeer, int numOfPiece){
        this.curPeer = curPeer;
        this.serverPeer = serverPeer;
        this.peerID = curPeer.getPeerId();
        this.serverPeerID = serverPeer.getPeerId();
        this.serverPeerAddr = serverPeer.getHostName();
        this.hasFileOrNot = curPeer.getHasFileOrNot();
//        this.listeningPort = serverPeer.getListeningPort();
        this.listeningPort = "8000";
        this.numOfPiece = numOfPiece;
//        // maintain a bitfield of itself for which piece of document it has
//        curPeer.bitFieldSelf = new boolean[(int)Math.ceil(numOfPiece / 8) * 8];
        if(this.hasFileOrNot == true){
            //set all bits of bitfield 1
            curPeer.setBitFieldSelfAllOne();
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
            sentHandShakeMsg.setPeerID(peerID);
            System.out.println("{Client} Send handshake message from Client " + peerID + " to Client " + this.serverPeerID) ;
            sendHandShakeMsg(sentHandShakeMsg);
            System.out.printf("{Client} Waiting Reply Handshake...");
            //read socket in
            Object readObject = in.readObject();
            System.out.println("{Client} Receive: " + readObject.getClass().getName());
            //if received Msg is handshake
            if(readObject.getClass().getName() == "Msg.HandShakeMsg"){
                receivedHandShakeMsg = (HandShakeMsg) readObject;
                System.out.println("{Client} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader() + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                if(receivedHandShakeMsg.getPeerID() == serverPeerID){
                    System.out.println("{Client} HandShake succeed! ");
                }
            }

            //2. send bitfield Msg to server
            sentActualMsg = new BitFieldMsg(this.numOfPiece);
            System.out.printf("{Client} set the bitfield of BitfieldMsg.");
            sentActualMsg.setMessagePayload(sentActualMsg.booleanArray2byteArray(curPeer.getBitFieldSelf()));
            System.out.printf("{Client} sent bitfieldMsg from Client " + peerID + "to Client " + serverPeerID);
            sendActualMsg(sentActualMsg);

            // enter while loop and wait various kinds of Msg from neighbors
//            while(true){
//                //read socket in
//                readObject = in.readObject();
//                System.out.println("{Client} Receive: " + readObject.getClass().getName());
//                MsgType = readObject.getClass().getName();
//
//                switch (MsgType){
//                    //received ActualMsg is BitFieldMsg
//                    case "Msg.BitFieldMsg":
//                        receivedActualMsg = (BitFieldMsg) readObject;
//
//
//
//                        break;
//                    //received ActualMsg is InterestedMsg
//                    case "Msg.InterestedMsg":
//
//
//                        break;
//                    //received ActualMsg is NotInterestedMsg
//                    case "Msg.NotInterestedMsg":
//
//
//                        break;
//                    //received ActualMsg is RequestMsg
//                    case "Msg.RequestMsg":
//
//
//                        break;
//                    //received ActualMsg is PieceMsg
//                    case "Msg.PieceMsg":
//
//
//                        break;
//                    //received ActualMsg is ChokeMsg
//                    case "Msg.ChokeMsg":
//
//
//                        break;
//                    //received ActualMsg is UnChokeMsg
//                    case "Msg.UnChokeMsg":
//
//
//                        break;
//                    //received ActualMsg is HaveMsg
//                    case "Msg.HaveMsg":
//
//
//                        break;
//                }
//
//
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
     * send Actual Msg include 8 different kinds of Msg via socket
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

