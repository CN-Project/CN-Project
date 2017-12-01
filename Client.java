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

    private String clientPeerID;
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
    public boolean isCompleted = false;

    public Client(Peer clientPeer, Peer serverPeer){
        this.clientPeer = clientPeer;
        this.serverPeer = serverPeer;
        this.clientPeerID = clientPeer.getPeerId();
        this.serverPeerID = serverPeer.getPeerId();
        this.serverPeerAddr = serverPeer.getHostName();
        this.hasFileOrNot = clientPeer.getHasFileOrNot();
//        this.listeningPort = serverPeer.getListeningPort();
        this.listeningPort = serverPeer.getListeningPort();
        if(this.hasFileOrNot){
            //set all bits of bitfield 1
            clientPeer.setBitFieldSelfAllOne();
        }
    }

    public synchronized void run(){
        try{
            //initialize Socket of client
            requestSocket = new Socket(serverPeerAddr, Integer.parseInt(this.listeningPort));
            System.out.println("\n" + "{Client} connect to localhost at port " + this.listeningPort);
            //initialize outputstream and inputstream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());

            // 1. send handshake to server
            sentHandShakeMsg.setPeerID(clientPeerID);
            System.out.println("{Client} Send handshake message from Client " + this.clientPeerID + " to Server " + this.serverPeerID) ;
            sendHandShakeMsg(sentHandShakeMsg);
            System.out.println("{Client} Waiting Reply Handshake...");
            //read socket in
            Object readObject = in.readObject();
            System.out.println("{Client} Receive: " + readObject.getClass().getName());
            //if received Msg is handshake
            if(readObject.getClass().getName() == "Msg.HandShakeMsg"){
                receivedHandShakeMsg = (HandShakeMsg) readObject;
                System.out.println("{Client} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader() + "from Server " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                if(receivedHandShakeMsg.getPeerID().equals(this.serverPeerID)){
                    System.out.println("{Client} HandShake success!" + "\n");
                }else {
                    System.out.println("{Client} HandShake FAIL!");
                    System.out.println("{Client} clientPeerID" + receivedHandShakeMsg.getPeerID());
                    System.out.println("{Client} serverID" + this.serverPeerID);

                }
            }

            // 2. send bitfield Msg to server
            sentActualMsg = new BitFieldMsg(clientPeer.getNumOfPiece());
            System.out.println("\n" + "{Client} set the bitfield of BitfieldMsg.");
            sentActualMsg.setMessagePayload(sentActualMsg.booleanArray2byteArray(clientPeer.getBitFieldSelf()));
            sendActualMsg(sentActualMsg);
            System.out.println("{Client} send bitfieldMsg from Client " + this.clientPeerID
                    + " to Server " + this.serverPeerID + "\n");
            isCompleted = true;

            // enter while loop and wait various kinds of Msg from neighbors
//            while(true){
//
//            }


        }
        catch (ConnectException e)
        {
            System.err.println(e);
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
        }
//        finally {
//            //close TCP connection
//            try {
//                in.close();
//                out.close();
//                requestSocket.close();
//            }
//            catch (IOException ioException){
//                ioException.printStackTrace();
//            }
//        }
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
            if(!(requestSocket.isConnected() && !requestSocket.isClosed())){
                requestSocket = new Socket(this.serverPeerAddr, Integer.parseInt(this.listeningPort));
                out = new ObjectOutputStream(requestSocket.getOutputStream());
            }
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
//            if(requestSocket == null | !(requestSocket.isConnected() && !requestSocket.isClosed())){
//                requestSocket = new Socket(this.serverPeerAddr, Integer.parseInt(this.listeningPort));
//                out = new ObjectOutputStream(requestSocket.getOutputStream());
//            }
            out.writeObject(actualMsg);
            out.flush();
            System.out.println(actualMsg);
        }
        catch (IOException ioException){
            try{
                requestSocket = new Socket(this.serverPeerAddr, Integer.parseInt(this.listeningPort));
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                out.writeObject(actualMsg);
                out.flush();
                System.out.println(actualMsg);
            }
            catch(Exception e){
                System.out.println("problem still");
            }
        }
    }
}

