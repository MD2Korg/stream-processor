package md2k.mCerebrum.cStress;

import java.util.HashMap;

/**
 * Created by hnat on 7/2/15.
 */
public class SensorConfiguration {

    private HashMap<String,Sensor> sensors;

    public SensorConfiguration() {
        this.sensors = new HashMap<>();
    }

    public void add(String identifier, double frequency, int channel) {
        this.sensors.put(identifier, new Sensor(identifier,frequency,channel));
    }

    public double getFrequency(String identifier) {
        if (this.sensors.containsKey(identifier))
            return this.sensors.get(identifier).frequency;
        return -1;
    }

    public double getChannel(String identifier) {
        if (this.sensors.containsKey(identifier))
            return this.sensors.get(identifier).channel;
        return -1;
    }

}
