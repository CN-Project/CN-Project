import Msg.UnChokeMsg;

import java.util.*;

public class OptimisticNBUpdate implements Runnable {
    private Peer peer = null;
    private int interval;
    public OptimisticNBUpdate(Peer peer, int interval){
        this.peer = peer;
        this.interval = interval;
    }

    public void run() {
        while(true){
            long t1 = new Date().getTime();
            updateOptimisticNB();
            long t2 = new Date().getTime();
            long waitTime = interval*1000 - (t2 - t1);
            try {
                if(waitTime>0)
                    Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void updateOptimisticNB(){
        Set<String> unchokedList = this.peer.getUnchokedList();
        Set<String> interestList = this.peer.getInterestedList();

        ArrayList<String> chokedList = new ArrayList<>();

        for (String peerid: interestList){
            if(!unchokedList.contains(peerid)){
                chokedList.add(peerid);
            }
        }

        if (chokedList.size() > 0){
            Random random = new Random();
            int unchokedIndex = random.nextInt(chokedList.size());

            peer.getClientThreadMap().get(chokedList.get(unchokedIndex)).sendActualMsg(new UnChokeMsg());
        }
    }
}
