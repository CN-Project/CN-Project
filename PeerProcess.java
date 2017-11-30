import Msg.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.RandomAccess;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class PeerProcess {

    private String COMMON_FILE_NAME = "Common.cfg";

    private String PEER_FILE_NAME = "PeerInfo.cfg";

    private Peer inputPeer;

    private Map<String, String> CommonCfgMap;    // Configuration name is used as the key, like "FileSize" etc..

    private Map<String, Peer> peerList;  // PeerID is used as the key.


    public static void main(String[] args) {

        PeerProcess process = new PeerProcess();
//        CfgGenerator cfgGenerator = new CfgGenerator();
//        cfgGenerator.run();
//        System.out.println("Successfully generate configuration files......" + "\n");


        /** Get common configuration info and store in a map*/
        process.getCommonCfg(process.COMMON_FILE_NAME);
        int fileSize = Integer.parseInt(process.CommonCfgMap.get("FileSize"));
        int pieceSize = Integer.parseInt(process.CommonCfgMap.get("PieceSize"));
        int numOfPiece = (int) Math.ceil((double) fileSize / (double) pieceSize);


        /** Get peer information and store in a map peerList*/
        process.getPeerCfg(process.PEER_FILE_NAME, numOfPiece);

        /** Create connection to former peers and self server for this input peer. */
        System.out.println("Start to join input peer into whole network......" + "\n");
        String inputPeerID = args[0];
        process.inputPeer = process.peerList.get(inputPeerID);
        process.inputPeer.setPeerList(process.peerList);

        // Connect this peer to all former peers, this peer set as client
        process.setupConnection(process.peerList);
        // Setup this peer as a server
        process.createServer();
        // Create peer subdirectory, if this peer is the first peer, split file into pieces.
        process.fileHandling();

        // Run preferred & optimistic neighbors update
        Thread preferredNB = new Thread(new PreferredNBUpdate(process.inputPeer, 3, 6));
        preferredNB.start();

//        Thread optimisticNB = new Thread(new OptimisticNBUpdate(process.inputPeer, 6));
//        optimisticNB.start();

    }

    /**
     * Function used to read Common.cfg and get all configuration information
     * @param filename
     * @return Map of common configuration
     */
    public void getCommonCfg(String filename) {

        CommonCfgMap = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                String[] content = line.trim().split(" ");
                CommonCfgMap.put(content[0], content[1]);
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException fne) {
            System.out.println("Cannot find this file, please use CfgGenerator first.");
        } catch (IOException ie) {
            System.out.println("Cannot read this file, please check the file.");
        }
    }


    /**
     * Function used to read peerInfo.cfg and store all peer information in a map, set peerID as the key.
     * @param filename
     * @param numOfPiece
     */
    public void getPeerCfg(String filename, int numOfPiece) {

        peerList = new ConcurrentHashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                String[] content = line.trim().split(" ");
                boolean hasFileOrNot = content[3].equals("1");
                Peer peer = new Peer(content[0], content[1], content[2], hasFileOrNot, numOfPiece);
                peerList.put(content[0], peer);
                line = reader.readLine();
            }
            reader.close();
        } catch (FileNotFoundException fne) {
            System.out.println("Cannot find this file, please use CfgGenerator first");
        } catch (IOException ie) {
            System.out.println("Cannot read this file, please check the file");
        }
    }

    /** Get the input peer of this peer process. */
    public Peer getInputPeer() {
        return this.inputPeer;
    }


    /**
     * Setup connection to all former peers of this peer.
     * @param peerList
     */
    public void setupConnection(Map<String, Peer> peerList) {
        String peerID = this.inputPeer.getPeerId();
        for (Map.Entry<String, Peer> entry : peerList.entrySet()) {
            if (entry.getKey().equals(peerID)) {
                continue;
            }
            String serverPeerID = entry.getKey();
            Peer serverPeer = entry.getValue();
            if (Integer.parseInt(serverPeerID) < Integer.parseInt(peerID)) {
                System.out.println("Set up new connection from " + peerID + " to " + serverPeerID);
                Client newConnection = new Client(this.inputPeer, serverPeer);
                this.inputPeer.addClientThreadMap(serverPeerID, newConnection);
                this.inputPeer.updateConnetedList(serverPeerID);
                newConnection.start();
            }
        }
    }


    /** Creat a server thread for this input peer, keep its socket running. */
    public void createServer() {
        new Thread(new Server(this.inputPeer)).start();

    }


    /**
     * Create file handler for this input peer, if this peer contains all file, split file into pieces,
     * and set this peer's bitFiled to all one.
     */
    public void fileHandling() {

        String peerDirectory = "peer_" + this.inputPeer.getPeerId();
        File peerDirectoryFile = new File(peerDirectory);

        if (this.inputPeer.getHasFileOrNot()) {
            // If this peer is the first peer, make it a "Pieces" directory, split file and store pieces in it.

            String fileName = this.CommonCfgMap.get("FileName");
            File completeFile = new File(peerDirectoryFile.getAbsolutePath() + "/" + fileName);

            int fileSize = Integer.parseInt(this.CommonCfgMap.get("FileSize"));
            int pieceSize = Integer.parseInt(this.CommonCfgMap.get("PieceSize"));
            int numOfPiece = (int) Math.ceil((double) fileSize / (double) pieceSize);
            long lon = completeFile.length() / (long) numOfPiece + 1L;
            try {
                RandomAccessFile raf = new RandomAccessFile(completeFile, "r");
                byte[] bytes = new byte[1024];
                int len = -1;
                for (int i = 0; i < numOfPiece; i++) {
                    File pieceFile = new File(peerDirectoryFile.getAbsolutePath() + "/" + i + ".dat");
                    RandomAccessFile rafout = new RandomAccessFile(pieceFile, "rw");

                    while ((len = raf.read(bytes)) != -1) {
                        rafout.write(bytes, 0, len);
                        if (rafout.length() > lon) {
                            break;
                        }
                    }
                    rafout.close();
                }
                raf.close();
            } catch (FileNotFoundException fne) {
                System.out.println("Cannot find the complete file, please check it first");
                fne.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            this.inputPeer.setBitFieldSelfAllOne();
        } else {
            // If this peer is not the first peer, make a directory to store files.
            peerDirectoryFile.mkdir();
        }
    }
}
