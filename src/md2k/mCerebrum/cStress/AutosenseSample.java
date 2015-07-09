package md2k.mCerebrum.cStress;

/**
 * Created by hnat on 7/1/15.
 */
public class AutosenseSample {

    public long timestamp;
    public int value;

    public AutosenseSample(AUTOSENSE_PACKET ap, int i) {
        int SAMPLE_PERIOD = 1;
        this.timestamp = ap.timestamp - SAMPLE_PERIOD *(4-i);
        this.value = ap.data[i];
    }

}
