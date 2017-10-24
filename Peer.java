import Msg.*;
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

    /** Store downloaded file pieces in fileStore[int index][byte[] content]. */
    private byte[][] fileStore;

    /** A peer will keep its own bitfield info and neighbors' bitfield info, and its interested list */
    private Set<Peer> interestedList = new HashSet<>();

    private Map<String, boolean[]> bitFieldNeighbor = new HashMap<>();

    /** Every peer will keep the server that it has already connected with as < serverPeerID, ClientConnectionThread > */
    private Map<String, Client> connectedServerMap = new HashMap<>();


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

    public void addInterestedList(Peer peer){
        this.interestedList.add(peer);
    }

    public void setBitFieldSelfAllOne(){
        for(int i = 0; i < this.bitFieldSelf.length; i++){
            this.bitFieldSelf[i] = true;
        }
    }

    public void setFileStore(byte[] content, int index) {
        this.fileStore[index] = content;
    }

    public void setBitFieldSelfOneBit(int index){
        this.bitFieldSelf[index] = true;
    }

    public void addBitFieldNeighbor(String peerId, boolean[] bitFieldNeighbor){
        this.bitFieldNeighbor.put(peerId, bitFieldNeighbor);
    }

    public void updateBitFieldNeighbor(String peerId, int index) {
        this.bitFieldNeighbor.get(peerId)[index] = true;
    }

    public void addConnectedServerMap(String peerId, Client client) {
        connectedServerMap.put(peerId, client);
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

    public Set<Peer> getInterestedList(){
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

    public Map<String, Client> getConnectedServerMap() {
        return this.connectedServerMap;
    }
}
