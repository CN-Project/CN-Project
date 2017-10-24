import Msg.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

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

        /** Get common configuration info and store in a map*/
        process.getCommonCfg(process.COMMON_FILE_NAME);
        int fileSize = Integer.parseInt(process.CommonCfgMap.get("FileSize"));
        int pieceSize = Integer.parseInt(process.CommonCfgMap.get("PieceSize"));
        int numOfPiece = (int) Math.ceil((double) fileSize / (double) pieceSize);


        /** Get peer information and store in a map peerList*/
        process.getPeerCfg(process.PEER_FILE_NAME, numOfPiece);

        /** Create connection to former peers and self server for this input peer. */
        String inputPeerID = args[0];
        process.inputPeer = process.peerList.get(inputPeerID);

        System.out.println(process.inputPeer.getFileStore().length);
        process.setupConnection(process.peerList);
        process.createServer();

        /** Create the file directory, if the input peer contains all file, split it into pieces. */
        process.fileHandling();

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

        peerList = new HashMap<>();

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
     * Setup connection to the former peers of this peer.
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
                Client newConnection = new Client(this.inputPeer, serverPeer);
                this.inputPeer.addConnectedServerMap(serverPeerID, newConnection);
                newConnection.start();
            }
        }
    }


    /** Creat a server thread for this input peer, keep its socket running. */
    public void createServer() {
        new Thread(new Server(this.inputPeer)).start();
    }


    /**
     * Create file handler for this input peer, if this peer contains all file, split file into pieces.
     */
    public void fileHandling() {

        String peerDirectory = "peer_" + this.inputPeer.getPeerId();
        File peerDirectoryFile = new File(peerDirectory);

        if (this.inputPeer.getHasFileOrNot()) {
            String fileName = this.CommonCfgMap.get("FileName");
            File completeFile = new File(peerDirectoryFile.getAbsolutePath() + "/" + fileName);
            try {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(completeFile));
                // Cut into pieces
            } catch (FileNotFoundException fne) {
                System.out.println("Cannot find the complete file, please check it first");
            }
            this.inputPeer.setBitFieldSelfAllOne();
        } else {
            peerDirectoryFile.mkdir();
        }
    }


}
