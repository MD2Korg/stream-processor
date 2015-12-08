package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.DataPointStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.SignalProcessing.Smoothing;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Time;
import md2k.mCerebrum.cStress.Library.Vector;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Md. Mahbubur Rahman <mmrahman@memphis.edu>
 * - Timothy Hnat <twhnat@memphis.edu>
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
 * Accelerometer feature computation class
 */
public class AccelerometerFeatures {

    /**
     * Accelerometer feature processor for cStress-Autosense
     *
     * @param datastreams        object
     * @param ACTIVITY_THRESHOLD threshold based on accelerometer magnitude
     * @param windowSize         seconds
     */
    public AccelerometerFeatures(DataStreams datastreams, double ACTIVITY_THRESHOLD, int windowSize) {
        //Compute normalized accelerometer values
        DataPointStream accelx = datastreams.get("org.md2k.cstress.data.accelx");
        DataPointStream accelxNormalized = datastreams.get("org.md2k.cstress.data.accelx.normalized");
        Smoothing.normalize(accelxNormalized, accelx);

        DataPointStream accely = datastreams.get("org.md2k.cstress.data.accely");
        DataPointStream accelyNormalized = datastreams.get("org.md2k.cstress.data.accely.normalized");
        Smoothing.normalize(accelyNormalized, accely);

        DataPointStream accelz = datastreams.get("org.md2k.cstress.data.accelz");
        DataPointStream accelzNormalized = datastreams.get("org.md2k.cstress.data.accelz.normalized");
        Smoothing.normalize(accelzNormalized, accelz);


        //Window accel data streams
        ArrayList<DataPoint[]> segxWindowed = Time.window(datastreams.get("org.md2k.cstress.data.accelx").data, windowSize);
        ArrayList<DataPoint[]> segyWindowed = Time.window(datastreams.get("org.md2k.cstress.data.accely").data, windowSize);
        ArrayList<DataPoint[]> segzWindowed = Time.window(datastreams.get("org.md2k.cstress.data.accelz").data, windowSize);

        try {
            //Compute magnitude and stdev from windowed datastreams
            for (int i = 0; i < segxWindowed.size(); i++) {
                DataPoint[] wx = segxWindowed.get(i);
                DataPoint[] wy = segyWindowed.get(i);
                DataPoint[] wz = segzWindowed.get(i);
                double[] magnitude = Vector.magnitude(wx, wy, wz);
                for (int j = 0; j < magnitude.length; j++) {
                    datastreams.get("org.md2k.cstress.data.accel.magnitude").add(new DataPoint(wx[j].timestamp, magnitude[j]));
                }
                DescriptiveStatistics sd = new DescriptiveStatistics(magnitude);

                if (wx.length > 0) {
                    datastreams.get("org.md2k.cstress.data.accel.windowed.magnitude.stdev").add(new DataPoint(wx[0].timestamp, sd.getStandardDeviation()));
                }
            }


            //Compute Activity from datastreams
            double lowlimit = datastreams.get("org.md2k.cstress.data.accel.magnitude").getPercentile(1);
            double highlimit = datastreams.get("org.md2k.cstress.data.accel.magnitude").getPercentile(99);
            double range = highlimit - lowlimit;

            DataPointStream stdmag = datastreams.get("org.md2k.cstress.data.accel.windowed.magnitude.stdev");

            boolean[] activityOrNot = new boolean[stdmag.data.size()];
            for (int i = 0; i < stdmag.data.size(); i++) {
                activityOrNot[i] = stdmag.data.get(i).value > (lowlimit + ACTIVITY_THRESHOLD * range);
            }

            int minActive = 0;
            for (boolean b : activityOrNot) {
                if (b) {
                    minActive += 1;
                }
            }
            int active = 0;
            if (minActive > (stdmag.data.size() / 2)) {
                active = 1;
            }

            datastreams.get("org.md2k.cstress.data.accel.activity").add(new DataPoint(datastreams.get("org.md2k.cstress.data.accelx").data.get(0).timestamp, active));

        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }


}
