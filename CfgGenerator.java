import java.io.*;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class CfgGenerator {

    /**
     * Define default values for CommonCfg
     */
    private static final int NUMBER_OF_PREFERRED_NEIGHBORS = 2;

    private static final int UNCHOKING_INTERVAL = 5;

    private static final int OPTIMISTIC_UNCHOKING_INTERVAL = 15;

    private static final int FILE_SIZE = 10000232;

    private static final int PIECE_SIZE = 32768;

    private static final String FILE_NAME = "TheFile.dat";


    public static void main(String[] args) {
        CfgGenerator cfg = new CfgGenerator();
        cfg.writeCommonCfg();
        File peerInfo = new File("PeerInfo.cfg");
        if (peerInfo.exists()) {
            peerInfo.delete();
        }
        cfg.writePeerInfo(1001, "lin114-00-cise.ufl.edu", 6008, 1);
        for (int i = 1; i <= 5; i++) {
            cfg.writePeerInfo(1001 + i, "lin114-0" + i + "-cise.ufl.edu", 6008, 0);
        }
    }
    /**
     * Write Common.cfg using specific parameters
     */
    public void writeCommonCfg(int numberOfPreferredNeighbors, int unchokingInterval,
                               int optimisticUnchokingInterval, String fileName, int fileSize, int pieceSize) {
        try {

            PrintWriter out = new PrintWriter(new File("Common.cfg"));
            out.println("NumberOfPreferredNeighbors" + " " + numberOfPreferredNeighbors);
            out.println("UnchokingInterval" + " " + unchokingInterval);
            out.println("OptimisticUnchokingInterval" + " " + optimisticUnchokingInterval);
            out.println("FileName" + " " + fileName);
            out.println("FileSize" + " " + fileSize);
            out.println("PieceSize" + " " + pieceSize);

            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot generate cfg file, file not found");
        }
    }


    /**
     * Use the default values if no specific value is given
     */
    public void writeCommonCfg() {
        writeCommonCfg(NUMBER_OF_PREFERRED_NEIGHBORS, UNCHOKING_INTERVAL,
                       OPTIMISTIC_UNCHOKING_INTERVAL, FILE_NAME,
                       FILE_SIZE, PIECE_SIZE);
    }


    /**
     *Write PeerInfo.cfg using specific parameters
     */
    public void writePeerInfo(int peerID, String hostName, int listeningPort, int hasFileOrNot) {
        try {
            FileWriter out = new FileWriter(new File("PeerInfo.cfg"), true);
            out.write(peerID + " " + hostName + " " + listeningPort + " " + hasFileOrNot + "\n");
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot generate cfg file, file not found");
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }

}
