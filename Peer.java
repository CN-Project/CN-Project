import Msg.*;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class Peer {

    /** Four basic variables get from peerInfo */
    private String peerId;

    private int receivedPieceCount = 0;

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
    private Map<String, boolean[]> bitFieldNeighbor = new ConcurrentHashMap<>();

    /** Every peer will keep the server that it has already connected with as < serverPeerID, ClientConnectionThread > */
    private Map<String, Client> clientThreadMap = new ConcurrentHashMap<>();

    /** Store the download rate for every peer, used to decided preferred neighbor. */
    private Map<String, Integer> downloadRateMap = new ConcurrentHashMap<>();

    /** Every peer will keep the unchoked list for preferred neighbor. */
    private Set<String> unchokedList = Collections.synchronizedSet(new HashSet<>());

    /** List that stores all choked peers. */
    private Set<String> chokedList = Collections.synchronizedSet(new HashSet<>());

    /** A peer will keep its own bitfield info and neighbors' bitfield info, and its interested list */
    private Set<String> interestedList = Collections.synchronizedSet(new HashSet<>());

    /** all peers provided by the cfg file*/
    private Map<String, Peer> peerList = new ConcurrentHashMap<>();

    /** all peers connecting to the current peer*/
    private Set<String> connectedList = Collections.synchronizedSet(new HashSet<>());

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

    public void removeFromInterestedList(String peerID){
        this.interestedList.remove(peerID);
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

    public void setPeerList(Map<String, Peer> peerList)
    {
        for (Map.Entry<String, Peer> entry : peerList.entrySet()) {
            String peerID = entry.getKey();
            Peer currentPeer = entry.getValue();
            this.peerList.put(peerID, currentPeer);
        }
    }

    public void addReceivedPieceCount(){
        this.receivedPieceCount++;
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

    public Map<String, Peer> getPeerList() { return this.peerList; }

    public Map<String, Integer> getDownloadRateMap() {
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

    public int getReceivedPieceCount(){
        return this.receivedPieceCount;
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
        for(int i = 0; i < this.numOfPiece; i++){
            if(!bitFieldSelf[i] && bitFieldNeighbor[i]){
                return true;
            }
        }
        return false;
    }
    public boolean hasCompleteFileOrNot(){
        for(int i = 0; i < this.numOfPiece; i++){
            if(!this.bitFieldSelf[i]){
                return false;
            }
        }
        return true;
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
        for(int i = 0; i < this.numOfPiece; i++){
            if(!bitFieldSelf[i] && bitFieldNeighbor[i]){
                return i;
            }
        }
        return -1;
    }
    /***
     * read a file and convert it to a byte array
     * @param filename
     * @return byte array
     * @throws IOException
     */
    public byte[] readFile2ByteArray(String filename)throws IOException{

        FileChannel fc = null;
        try{
            fc = new RandomAccessFile(filename,"r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
//            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int)fc.size()];
            if (byteBuffer.remaining() > 0) {
//              System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        }catch (IOException e) {
            e.printStackTrace();
            throw e;
        }finally{
            try{
                fc.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * read a byte array and store the content into file
     * @param bytes
     * @param outputFile
     * @return
     */
    public File storeByteArray2File(byte[] bytes, String outputFile) {
        File ret = null;
        BufferedOutputStream stream = null;
        try {
            ret = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(ret);
            stream = new BufferedOutputStream(fstream);
            stream.write(bytes);
//            stream.close();
        } catch (Exception e) {
            // log.error("helper:get file from byte process error!");
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // log.error("helper:get file from byte process error!");
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
    public byte[] concatByteArray(byte[] a, byte[] b){

        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
