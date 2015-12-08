package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.SignalProcessing.Smoothing;
import md2k.mCerebrum.cStress.Library.Vector;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.util.PuffMarkerUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joy on 12/4/2015.
 */
public class AccelGyroFeatures {


    public AccelGyroFeatures(DataStreams datastreams, String wrist) {

//        String[] wristList = new String[]{PuffMarkerUtils.LEFT_WRIST, PuffMarkerUtils.RIGHT_WRIST};
//        for (String wrist : wristList) {
        DataStream gyrox = datastreams.get(PuffMarkerUtils.KEY_DATA_GYRO_X + wrist);
        DataStream gyroy = datastreams.get(PuffMarkerUtils.KEY_DATA_GYRO_Y + wrist);
        DataStream gyroz = datastreams.get(PuffMarkerUtils.KEY_DATA_GYRO_Z + wrist);

        DataStream accelx = datastreams.get(PuffMarkerUtils.KEY_DATA_ACCEL_X + wrist);
        DataStream accely = datastreams.get(PuffMarkerUtils.KEY_DATA_ACCEL_Y + wrist);
        DataStream accelz = datastreams.get(PuffMarkerUtils.KEY_DATA_ACCEL_Z + wrist);

        DataStream gyr_mag = datastreams.get("org.md2k.cstress.data.gyr.mag" + wrist);
        Vector.magnitude(gyr_mag, gyrox.data, gyroy.data, gyroz.data);
        System.out.println("gyr_mag=" + gyr_mag.data.size());

        DataStream gyr_mag_800 = datastreams.get("org.md2k.cstress.data.gyr.mag800" + wrist);
        Smoothing.smooth(gyr_mag_800, gyr_mag, PuffMarkerUtils.GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE);
        DataStream gyr_mag_8000 = datastreams.get("org.md2k.cstress.data.gyr.mag8000" + wrist);
        Smoothing.smooth(gyr_mag_8000, gyr_mag, PuffMarkerUtils.GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE);

        DataStream roll = datastreams.get("org.md2k.cstress.data.roll" + wrist);
        DataStream pitch = datastreams.get("org.md2k.cstress.data.pitch" + wrist);
        //TODO: add yew

        if (PuffMarkerUtils.LEFT_WRIST.equals(wrist))
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, -1);
        else
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, 1);

        DataStream gyr_intersections = datastreams.get("org.md2k.cstress.data.gyr.intersections" + wrist);

        int[] intersectionIndexGYR_L = segmentationUsingTwoMovingAverage(gyr_intersections, gyr_mag_8000, gyr_mag_800, 0, 2);
        System.out.println("Arraylen=" + intersectionIndexGYR_L.length / 2 + "; datastreamlen=" + gyr_intersections.data.size());
//        }
    }


    /*
        segmenting those part where slow moving avg is greater than fast moving avg
    */
    public static int[] segmentationUsingTwoMovingAverage(DataStream output, DataStream slowMovingAverage
            , DataStream fastMovingAverage
            , int THRESHOLD, int near) {
        int[] indexList = new int[slowMovingAverage.data.size()];
        int curIndex = 0;
        for (int i = 0; i < slowMovingAverage.data.size(); i++) {
            double diff = slowMovingAverage.data.get(i).value - fastMovingAverage.data.get(i).value;
            if (diff > THRESHOLD) {
                if (curIndex == 0) {
                    indexList[curIndex++] = i;
                    indexList[curIndex] = i;
                } else {
                    if (i <= indexList[curIndex] + near) indexList[curIndex] = i;
                    else {
                        indexList[++curIndex] = i;
                        indexList[++curIndex] = i;
                    }
                }
            }
        }
        int[] intersectionIndex = new int[curIndex + 1];

//        System.arraycopy(indexList, 0, intersectionIndex, 0, curIndex + 1);
        if (curIndex > 0)
            for (int i = 0; i <= curIndex; i += 2) {
                output.data.add(new DataPoint(indexList[i], indexList[i + 1]));
                intersectionIndex[i] = indexList[i];
            }
        return intersectionIndex;
    }

    private void calculateRollPitchSegment(DataStream rolls, DataStream pitchs, DataStream accelx, DataStream accely, DataStream accelz, int sign) {
        for (int i = 0; i < accelx.data.size(); i++) {
            double rll = roll(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            double ptch = pitch(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            rolls.data.add(new DataPoint(accelx.data.get(i).timestamp, rll));
            pitchs.data.add(new DataPoint(accelx.data.get(i).timestamp, ptch));
        }
    }

    public static double roll(double ax, double ay, double az) {
        return 180 * Math.atan2(ax, Math.sqrt(ay * ay + az * az)) / Math.PI;
    }

    public static double pitch(double ax, double ay, double az) {
        return 180 * Math.atan2(-ay, -az) / Math.PI;
    }


}
