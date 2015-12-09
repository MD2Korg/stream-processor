package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.DataPointStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.SignalProcessing.Smoothing;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Vector;
import md2k.mCerebrum.cStress.util.PuffMarkerUtils;


/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleneen <nsleheen@memphis.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public class AccelGyroFeatures {


    public AccelGyroFeatures(DataStreams datastreams, String wrist) {

//        String[] wristList = new String[]{PuffMarkerUtils.LEFT_WRIST, PuffMarkerUtils.RIGHT_WRIST};
//        for (String wrist : wristList) {
        DataPointStream gyrox = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_GYRO_X + wrist);
        DataPointStream gyroy = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_GYRO_Y + wrist);
        DataPointStream gyroz = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_GYRO_Z + wrist);

        DataPointStream accelx = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_ACCEL_X + wrist);
        DataPointStream accely = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_ACCEL_Y + wrist);
        DataPointStream accelz = datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_ACCEL_Z + wrist);

        DataPointStream gyr_mag = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag" + wrist);
        Vector.magnitude(gyr_mag, gyrox.data, gyroy.data, gyroz.data);
        System.out.println("gyr_mag=" + gyr_mag.data.size());

        DataPointStream gyr_mag_800 = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag800" + wrist);
        Smoothing.smooth(gyr_mag_800, gyr_mag, PuffMarkerUtils.GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE);
        DataPointStream gyr_mag_8000 = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag8000" + wrist);
        Smoothing.smooth(gyr_mag_8000, gyr_mag, PuffMarkerUtils.GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE);

        DataPointStream roll = datastreams.getDataPointStream("org.md2k.cstress.data.roll" + wrist);
        DataPointStream pitch = datastreams.getDataPointStream("org.md2k.cstress.data.pitch" + wrist);
        //TODO: add yew

        if (PuffMarkerUtils.LEFT_WRIST.equals(wrist))
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, -1);
        else
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, 1);

        DataPointStream gyr_intersections = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.intersections" + wrist);

        int[] intersectionIndexGYR_L = segmentationUsingTwoMovingAverage(gyr_intersections, gyr_mag_8000, gyr_mag_800, 0, 2);
        System.out.println("Arraylen=" + intersectionIndexGYR_L.length / 2 + "; datastreamlen=" + gyr_intersections.data.size());
//        }
    }


    /*
        segmenting those part where slow moving avg is greater than fast moving avg
    */
    public static int[] segmentationUsingTwoMovingAverage(DataPointStream output, DataPointStream slowMovingAverage
            , DataPointStream fastMovingAverage
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

    public static double roll(double ax, double ay, double az) {
        return 180 * Math.atan2(ax, Math.sqrt(ay * ay + az * az)) / Math.PI;
    }

    public static double pitch(double ax, double ay, double az) {
        return 180 * Math.atan2(-ay, -az) / Math.PI;
    }

    private void calculateRollPitchSegment(DataPointStream rolls, DataPointStream pitchs, DataPointStream accelx, DataPointStream accely, DataPointStream accelz, int sign) {
        for (int i = 0; i < accelx.data.size(); i++) {
            double rll = roll(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            double ptch = pitch(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            rolls.data.add(new DataPoint(accelx.data.get(i).timestamp, rll));
            pitchs.data.add(new DataPoint(accelx.data.get(i).timestamp, ptch));
        }
    }


}
