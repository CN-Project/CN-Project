/**
 * Created by xiyaoma on 10/19/17.
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Handler;

public class Server {

    private static final int port = 8000;

    public static void main(String args[]) throws IOException {
        System.out.println("{Server} The server is running.");
        ServerSocket serverSocket = new ServerSocket(port);
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
        private String message;
        private String MESSAGE;
        private Socket connection;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int clientNum;
        private HandShakeMsg sentHandShakeMsg = new HandShakeMsg();
        private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg();
        private String peerID = "2";
        private String serverPeerID = "1";

        public Handler(Socket connection, int clientNum){
            this.connection = connection;
            this.clientNum = clientNum;
        }

        public void run(){
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    while (true){
                        Object readObject = in.readObject();
                        System.out.println("{Server} Receive: " + readObject.getClass().getName());

                        if(readObject.getClass().getName() == "HandShakeMsg"){

                            receivedHandShakeMsg = (HandShakeMsg) readObject;
                            System.out.println("{Server} Receive handshake message " + receivedHandShakeMsg.getHandShakeHeader() + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));

                            sentHandShakeMsg.setPeerID(this.peerID);
                            System.out.println("{Server} Send handshake message from Client " + peerID + "to Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));
                            sendHandShakeMsg(sentHandShakeMsg);

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

        public void sendMsg(String message){
            /***
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
