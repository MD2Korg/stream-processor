package md2k.mCerebrum.cStress;

/**
 * Created by hnat on 7/2/15.
 */
public class Sensor {
    public String identifier;
    public double frequency;
    public int channel;

    public Sensor(String identifier, double frequency, int channel) {
        this.identifier = identifier;
        this.frequency = frequency;
        this.channel = channel;
    }
}
