package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Statistics.BinnedStatistics;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Lomb;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
public class ECGFeatures {

    public DescriptiveStatistics RRStats;
    public DescriptiveStatistics RRHeartRateStats;
    public DescriptiveStatistics RRStatsNormalized;
    public ArrayList<Long> RRStatsTimestamps;

    //Data inputs
    private final DataPoint[] datapoints;
    private final double frequency;

    //Feature storage
    private double[] rr_value = new double[0];
    private long[] rr_timestamp = new long[0];
    private long[] rr_index = new long[0];
    private int[] rr_outlier = new int[0];

    private double HeartRate;
    private double LombLowFrequencyEnergy;
    private double LombMediumFrequencyEnergy;
    private double LombHighFrequencyEnergy;
    private double LombLowHighFrequencyEnergyRatio;

    public ECGFeatures(DataPoint[] dp, double freq, BinnedStatistics ECGStats, boolean activity) {

        datapoints = dp;
        frequency = freq;

        if (computeRR()) {

            //Decide if we should add the RR intervals from this minute to the running stats
            if (!activity) {
                for (int i = 0; i < rr_value.length; i++) {
                    if (rr_outlier[i] == AUTOSENSE.QUALITY_GOOD)
                        ECGStats.add((int) (rr_value[i] * 1000));
                }
            }

            RRStats = new DescriptiveStatistics();
            RRHeartRateStats = new DescriptiveStatistics();
            RRStatsNormalized = new DescriptiveStatistics();
            RRStatsTimestamps = new ArrayList<Long>();

            //Normalize RR intervals using Winsorized mean and stddev
            for (int i = 0; i < rr_value.length; i++) {
                if (rr_outlier[i] == AUTOSENSE.QUALITY_GOOD) {
                    RRStats.addValue(rr_value[i]);
                    RRHeartRateStats.addValue(60.0/rr_value[i]);
                    RRStatsNormalized.addValue((rr_value[i] - ECGStats.getWinsorizedMean() / 1000.0) / (ECGStats.getWinsorizedStdev() / 1000.0));
                    RRStatsTimestamps.add(rr_timestamp[i]);
                }
            }




            HeartRate = RRHeartRateStats.getPercentile(50);

            DataPoint[] rrDatapoints = new DataPoint[(int) RRStats.getN()];
            for (int i = 0; i < rrDatapoints.length; i++) {
                rrDatapoints[i] = new DataPoint(RRStats.getElement(i), i);
            }


            Lomb HRLomb = Core.lomb(rrDatapoints);

            LombLowHighFrequencyEnergyRatio = Core.heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15);
            LombLowFrequencyEnergy = Core.heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);
            LombMediumFrequencyEnergy = Core.heartRatePower(HRLomb.P, HRLomb.f, 0.2, 0.3);
            LombHighFrequencyEnergy = Core.heartRatePower(HRLomb.P, HRLomb.f, 0.3, 0.4);

        }
    }


    public DataPoint[] rawFeatures() {
        DataPoint[] result = new DataPoint[this.rr_value.length];
        for(int i=0; i<this.rr_value.length; i++) {
            DataPoint dp = new DataPoint(this.rr_value[i],this.rr_timestamp[i]);
            result[i] = dp;
        }
        return result;
    }


    /**
     * Compute RR intervals
     *
     * @return True if successful, False if there is not enough data
     */
    private boolean computeRR() {
        long[] Rpeak_index = Core.detect_Rpeak(datapoints, frequency);

        long[] pkT = new long[Rpeak_index.length];
        for (int i = 0; i < Rpeak_index.length; i++) {
            pkT[i] = datapoints[(int) Rpeak_index[i]].timestamp;
        }

        if (pkT.length < 2) {
            System.out.println("Not enough peaks, aborting computation");
            return false;
        }

        rr_value = new double[pkT.length - 1];
        rr_timestamp = new long[pkT.length - 1];


        for (int i = 1; i < pkT.length; i++) {
            rr_value[i - 1] = (double) (pkT[i] - pkT[i - 1]) / 1000.0;
            rr_timestamp[i - 1] = pkT[i - 1];

        }

        rr_index = new long[pkT.length];
        System.arraycopy(pkT, 0, rr_index, 0, pkT.length);

        rr_outlier = Core.detect_outlier_v2(rr_value, rr_timestamp);

        DescriptiveStatistics valueStats = new DescriptiveStatistics();
        for (int i = 0; i < rr_value.length; i++) {
            if (rr_outlier[i] == 0) {
                valueStats.addValue(rr_value[i]);
            }
        }

        double mu = valueStats.getMean();
        double sigma = valueStats.getStandardDeviation();

        for (int i = 0; i < rr_outlier.length; i++) {
            if (rr_outlier[i] == 0) {
                if (Math.abs(rr_value[i] - mu) > (3.0 * sigma)) {
                    rr_outlier[i] = AUTOSENSE.QUALITY_NOISE;
                } else {
                    rr_outlier[i] = AUTOSENSE.QUALITY_GOOD;
                }
            }
        }

        return true;
    }


    public double getHeartRate() {
        return HeartRate;
    }

    public double getLombLowFrequencyEnergy() {
        return LombLowFrequencyEnergy;
    }

    public double getLombMediumFrequencyEnergy() {
        return LombMediumFrequencyEnergy;
    }

    public double getLombHighFrequencyEnergy() {
        return LombHighFrequencyEnergy;
    }

    public double getLombLowHighFrequencyEnergyRatio() {
        return LombLowHighFrequencyEnergyRatio;
    }
}
