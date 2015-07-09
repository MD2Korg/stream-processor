package md2k.mCerebrum.cStress;

/**
 * Created by hnat on 6/30/15.
 */
public class AUTOSENSE_PACKET {

    public long timestamp;
    public int channelID;
    public int[] data;

    public AUTOSENSE_PACKET(long timestamp, int channelID, int[] data) {
        this.timestamp = timestamp;
        this.channelID = channelID;
        this.data = data;
    }

    public String toString() {
        return new StringBuilder().append(this.timestamp).append(" (").append(this.channelID).append(") ").toString();
    }
}
