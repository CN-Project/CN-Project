import Msg.ChokeMsg;
import Msg.UnChokeMsg;

import java.util.*;

public class PreferedNBUpdate implements Runnable {
    private int preferredNBNum;
    private Peer peer = null;
    private int intervalP;
    public PreferedNBUpdate(Peer peer, int preferredNBNum, int interval){
        this.preferredNBNum = preferredNBNum;
        this.peer = peer;
        this.intervalP = interval;
    }

    public void run() {
        while(true){
            long t1 = new Date().getTime();
            updatePreferNB();
            long t2 = new Date().getTime();
            long waitTime = intervalP*1000 - (t2 - t1);
            try {
                if(waitTime>0)
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePreferNB(){
        HashMap<String, Integer> downloadMap = peer.getDownloadRateMap();
        HashMap<String, Integer> interestedNBMap = new HashMap<>();
        Set<String> interestedList = peer.getInterestedList();
        Set<String> unchokedList = new HashSet<>();
        Set<String> chokedList = new HashSet<>();
        Map<String, Client> neighborClients = peer.getClientThreadMap();

        for (String peerid: interestedList){
            if(downloadMap.containsKey(peerid)){
                interestedNBMap.put(peerid, downloadMap.get(peerid));
            }
            else{
                interestedNBMap.put(peerid, 0);
            }
        }

        List<Map.Entry<String, Integer>> infoIds = new ArrayList<Map.Entry<String, Integer>>(interestedNBMap.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey().toString());
            }
        });
        for (int i = 0; i < infoIds.size(); i++) {
            String peer = infoIds.get(i).getKey();
            if(i < preferredNBNum){
                unchokedList.add(peer);
            }
            else{
                chokedList.add(peer);
            }
        }

        Set<String> currentUnchokedList = peer.getUnchokedList();
        for(String currentUnchoked:currentUnchokedList){
            if(!unchokedList.contains(currentUnchoked)){
                chokedList.add(currentUnchoked);
            }
        }

        for(String unchokePeer:unchokedList){
            if(!currentUnchokedList.contains(unchokePeer)){
                neighborClients.get(unchokePeer).sendActualMsg(new UnChokeMsg());
            }
        }

        Set<String> currentChokedList = peer.getChokedList();
        for(String chokePeer:chokedList){
            if(!currentChokedList.contains(chokePeer)){
                neighborClients.get(chokePeer).sendActualMsg(new ChokeMsg());
            }
        }

        peer.setChokedList(chokedList);
        peer.setUnchokedList(unchokedList);
    }
}
