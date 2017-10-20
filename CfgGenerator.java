import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by jiantaozhang on 2017/10/20.
 */
public class CfgGenerator {

    /**
     * Define constant values for CommonCfg
     */
    private static final int NUMBER_OF_PREFERRED_NEIGHBORS = 2;

    private static final int UNCHOKING_INTERVAL = 5;

    private static final int OPTIMISTIC_UNCHOKING_INTERVAL = 15;

    private static final int FILE_SIZE = 10000232;

    private static final int PIECE_SIZE = 32768;

    private static final String FILE_NAME = "TheFile.dat";


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
            PrintWriter out = new PrintWriter(new File("PeerInfo.cfg"));
            out.println(peerID + " " + hostName + " " + listeningPort + " " + hasFileOrNot);
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot generate cfg file, file not found");
        }

    }


    public static void main(String[] args) {
        CfgGenerator cg = new CfgGenerator();
        cg.writeCommonCfg();
        cg.writePeerInfo(1001, "lin114-00-cise.ufl.edu", 6008, 1);
    }


}
