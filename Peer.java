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

    /** A map store all other peers' bitfield array. */
    private Map<String, boolean[]> bitFieldNeighbor = new HashMap<>();

    /** Every peer will keep the server that it has already connected with as < serverPeerID, ClientConnectionThread > */
    private Map<String, Client> clientThreadMap = new HashMap<>();

    /** Store the download rate for every peer, used to decided preferred neighbor. */
    private HashMap<String, Integer> downloadRateMap = new HashMap<>();

    /** Every peer will keep the unchoked list for preferred neighbor. */
    private Set<String> unchokedList = new HashSet<>();

    /** List that stores all choked peers. */
    private Set<String> chokedList = new HashSet<>();

    /** A peer will keep its own bitfield info and neighbors' bitfield info, and its interested list */
    private Set<String> interestedList = new HashSet<>();

    /** all peers provided by the cfg file*/
    private HashMap<String, Peer> peerList = new HashMap<>();

    /** all peers connecting to the current peer*/
    private HashSet<String> connectedList = new HashSet<>();

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

    public void setUnchokedList(Set<String> unchokedList) {
        this.unchokedList = unchokedList;
    }

    public void setChokedList(Set<String> chokedList) {
        this.chokedList = chokedList;
    }

    public void setPeerList(HashMap<String, Peer> peerList)
    {
        for (Map.Entry<String, Peer> entry : peerList.entrySet()) {
            String peerID = entry.getKey();
            Peer currentPeer = entry.getValue();
            this.peerList.put(peerID, currentPeer);
        }
    }

    public void addUnchokedList(String peerId) {
        unchokedList.add(peerId);
    }

    public void removeFromUnchokedList(String id) {
        unchokedList.remove(id);
    }

    public void updateConnetedList(String peerId){
        this.connectedList.add(peerId);
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

    public HashMap<String, Peer> getPeerList() { return this.peerList; }

    public HashMap<String, Integer> getDownloadRateMap() {
        return downloadRateMap;
    }

    public void addDownloadRateMap(String peerId){
        if(downloadRateMap.containsKey(peerId)){
            downloadRateMap.put(peerId, downloadRateMap.get(peerId)+1);
        }
        else{
            downloadRateMap.put(peerId, new Integer(1));
        }
    }

    public Set<String> getUnchokedList() {
        return unchokedList;
    }

    public Set<String> getChokedList() {
        return chokedList;
    }

    public boolean isInConnectedList(String peerId){
        return this.connectedList.contains(peerId);
    }

    public boolean isHasPieces(){
        return this.hasPiecesOrNot;
    }

    /***
     * compare the bitfield of curpeer with bitfield of serverpeer to determine whether it have interested piece or not
     * @return (boolean) interested or not interested
     */
    public boolean isInterested(String peerId){
        boolean[] bitFieldSelf = this.bitFieldSelf;
        boolean[] bitFieldNeighbor = this.bitFieldNeighbor.get(peerId);
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

    /***
     *  choose a piece current peer need to download from other peer
     * @param peerId
     * @return piece index, return -1 if other peer don't have it
     */
    public int getAPieceIndex(String peerId){
        boolean[] bitFieldSelf = this.bitFieldSelf;
        boolean[] bitFieldNeighbor = this.bitFieldNeighbor.get(peerId);
        for(int i = 0; i < bitFieldSelf.length; i++){
            if(!bitFieldSelf[i] && bitFieldNeighbor[i]){
                return i;
            }
        }
        return -1;
    }
}
