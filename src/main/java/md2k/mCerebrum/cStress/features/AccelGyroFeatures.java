package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.library.Vector;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

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


/**
 * AutoSense Accelerometer and Gyrometer processor.
 */
public class AccelGyroFeatures {


    /**
     * Constructor
     * @param datastreams Global datastreams object
     * @param wrist Which wrist to operate on
     */
    public AccelGyroFeatures(DataStreams datastreams, String wrist) {

        DataPointStream gyrox = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_GYRO_X + wrist);
        DataPointStream gyroy = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_GYRO_Y + wrist);
        DataPointStream gyroz = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_GYRO_Z + wrist);

        DataPointStream accelx = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_ACCEL_X + wrist);
        DataPointStream accely = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_ACCEL_Y + wrist);
        DataPointStream accelz = datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_ACCEL_Z + wrist);

        DataPointStream gyr_mag = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag" + wrist);
        Vector.magnitude(gyr_mag, gyrox.data, gyroy.data, gyroz.data);

        DataPointStream gyr_mag_800 = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag800" + wrist);
        Smoothing.smooth(gyr_mag_800, gyr_mag, PUFFMARKER.GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE);
        DataPointStream gyr_mag_8000 = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag8000" + wrist);
        Smoothing.smooth(gyr_mag_8000, gyr_mag, PUFFMARKER.GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE);

        DataPointStream roll = datastreams.getDataPointStream("org.md2k.cstress.data.roll" + wrist);
        DataPointStream pitch = datastreams.getDataPointStream("org.md2k.cstress.data.pitch" + wrist);
        //TODO: add yaw

        if (PUFFMARKER.LEFT_WRIST.equals(wrist)) {
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, -1);
        } else {
            calculateRollPitchSegment(roll, pitch, accelx, accely, accelz, 1);
        }
    }


    /**
     * Segmentation based on two moving averages
     *
     * @param output Output datastream
     * @param slowMovingAverage Input slow moving average
     * @param fastMovingAverage Input fast moving average
     * @param THRESHOLD Threshold //TODO: more details
     * @param near //TODO: What is this?
     * @return
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

        if (curIndex > 0)
            for (int i = 0; i <= curIndex; i += 2) {
                output.data.add(new DataPoint(indexList[i], indexList[i + 1]));
                intersectionIndex[i] = indexList[i];
            }
        return intersectionIndex;
    }

    /**
     * Compute roll from accelerometer inputs
     * @param ax Accelerometer x-axis
     * @param ay Accelerometer y-axis
     * @param az Accelerometer z-axis
     * @return
     */
    public static double roll(double ax, double ay, double az) {
        return 180 * Math.atan2(ax, Math.sqrt(ay * ay + az * az)) / Math.PI;
    }


    /**
     * Compute pitch from accelerometer inputs
     * @param ax Accelerometer x-axis
     * @param ay Accelerometer y-axis
     * @param az Accelerometer z-axis
     * @return
     */
    public static double pitch(double ax, double ay, double az) {
        return 180 * Math.atan2(-ay, -az) / Math.PI;
    }

    /**
     * Segment datastreams based on roll and pitch
     * @param roll Output roll datastream
     * @param pitch Output pitch datastream
     * @param accelx Input accelerometer x datastream
     * @param accely Input accelerometer y datastream
     * @param accelz Input accelerometer z datastream
     * @param sign Sign //TODO: What does this mean?
     */
    private void calculateRollPitchSegment(DataPointStream roll, DataPointStream pitch, DataPointStream accelx, DataPointStream accely, DataPointStream accelz, int sign) {
        for (int i = 0; i < accelx.data.size(); i++) {
            double rll = roll(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            double ptch = pitch(accelx.data.get(i).value, sign * accely.data.get(i).value, accelz.data.get(i).value);
            roll.data.add(new DataPoint(accelx.data.get(i).timestamp, rll));
            pitch.data.add(new DataPoint(accelx.data.get(i).timestamp, ptch));
        }
    }


}
