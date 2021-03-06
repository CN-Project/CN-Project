import Msg.ChokeMsg;
import Msg.UnChokeMsg;

import java.text.SimpleDateFormat;
import java.util.*;

public class PreferredNBUpdate implements Runnable {
    private int preferredNBNum;
    private Peer peer = null;
    private int intervalP;
    public PreferredNBUpdate(Peer peer, int preferredNBNum, int interval){
        this.preferredNBNum = preferredNBNum;
        this.peer = peer;
        this.intervalP = interval;
    }

    public void run() {
        while(true){
            long t1 = new Date().getTime();
            updatePreferedNB();
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

    private synchronized void updatePreferedNB(){
        Map<String, Integer> downloadMap = peer.getDownloadRateMap();
        Map<String, Integer> interestedNBMap = new HashMap<>();
        Set<String> interestedList = peer.getInterestedList();
        Set<String> unchokedList = new HashSet<>();
        Set<String> chokedList = new HashSet<>();
        Map<String, Client> neighborClients = peer.getClientThreadMap();
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String time = dateFormat.format(new Date());
        String logContent;

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
        for (String currentUnchoked:currentUnchokedList){
            if(unchokedList.contains(currentUnchoked)){
                unchokedList.remove(currentUnchoked);
            }
        }
        Set<String> currentChokedList = peer.getChokedList();
        for(String currentChoked:currentChokedList){
            if(chokedList.contains(currentChoked)){
                chokedList.remove(currentChoked);
            }
        }

        for(String chokePeer:chokedList){
            neighborClients.get(chokePeer).sendActualMsg(new ChokeMsg());
            System.out.println("{Server} Send choke from Client " + this.peer.getPeerId()
                    + " to Server " + chokePeer + "\n");
            currentChokedList.add(chokePeer);
        }

        for(String unchokePeer:unchokedList){
            neighborClients.get(unchokePeer).sendActualMsg(new UnChokeMsg());
            System.out.println("{Server} Send unChoke from Client " + this.peer.getPeerId()
                    + " to Server " + unchokePeer + "\n");
            currentUnchokedList.add(unchokePeer);
        }

        peer.setChokedList(currentChokedList);
        peer.setUnchokedList(currentUnchokedList);
        logContent = "[ " + time + " ]: Peer " + this.peer.getPeerId() + " has the preferred neighbors" + currentUnchokedList.toString() + ".";
        this.peer.writeLog(logContent);
    }
}
