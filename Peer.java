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

    private Map<String, boolean[]> bitFieldNeighbor;

    public Peer(){}

    public Peer(int bitFieldSize) {
        this.bitFieldSelf = new boolean[bitFieldSize];
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
}
