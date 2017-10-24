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

public class Server extends Thread{

    private String linsteningPort;
    private Peer serverPeer;

    public Server(Peer serverPeer) {
        this.linsteningPort = serverPeer.getListeningPort();
        this.serverPeer = serverPeer;
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
                new Handler(socket, this.serverPeer).start();
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

        private Msg.HandShakeMsg sentHandShakeMsg = new Msg.HandShakeMsg();
        private Msg.HandShakeMsg receivedHandShakeMsg = new Msg.HandShakeMsg();
        private ActualMsg sentActualMsg;
        private ActualMsg receivedAcutalMsg;
        private Peer serverPeer;
        private String clientPeerID;
        private String serverPeerID;
        private byte[] indexOfPiece;

        public Handler(Socket socket, Peer serverPeer){
            this.socket = socket;
            this.serverPeer = serverPeer;
            this.serverPeerID = serverPeer.getPeerId();
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
                                System.out.println("{Server} Receive HandShake message " + receivedHandShakeMsg.getHandShakeHeader()
                                        + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));
                                this.clientPeerID = receivedHandShakeMsg.getPeerID();

                                // send HandShakeMsg back
                                sentHandShakeMsg.setPeerID(this.serverPeerID);
                                System.out.println("{Server} Send HandShake message from Server " + this.serverPeerID
                                        + "to Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));
                                this.sendHandShakeMsg(sentHandShakeMsg);
                                break;

                            case "Msg.BitFieldMsg":// receive Bitfield Msg and send its bitfield Msg back to the Client.

                                receivedAcutalMsg = (BitFieldMsg) readObject;
                                System.out.println("{Server} Receive BitFieldMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                // not update bitFieldNeighbor

                                if(serverPeer.isHasPieces()){
                                    //if the server has some pieces
                                    sentActualMsg = new BitFieldMsg(serverPeer.getNumOfPiece());
                                    sentActualMsg.setMessagePayload(sentActualMsg.booleanArray2byteArray(serverPeer.getBitFieldSelf()));
                                    this.sendActualMsg(sentActualMsg);
                                    System.out.println("{Server} Send BitFieldMsg from Server " + this.serverPeerID
                                        + " back to Client " + this.clientPeerID);
                                }
                                break;

                            case "Msg.HaveMsg":
                                receivedAcutalMsg = (HaveMsg) readObject;
                                System.out.println("{Server} Receive HaveMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                indexOfPiece = receivedAcutalMsg.parseIndexFromPieceMsg();
                                // update the bitFieldNeighbor
                                serverPeer.updateBitFieldNeighbor(this.clientPeerID, serverPeer.byteArray2int(indexOfPiece));
                                // send InterestedMsg or NotInterestedMsg
                                if(serverPeer.isInterested(clientPeerID)){
                                    sentActualMsg = new InterestedMsg();
                                    // call the client of this peer to send InterestedMsg
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                }else {
                                    sentActualMsg = new NotInterestedMsg();
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                }
                                break;

                            case "Msg.InterestedMsg":
                                receivedAcutalMsg = (InterestedMsg) readObject;
                                System.out.println("{Server} Receive InterestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                // add ClientPeer into InterestedList
                                serverPeer.addInterestedList(this.clientPeerID);
                                // decide k preferred neighbors
                                /***
                                 *  Zheng Feng Part
                                 */

                                break;

                            case "Msg.NotInterestedMsg":
                                receivedAcutalMsg = (NotInterestedMsg) readObject;
                                System.out.println("{Server} Receive NotInterestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                System.out.println("{Server} No action required. ");
                                break;

                            case "Msg.RequestMsg":
                                receivedAcutalMsg = (RequestMsg) readObject;
                                System.out.println("{Server} Receive RequestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                indexOfPiece = receivedAcutalMsg.parseIndexFromPieceMsg();
                                if(serverPeer.getBitFieldSelf()[serverPeer.byteArray2int(indexOfPiece)]){
                                    // if serverPeer has the requested piece, send pieceMsg to the Client
                                    sentActualMsg = new PieceMsg();
                                    // to be done, set pieceMsg bitField, --------------------------
                                    // which includes 4-bytes index and corresponding piece of document.------------------

                                }
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

}
