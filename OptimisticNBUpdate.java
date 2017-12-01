import Msg.UnChokeMsg;

import java.text.SimpleDateFormat;
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
    private synchronized void updateOptimisticNB(){
        Set<String> chokedList = this.peer.getChokedList();
        Set<String> interestList = this.peer.getInterestedList();

        ArrayList<String> chokedInterestedList = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat();
        String time = dateFormat.format(new Date());
        String logContent;

        for (String peerid: interestList){
            if(chokedList.contains(peerid)){
                chokedInterestedList.add(peerid);
            }
        }

        if (chokedInterestedList.size() > 0){
            Random random = new Random();
            int unchokedIndex = random.nextInt(chokedInterestedList.size());
            peer.getClientThreadMap().get(chokedInterestedList.get(unchokedIndex)).sendActualMsg(new UnChokeMsg());
            this.peer.addUnchokedList(chokedInterestedList.get(unchokedIndex));
            this.peer.removeFromchokedList(chokedInterestedList.get(unchokedIndex));
            logContent = "[ " + time + " ]: Peer " + this.peer.getPeerId() + " has the optimistically unchoked neighbor" + unchokedIndex + ".";
            this.peer.writeLog(logContent);
        }
    }
}
