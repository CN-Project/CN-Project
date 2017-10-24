import Msg.*;

import java.time.Period;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class Peer {

    /** Four basic variables get from peerInfo */
    private String peerId;

    private String hostName;

    private String listeningPort;

    private boolean hasFileOrNot;

    private int numOfPiece;

    private boolean[] bitFieldSelf;

    /** used in response bitField, if the server has no pieces at all, it has no need to send bitFieldMsg back */
    private boolean hasPiecesOrNot = false;


    /** Store downloaded file pieces in fileStore[int index][byte[] content]. */
    private byte[][] fileStore;

    /** A peer will keep its own bitfield info and neighbors' bitfield info, and its interested list */
    private Set<String> interestedList = new HashSet<>();

    private Map<String, boolean[]> bitFieldNeighbor = new HashMap<>();

    /** Every peer will keep the server that it has already connected with as < serverPeerID, ClientConnectionThread > */
    private Map<String, Client> clientThreadMap = new HashMap<>();

    public HashMap<String, Integer> getDownloadRateMap() {
        return downloadRateMap;
    }

    private HashMap<String, Integer> downloadRateMap = new HashMap<>();

    public Set<String> getUnchokedList() {
        return unchokedList;
    }

    public void setUnchokedList(Set<String> unchokedList) {
        this.unchokedList = unchokedList;
    }

    private Set<String> unchokedList = new HashSet<>();

    public Set<String> getChokedList() {
        return chokedList;
    }

    public void setChokedList(Set<String> chokedList) {
        this.chokedList = chokedList;
    }

    private Set<String> chokedList = new HashSet<>();

    // Empty constructor
    public Peer(){}


    /**
     * Constructor calculating the number of piece and size of bit field.
     * @param numOfPiece
     */
    public Peer(int numOfPiece) {
        this.numOfPiece = numOfPiece;
        this.bitFieldSelf = new boolean[(int) Math.ceil((double) numOfPiece / 8) * 8];
        this.fileStore = new byte[numOfPiece][];
    }


    /**
     * Constructor used to set specific params
     * @param peerId
     * @param hostName
     * @param listeningPort
     * @param hasFileOrNot
     */
    public Peer(String peerId, String hostName, String listeningPort, boolean hasFileOrNot, int numOfPiece) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFileOrNot = hasFileOrNot;
        this.numOfPiece = numOfPiece;
        this.bitFieldSelf = new boolean[(int) Math.ceil((double) numOfPiece / 8) * 8];
        this.fileStore = new byte[numOfPiece][];
    }

    /**
     * Functions to set params
     */
    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setListeningPort(String listeningPort) {
        this.listeningPort = listeningPort;
    }

    public void setHasFileOrNot(boolean hasFileOrNot) {
        this.hasFileOrNot = hasFileOrNot;
    }

    public void addInterestedList(String peerID){
        this.interestedList.add(peerID);
    }

    public void setFileStore(byte[] content, int index) {
        this.fileStore[index] = content;
    }

    public void setBitFieldSelfAllOne(){
        for(int i = 0; i < this.bitFieldSelf.length; i++){
            this.bitFieldSelf[i] = true;
        }
        this.hasPiecesOrNot = true;
    }

    public void setBitFieldSelfOneBit(int index){
        this.bitFieldSelf[index] = true;
        this.hasPiecesOrNot = true;
    }

    public void addBitFieldNeighbor(String peerId, boolean[] bitFieldNeighbor){
        this.bitFieldNeighbor.put(peerId, bitFieldNeighbor);
    }

    public void updateBitFieldNeighbor(String peerId, int index) {
        this.bitFieldNeighbor.get(peerId)[index] = true;
    }

    public void addClientThreadMap(String peerId, Client client) {
        clientThreadMap.put(peerId, client);
    }

    /**
     * Functions to get params
     */
    public String getPeerId() {
        return this.peerId;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getListeningPort() {
        return this.listeningPort;
    }

    public int getNumOfPiece() {
        return this.numOfPiece;
    }

    public boolean getHasFileOrNot() {
        return this.hasFileOrNot;
    }

    public Set<String> getInterestedList(){
        return this.interestedList;
    }

    public byte[][] getFileStore() {
        return fileStore;
    }

    public boolean[] getBitFieldSelf(){
        return this.bitFieldSelf;
    }

    public boolean[] getBitFieldNeighbor(String peerId){
        return this.bitFieldNeighbor.get(peerId);
    }

    public Map<String, Client> getClientThreadMap() {
        return this.clientThreadMap;
    }

    public boolean isHasPieces(){
        return this.hasPiecesOrNot;
    }

    /***
     * compare the bitfield of curpeer with bitfield of serverpeer to determine whether it have interested piece or not
     * @return (boolean) interested or not interested
     */
    public boolean isInterested(String serverPeerID){
        boolean[] bitFieldSelf = this.bitFieldSelf;
        boolean[] bitFieldNeighbor = this.bitFieldNeighbor.get(serverPeerID);
        for(int i = 0; i < bitFieldSelf.length; i++){
            if(!bitFieldSelf[i] && bitFieldNeighbor[i]){
                return true;
            }
        }
        return false;
    }

    /***
     * convert a byte[] to a int, used in the situation that need to parse index from some Msg(haveMsg etc,.).
     * @return (int) index
     */
    public int byteArray2int(byte[] bytes){
        int val = 0;
        if(bytes.length > 4) throw new RuntimeException("byte[] is too big to convert into int");
        for (int i = 0; i < bytes.length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }
}
