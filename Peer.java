import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class Peer {

    private String peerId;

    private String hostName;

    private String listeningPort;

    private boolean hasFileOrNot;

    private Set<Peer> interestedList = new HashSet<>();

    private boolean[] bitFieldSelf;

    private Map<String, boolean[]> bitFieldNeighbor = new HashMap<>();

    public Peer(){}

    public Peer(int numOfPiece) {
        this.bitFieldSelf = new boolean[(int)Math.ceil(numOfPiece / 8) * 8];
    }

    /**
     * Constructor used to set specific params
     * @param peerId
     * @param hostName
     * @param listeningPort
     * @param hasFileOrNot
     */
    public Peer(String peerId, String hostName, String listeningPort, boolean hasFileOrNot) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFileOrNot = hasFileOrNot;
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

    public void setBitFieldSelfOneBit(int index){
        this.bitFieldSelf[index] = true;
    }

    public void addBitFieldNeighbor(String peerId, boolean[] bitFieldNeighbor){
        this.bitFieldNeighbor.put(peerId, bitFieldNeighbor);

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

    public boolean getHasFileOrNot() {
        return this.hasFileOrNot;
    }

    public Set<Peer> getInterestedList(){
        return this.interestedList;
    }

    public boolean[] getBitFieldSelf(){
        return this.bitFieldSelf;
    }

    public Map<String, boolean[]> getBitFieldNeighbor(){
        return this.bitFieldNeighbor;
    }

}
