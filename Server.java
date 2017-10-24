/**
 * Created by xiyaoma on 10/19/17.
 */
import Msg.*;
import Msg.ActualMsg;
import Msg.BitFieldMsg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Handler;

public class Server extends Thread{

    private String linsteningPort;
    private Peer curPeer;

    public Server(Peer curPeer) {
        this.linsteningPort = curPeer.getListeningPort();
    }
    public void run()
    {
        try
        {
            System.out.println("The server is running.");
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(this.linsteningPort));
            while(true)
            {
                Socket socket = serverSocket.accept();
                new Handler(socket, this.curPeer).start();
                System.out.println("Client is connected!");
            }
        }
        catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    private static class Handler extends Thread{
        private String MsgType;
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int clientNum;

        private Msg.HandShakeMsg sentHandShakeMsg = new Msg.HandShakeMsg();
        private Msg.HandShakeMsg receivedHandShakeMsg = new Msg.HandShakeMsg();
        private ActualMsg sentActualMsg;
        private ActualMsg receivedAcutalMsg;
        private String peerId;
        private Peer curPeer;

        public Handler(Socket socket, Peer curPeer){
            this.socket = socket;
            this.curPeer = curPeer;
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

                                sentHandShakeMsg.setPeerID(this.peerId);
                                System.out.println("{Server} Send handshake message from Client " + peerId + "to Client "
                                        + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                                sendHandShakeMsg(sentHandShakeMsg);
                                break;
                            case "Msg.BitFieldMsg":// receive Bitfield Msg and send its bitfield Msg back to the Client.

                                receivedAcutalMsg = (BitFieldMsg) readObject;
                                
                                break;

                            case "Msg.HaveMsg":


                                break;

                            case "Msg.InterestedMsg":

                                break;

                            case "Msg.NotInterestedMsg":

                                break;

                            case "Msg.RequestMsg":

                                break;
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
