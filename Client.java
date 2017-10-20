/**
 * Created by xiyaoma on 10/19/17.
 */
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
    Socket requestSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    String message;
    String MEAASGE;
    String peerID = "1";
    String serverPeerID = "2";
    String serverPeerAddr = "localhost";
    int serverPeerPort = 8000;
    HandShakeMsg sentHandShakeMsg = new HandShakeMsg(); // HandShake Msg send to the server
    HandShakeMsg receivedHandShakeMsg = new HandShakeMsg(); // HandShake Msg received from the server


    public Client(){}

    public void run(){
        try{
            //initialize Socket of client
            requestSocket = new Socket(serverPeerAddr, serverPeerPort);
            System.out.println("{Client} connect to localhost at port 8000");
            //initialize outputstream and inputstream
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
            //set peerID of the current peer
            sentHandShakeMsg.setPeerID(peerID);
            System.out.println("{Client} Send handshake message from Client " + peerID + " to Client " + this.serverPeerID) ;
            sendHandShakeMsg(sentHandShakeMsg);

            Object readObject = in.readObject();
            System.out.println("{Client} Receive: " + readObject.getClass().getName());

            if(readObject.getClass().getName() == "HandShakeMsg"){
                receivedHandShakeMsg = (HandShakeMsg) readObject;
                System.out.println("{Client} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader() + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                if(receivedHandShakeMsg.getPeerID() == serverPeerID){
                    System.out.println("{Client} HandShake succeed! ");
                }
            }

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
    public static void main(String args[]){
        Client client = new Client();
        client.run();
    }


    public void sendMsg(String message){
        /***
         * send message via socket
         */
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("{Client} Send message: " + message);
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void sendHandShakeMsg(HandShakeMsg handShakeMsg){
        /***
         * send HandShakeMessage via socket
         */
        try {
            out.writeObject(handShakeMsg);
            out.flush();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}

