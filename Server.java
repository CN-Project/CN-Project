/**
 * Created by xiyaoma on 10/19/17.
 */
import Msg.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Handler;

public class Server extends Thread {

    public Peer curPeer;

    public Server(Peer serverPeer) {
        this.curPeer = serverPeer;
    }

    public static void main(String args[]) throws IOException {
        System.out.println("{Server} The server is running.");
        ServerSocket serverSocket = new ServerSocket(8000);
        int clientNum = 1;
        try{
            while (true){
                new Handler(serverSocket.accept(),clientNum).start();
                System.out.println("{Server} Client " + clientNum + "is connected.");
                clientNum++;
            }
        }
        finally {
            serverSocket.close();
        }
    }

    private static class Handler extends Thread{
        private String MsgType;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int clientNum;
        // Message used for handShake.
        private HandShakeMsg sentHandShakeMsg = new HandShakeMsg();
        private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg();
        // Actual message with different types.
        private ActualMsg sentActualMsg;
        private ActualMsg receivedAcutalMsg;
        private String peerID;

        public Handler(Socket connection, int clientNum){
            this.socket = connection;
            this.clientNum = clientNum;
        }

        public void run(){
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                try {
                    while (true){
                        Object readObject = in.readObject();
                        System.out.println("{Server} Receive: " + readObject.getClass().getName());

                        MsgType = readObject.getClass().getName();
                        switch (MsgType) {
                            case "Msg.HandShakeMsg":
                                receivedHandShakeMsg = (HandShakeMsg) readObject;
                                System.out.println("{Server} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader()
                                        + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                                sentHandShakeMsg.setPeerID(this.peerID);
                                System.out.println("{Server} Send handshake message from Client " + peerID + "to Client "
                                        + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                                sendHandShakeMsg(sentHandShakeMsg);
                                break;
                            case "Msg.BitFieldMsg":
                                receivedAcutalMsg = (BitFieldMsg) readObject;


                        }
//                        //receive the message sent from the socket
//                        message = (String) in.readObject();
//                        System.out.println("Received message: " + message);
//                        MESSAGE = message.toUpperCase();
//                        sendMsg(MESSAGE);

                    }
                }
                catch(ClassNotFoundException classnot){
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendHandShakeMsg(Msg.HandShakeMsg handShakeMsg){
            /**
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

        public void sendMsg(String message){
            /**
             * send message via socket
             */
            try {
                out.writeObject(message);
                out.flush();
                System.out.println("{Server} Send message: " + message);
            }
            catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

}
