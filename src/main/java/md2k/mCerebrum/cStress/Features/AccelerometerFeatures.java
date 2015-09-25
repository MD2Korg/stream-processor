package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Statistics.RunningStatistics;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

/**
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
public class AccelerometerFeatures {
    public static final double ACTIVITY_THRESHOLD = 0.35;
    public boolean Activity;

    public double[] StdevMagnitude;
    private long[] rawTimestamps;
    private double[] rawstdMagnitudeArray;

    /** Accelerometer Feature Computation
     * Reference: accelerometerfeature_extraction.m
     * @param segx
     * @param segy
     * @param segz
     * @param samplingFreq
     */
    public AccelerometerFeatures(DataPoint[] segx, DataPoint[] segy, DataPoint[] segz, double samplingFreq, RunningStatistics MagnitudeStats) {
        int windowSize = 10*1000;

        ArrayList<DataPoint[]> segxWindowed = Core.window(segx, windowSize);
        ArrayList<DataPoint[]> segyWindowed = Core.window(segy, windowSize);
        ArrayList<DataPoint[]> segzWindowed = Core.window(segz, windowSize);
        long[] timestampArray = new long[segxWindowed.size()];

        ArrayList<Double> stdMagnitudeArray = new ArrayList<Double>();
        for(int i=0; i<segxWindowed.size(); i++) {

            DataPoint[] wx = segxWindowed.get(i);
            DataPoint[] wy = segyWindowed.get(i);
            DataPoint[] wz = segzWindowed.get(i);


            double[] magnitude = Core.magnitude(wx, wy, wz);

            DescriptiveStatistics statsMagnitude = new DescriptiveStatistics();
            for (double d : magnitude) {
                statsMagnitude.addValue(d);
                MagnitudeStats.add(d);
            }
            stdMagnitudeArray.add(statsMagnitude.getStandardDeviation());
            timestampArray[i] = wx[0].timestamp;
        }
        StdevMagnitude = new double[stdMagnitudeArray.size()];
        for(int i=0; i<StdevMagnitude.length; i++) {
            StdevMagnitude[i] = stdMagnitudeArray.get(i);
        }

        this.rawTimestamps = timestampArray;
        this.rawstdMagnitudeArray = StdevMagnitude;

        this.Activity = activityAnalysis(this.StdevMagnitude, MagnitudeStats); //From Autosense Matlab code

    }

    public DataPoint[] rawFeatures() {
        DataPoint[] result = new DataPoint[this.rawstdMagnitudeArray.length];
        for(int i=0; i<this.rawstdMagnitudeArray.length; i++) {
            DataPoint dp = new DataPoint(this.rawstdMagnitudeArray[i],this.rawTimestamps[i]);
            result[i] = dp;
        }
        return result;
    }

    public boolean activityAnalysis(double[] accelFeature, RunningStatistics magnitudeStats) {

        double lowlimit = magnitudeStats.getMean() - 3.0 * magnitudeStats.getStdev(); //Using this instead of percentile01
        double highlimit = magnitudeStats.getMean() + 3.0 * magnitudeStats.getStdev(); //Using this instead of percentile99
        double range = highlimit-lowlimit;

        boolean[] activityOrNot = new boolean[accelFeature.length];
        for(int i=0; i<accelFeature.length; i++) {
            activityOrNot[i] = accelFeature[i] > (lowlimit + ACTIVITY_THRESHOLD * range);
        }

        int minActive = 0;
        for(boolean b: activityOrNot) {
            if (b) {
                minActive += 1;
            }
        }
        return minActive > (accelFeature.length/2);
    }


}
