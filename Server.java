/**
 * Created by xiyaoma on 10/19/17.
 */
import Msg.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.*;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class Server extends Thread{

    private String linsteningPort;
    private Peer serverPeer;

//    private HashSet<String> connectedList = new HashSet<>();
    private Map<String, Peer> peerList = new ConcurrentHashMap<>();

    public Server(Peer serverPeer) {
        this.linsteningPort = serverPeer.getListeningPort();
        this.serverPeer = serverPeer;
        this.peerList = serverPeer.getPeerList();
    }
    public synchronized void run()
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
        private ActualMsg receivedActualMsg;
        private Peer serverPeer;
        private String clientPeerID;
        private String serverPeerID;
        private byte[] indexOfPiece;
        private boolean isChoked = true;
        private boolean HandShakeReceiver = false;



        public Handler(Socket socket, Peer serverPeer){
            this.socket = socket;
            this.serverPeer = serverPeer;
            this.serverPeerID = serverPeer.getPeerId();
        }

        public synchronized void run(){
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
                        SimpleDateFormat dateFormat = new SimpleDateFormat();
                        String time = dateFormat.format(new Date());
                        String logContent;
                        switch (MsgType) {

                            case "Msg.HandShakeMsg":

                                receivedHandShakeMsg = (HandShakeMsg) readObject;
                                System.out.println("\n" + "{Server} Receive HandShake message " + receivedHandShakeMsg.getHandShakeHeader()
                                        + "from Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()));
                                this.clientPeerID = receivedHandShakeMsg.getPeerID();

                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " is connected from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);
                                // send HandShakeMsg back
                                sentHandShakeMsg.setPeerID(this.serverPeerID);
                                System.out.println("{Server} Send HandShake message from Server " + this.serverPeerID
                                        + " to Client " + Integer.parseInt(receivedHandShakeMsg.getPeerID()) + "\n");
                                this.sendHandShakeMsg(sentHandShakeMsg);
                                this.HandShakeReceiver = true;
                                break;

                            case "Msg.BitFieldMsg":
                                receivedActualMsg = (BitFieldMsg) readObject;
                                System.out.println("\n" + "{Server} Receive BitFieldMsg from Client " + this.clientPeerID
                                            + " to Server " + this.serverPeerID);
                                //add the bitField of clientPeer into the bitFieldNeighbors
                                serverPeer.addBitFieldNeighbor(this.clientPeerID, receivedActualMsg.byteArray2booleanArray(receivedActualMsg.getMessagePayload()));
                                // create a client thread of current peer, to connect serverPeer
                                if (!this.serverPeer.isInConnectedList(this.clientPeerID)) {
                                    this.serverPeer.updateConnetedList(this.clientPeerID);
                                    Client newClient = new Client(this.serverPeer, this.serverPeer.getPeerList().get(this.clientPeerID));
                                    this.serverPeer.addClientThreadMap(this.clientPeerID, newClient);
                                    newClient.start();
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " make a connection to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);

                                    while (!newClient.isCompleted) {
                                        try {
                                            sleep(10);
                                        } catch (InterruptedException ire) {
                                            ire.printStackTrace();
                                        }
                                    }
                                }

                                if(serverPeer.isInterested(this.clientPeerID)){
                                    sentActualMsg = new InterestedMsg();
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " send InterestedMsg to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("{Server} Send InterestedMsg from Client " + this.serverPeerID
                                        + " back to Server " + this.clientPeerID + "\n");
                                }else {
                                    sentActualMsg = new NotInterestedMsg();
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " send NotInterestedMsg to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("{Server} Send NotInterestedMsg from Client " + this.serverPeerID
                                        + " back to Server " + this.clientPeerID + "\n");
                                }
                                break;

                            case "Msg.HaveMsg":
                                receivedActualMsg = (HaveMsg) readObject;
                                System.out.println("\n" + "{Server} Receive HaveMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " received HaveMsg from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);

                                indexOfPiece = receivedActualMsg.parseIndexFromPieceMsg();
                                // update the bitFieldNeighbor
                                serverPeer.updateBitFieldNeighbor(this.clientPeerID, serverPeer.byteArray2int(indexOfPiece));
                                // send InterestedMsg or NotInterestedMsg
                                if(serverPeer.isInterested(this.clientPeerID)){
                                    sentActualMsg = new InterestedMsg();
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " send InterestedMsg to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("{Server} Send InterestedMsg from Client " + this.serverPeerID
                                        + " back to Server " + this.clientPeerID + "\n");
                                }else {
                                    sentActualMsg = new NotInterestedMsg();
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " send NotInterestedMsg to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("{Server} Send NotInterestedMsg from Client " + this.serverPeerID
                                        + " back to Server " + this.clientPeerID + "\n");
                                }
                                break;

                            case "Msg.InterestedMsg":
                                receivedActualMsg = (InterestedMsg) readObject;
                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " received InterestedMsg from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);
                                System.out.println("\n" + "{Server} Receive InterestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                // add ClientPeer into InterestedList
                                serverPeer.addInterestedList(this.clientPeerID);
                                break;

                            case "Msg.NotInterestedMsg":
                                receivedActualMsg = (NotInterestedMsg) readObject;
                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " received NotInterestedMsg from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);
                                System.out.println("\n" + "{Server} Receive NotInterestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
//                                System.out.println("{Server} No action required. " + "\n");
                                serverPeer.removeFromInterestedList(this.clientPeerID);
                                break;

                            case "Msg.RequestMsg":
                                receivedActualMsg = (RequestMsg) readObject;
                                System.out.println("\n" + "{Server} Receive RequestedMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " received RequestedMsg from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);

                                indexOfPiece = receivedActualMsg.parseIndexFromPieceMsg();
                                if(serverPeer.getBitFieldSelf()[serverPeer.byteArray2int(indexOfPiece)]){
                                    // if serverPeer has the requested piece, send pieceMsg to the Client
                                    sentActualMsg = new PieceMsg();
                                    // to be done, set pieceMsg bitField, --------------------------
                                    int index = this.serverPeer.byteArray2int(indexOfPiece);
                                    if(this.serverPeer.getReceivedPieceCount() > 305){

                                    }
                                    byte[] content = this.serverPeer.readFile2ByteArray("peer_" + this.serverPeerID + "/" + index + ".dat");
                                    byte[] payload = this.serverPeer.concatByteArray(indexOfPiece, content);
                                    sentActualMsg.setMessagePayload(payload);

                                    // which includes 4-bytes index and corresponding piece of document.------------------
                                    serverPeer.getClientThreadMap().get(clientPeerID).sendActualMsg(sentActualMsg);
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " send PieceMsg to Peer " + this.clientPeerID + ".";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("{Server} Send PieceMsg from Client " + this.serverPeerID
                                            + " back to Server " + this.clientPeerID + "\n");

                                }
                                break;

                            case "Msg.PieceMsg":

                                receivedActualMsg = (PieceMsg) readObject;
                                //write log
                                logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " received PieceMsg from Peer " + this.clientPeerID + ".";
                                this.serverPeer.writeLog(logContent);
                                System.out.println("\n" + "{Server} Receive PieceMsg from Client " + this.clientPeerID + " to Client" + this.serverPeerID);

                                //update the bitField of the clientPeer in the bitFieldSelf
                                indexOfPiece = receivedActualMsg.parseIndexFromPieceMsg();
                                serverPeer.setBitFieldSelfOneBit(serverPeer.byteArray2int(indexOfPiece));
                                String fileName = "peer_" + this.serverPeerID + "/" + serverPeer.byteArray2int(indexOfPiece) + ".dat";
                                this.serverPeer.storeByteArray2File(Arrays.copyOfRange(receivedActualMsg.getMessagePayload(), 4, receivedActualMsg.getMessagePayload().length), fileName);
//                                this.serverPeer.addReceivedPieceCount();
                                if(this.serverPeer.hasCompleteFileOrNot()){
                                    this.serverPeer.setHasFileOrNot(true);
                                    mergeFiles();
                                    //write log
                                    logContent = "[ " + time + " ]: Peer " + this.serverPeerID + " has downloaded the complete file.";
                                    this.serverPeer.writeLog(logContent);
                                    System.out.println("\nPeer " + this.serverPeer.getPeerId() + " has received all file pieces, successfully merge into a complete filed!!\n");
                                }
                                // 1. check whether it is unchoked, if so, request another piece
                                if(!this.isChoked){
                                    int index = this.serverPeer.getAPieceIndex(this.clientPeerID);
                                    if(index != -1) {
                                        this.serverPeer.getClientThreadMap().get(this.clientPeerID).sendActualMsg(
                                                new RequestMsg(ByteBuffer.allocate(4).putInt(index).array()));
                                    }
                                }

                                // set HaveMsg payload field
                                sentActualMsg = new HaveMsg();
                                sentActualMsg.setMessagePayload(indexOfPiece);

                                // 2. send HaveMsg to all neighbors
                                for(Map.Entry<String, Client> entry : serverPeer.getClientThreadMap().entrySet()){
                                    Client client = entry.getValue();
                                    String destinationPeerID = entry.getKey();
                                    try {
                                        client.sendActualMsg(sentActualMsg);
                                    }catch (Exception e){
                                        System.out.println(e);
                                    }

                                    System.out.println("{Client} Send HaveMsg from Client " + this.serverPeerID
                                            + " to Server " + destinationPeerID + "\n");
                                }
                                // 3. update download rate map
                                this.serverPeer.addDownloadRateMap(this.clientPeerID);
                                break;

                            case "Msg.ChokeMsg":
                                receivedActualMsg = (ChokeMsg) readObject;
                                System.out.println("{Server} Receive ChokeMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                this.isChoked = true;
                                if (this.serverPeer.getUnchokedList().contains(this.clientPeerID)) {
                                    this.serverPeer.removeFromUnchokedList(this.clientPeerID);
                                }
                                break;
                            case "Msg.UnChokeMsg":
                                receivedActualMsg = (UnChokeMsg) readObject;
                                System.out.println("{Server} Receive UnChokeMsg from Client " + this.clientPeerID
                                        + " to Server " + this.serverPeerID);
                                if(isChoked) {
                                    isChoked = false;
                                    int index = this.serverPeer.getAPieceIndex(this.clientPeerID);
                                    if(index != -1) {
                                        this.serverPeer.getClientThreadMap().get(this.clientPeerID).sendActualMsg(
                                                new RequestMsg(ByteBuffer.allocate(4).putInt(index).array()));
                                        this.serverPeer.addUnchokedList(this.clientPeerID);
                                    }

                                }
                                break;
                        }
                    }
                }
                catch(ClassNotFoundException classnot){
                    System.err.println("[Server] Data received in unknown format");
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("[Server] Disconnect with Client");
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


        /**
         * Function used to merge files, when this peer has received all pieces, use mergeFile to merge into the complete one.
         */
        public void mergeFiles() {
            if (this.serverPeer.getPeerId().equals("1001")) {
                return;
            }

            File peerDirectoryFile = new File("peer_" + this.serverPeer.getPeerId());
            String path = peerDirectoryFile.getAbsolutePath();
            File targetFile = new File(path + "/TheFile.dat");


            try {
                RandomAccessFile target = new RandomAccessFile(targetFile, "rw");
                for (int i = 0; i < serverPeer.getNumOfPiece(); i++) {
                    File file = new File(path + "/" + i + ".dat");
                    RandomAccessFile pieceFile = new RandomAccessFile(file, "r");
                    byte[] bytes = new byte[1024];
                    int len = -1;

                    while ((len = pieceFile.read(bytes)) != -1) {
                        target.write(bytes, 0, len);
                    }
                    pieceFile.close();
                }
                target.close();
            } catch (FileNotFoundException fne) {
                fne.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        /***
         * block sendActualMsg, since every time send ActualMsg, need we to send it through the client of this Peer!!!
         */
//        /***
//         * send Actual Msg include 8 different kinds of Msgs via socket
//         * @param actualMsg
//         */
//        public void sendActualMsg(ActualMsg actualMsg){
//            try {
//                out.writeObject(actualMsg);
//                out.flush();
//            }
//            catch (IOException ioException){
//                ioException.printStackTrace();
//            }
//        }

    }




}
