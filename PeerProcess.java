import Msg.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiantaozhang on 2017/10/22.
 */
public class PeerProcess {

    public String COMMON_FILE_NAME = "Common.cfg";

    public String PEER_FILE_NAME = "PeerInfo.cfg";

    public Map<String, String> CommonCfgMap;

    public int bitFieldSize;


    public static void main(String[] args) {
        PeerProcess process = new PeerProcess();
        process.getCommonCfg(process.COMMON_FILE_NAME);

        Peer peerA = new Peer(process.bitFieldSize);
        Peer peerB = new Peer(process.bitFieldSize);
    }

    /**
     * Function used to read Common.cfg and get all configuration information
     * @param filename
     * @return Map of common configuration
     */
    public void getCommonCfg(String filename) {
        CommonCfgMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null) {
                String[] content = line.trim().split(" ");
                CommonCfgMap.put(content[0], content[1]);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Cannot find this file, please use CfgGenerator first.");
        } catch (IOException ie) {
            System.out.println("Cannot read this file, please check the file.");
        }
    }

    public void getBitFieldSize() {
        int fileSize = Integer.parseInt(CommonCfgMap.get("FileSize"));
        int pieceSize = Integer.parseInt(CommonCfgMap.get("PieceSize"));
        int numOfPiece = (int) Math.ceil(fileSize / pieceSize);
        bitFieldSize = (int) Math.ceil(numOfPiece / 8) * 8;
    }
}
