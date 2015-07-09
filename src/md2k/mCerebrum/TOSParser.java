package md2k.mCerebrum;

import md2k.mCerebrum.cStress.AUTOSENSE_PACKET;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by hnat on 6/29/15.
 */
public class TOSParser implements Iterable<AUTOSENSE_PACKET> {

    private final TreeMap<Long,AUTOSENSE_PACKET> data;


    public TOSParser() {
        this.data = new TreeMap<>();
    }

    public void importData(String filename) {

        AUTOSENSE_PACKET tempPacket;

        String[] tokens;
        int[] data = new int[] {0,0,0,0,0};
        long timestamp;
        int channelID;

        File file = new File(filename);

        int count = 0;

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            while(scanner.hasNext()){
                count++;
                if (count > 100000)
                    break;
                tokens = scanner.nextLine().split(",");
                channelID = Integer.parseInt(tokens[0]);
                timestamp = Long.parseLong(tokens[6]);
                for(int i=1;i<6;i++) {
                    data[i - 1] = Integer.parseInt(tokens[i]);
                }

                tempPacket = new AUTOSENSE_PACKET(timestamp, channelID, data);
                this.data.put(tempPacket.timestamp, tempPacket);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        for(Long index: this.data.navigableKeySet()) {
//            System.out.println(this.data.get(index));
//        }

//        System.out.println(this.data.size());

    }


    @Override
    public Iterator<AUTOSENSE_PACKET> iterator() {
        return this.data.values().iterator();
    }
}
