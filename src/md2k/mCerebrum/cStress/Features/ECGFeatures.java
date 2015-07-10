package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Structs.DataPoint;
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
public class ECGFeatures{
    private final DataPoint[] datapoints;
    private final double frequency;

    //ecg_statistical_features.m

    public double variance;
    public double heartrateFLHF;
    public double heartratepower12;
    public double heartratepower23;
    public double heartratepower34;
    public double rr_mean;
    public double rr_median;
    public double rr_quartilerange;
    public double rr_80percentile;
    public double rr_20percentile;
    public double rr_count;

    public DataPoint[] computeRR() {
        ArrayList<DataPoint> result = new ArrayList<>();

        long[] Rpeak_index = detect_Rpeak(this.datapoints, this.frequency);

        DataPoint[] dpArray = new DataPoint[result.size()];
        result.toArray(dpArray);
        return dpArray;
    }

    private long[] detect_Rpeak(DataPoint[] datapoints, double frequency) {

        double[] sample = new double[datapoints.length];
        double[] timestamps = new double[datapoints.length];
        for(int i=0; i<sample.length; i++) {
            sample[i] = datapoints[i].value;
            timestamps[i] = datapoints[i].timestamp;
        }

        int window_l = (int) Math.ceil(frequency/5.0);

        double thr1 = 0.5;
        double f = 2.0/frequency;
        double delp = 0.02;
        double dels1 = 0.02;
        double dels2 = 0.02;
        double[] F = {0.0, 4.5*f, 5.0*f, 20.0*f, 20.5*f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0/dels1, 1.0/delp, 500/dels2};
        double fl = 256;
        double[] b = filrs(fl, F, A, w); //TODO: This filter is a function of frequency.  Can it be hard-coded for this case?

        double[] y2 = conv(sample, b, "same"); //TODO: Fixme: Can this be done with FFT transformations?
        DescriptiveStatistics statsY2 = new DescriptiveStatistics();
        for(double d: y2) {
            statsY2.addValue(d);
        }
        for(int i=0; i<y2.length; i++) {
            y2[i] /= statsY2.getPercentile(90);
        }
        
        double[] h_D = {-1.0/8.0, -2.0/8.0, 0.0/8.0, 2.0/8.0, -1.0/8.0};
        double[] y3 = conv(y2, h_D, "same"); //TODO: Fixme: Can this be done with FFT transformations?
        DescriptiveStatistics statsY3 = new DescriptiveStatistics();
        for(double d: y3) {
            statsY3.addValue(d);
        }
        for(int i=0; i<y3.length; i++) {
            y3[i] /= statsY3.getPercentile(90);
        }

        double[] y4 = new double[y3.length];
        DescriptiveStatistics statsY4 = new DescriptiveStatistics();
        for(int i=0; i<y3.length; i++) {
            y4[i] = y3[i]*y3[i];
            statsY4.addValue(y4[i]);
        }
        for(int i=0; i<y4.length; i++) {
            y4[i] /= statsY4.getPercentile(90);
        }

        double[] h_I = blackman(window_l);
        double[] y5 = conv(y4, h_I, "same"); //TODO: Fixme: Can this be done with FFT transformations?
        DescriptiveStatistics statsY5 = new DescriptiveStatistics();
        for(double d: y5) {
            statsY5.addValue(d);
        }
        for(int i=0; i<y5.length; i++) {
            y5[i] /= statsY5.getPercentile(90);
        }

        //TODO: Complete this

        return new long[10];
    }

    private double[] blackman(int window_l) {
        double[] result = new double[window_l];
        int M = (int) Math.floor((window_l+1)/2);
        for(int i=0; i<M; i++) {
            result[i] = 0.42 - 0.5*Math.cos(2.0 * Math.PI * (double) i / (window_l-1)) + 0.08*Math.cos(4.0 * Math.PI * (double) i / (window_l-1));
            result[window_l-i-1] = result[i];
        }
        return result;
    }

    //TODO: Complete this
    private double[] filrs(double fl, double[] f, double[] a, double[] w) {
        return new double[2];
    }

    private double[] conv(double[] signal, double[] kernel, String type) {
        double[] result = new double[Math.max(Math.max(signal.length+kernel.length-1,signal.length),kernel.length)];
         //TODO: Complete this
        return new double[0];
    }

    public class Lomb {
        public double[] P;
        public double[] f;
    }

    public ECGFeatures(DataPoint[] dp, double freq) {

        this.datapoints = dp;
        this.frequency = freq;

        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (DataPoint aData : dp) {
            stats.addValue(aData.value);
        }

        variance = stats.getVariance();
        rr_mean = stats.getMean();
        rr_median = stats.getPercentile(50);
        rr_quartilerange = (stats.getPercentile(75) - stats.getPercentile(25)) / 2.0;
        rr_80percentile = stats.getPercentile(80);
        rr_20percentile = stats.getPercentile(20);
        rr_count = ( (double) dp.length ) / ( dp[dp.length-1].timestamp-dp[0].timestamp );

        Lomb HRLomb = lomb(dp);

        heartrateFLHF = heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15);
        heartratepower12 = heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);
        heartratepower23 = heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);
        heartratepower34 = heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);

    }

    private double heartRateLFHF(double[] P, double[] f, double lowRate, double highRate) {
        double result1 = 0;
        for(int i=0; i<P.length; i++) {
            if(f[i] < lowRate) {
                result1 += P[i];
            }
        }
        double result2 = 0;
        for(int i=0; i<P.length; i++) {
            if(f[i] >= lowRate && f[i] <= highRate) { //Should this be >= lowRate instead of what is in the code?
                result2 += P[i];
            }
        }
        return result1/result2;
    }

    private double heartRatePower(double[] P, double[] f, double lowFrequency, double highFrequency) {
        double result = 0;
        for(int i=0; i<P.length; i++) {
            if(f[i] >= lowFrequency && f[i] <= highFrequency) {
                result += P[i];
            }
        }

        return result;
    }

    private Lomb lomb(DataPoint[] dp) { //TODO: This is a very large bottleneck for ECG
        //HeartRateLomb.m


        double T = dp[dp.length-1].timestamp-dp[0].timestamp;
        int nf = (int) Math.round(0.5 * 4.0 * 1.0 * dp.length);
        double[] f = new double[nf];

        for(int i=0; i<nf; i++) {
            f[i] = (i+1)/(T*4);
        }

        nf = f.length;

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (DataPoint aData : dp) {
            stats.addValue(aData.value);
        }

        double mx = stats.getMean();
        double vx = stats.getVariance();

        for (DataPoint aDp : dp) {
            aDp.value -= mx;
        }

        double[] P = new double[nf];
        double[] wt;
        double[] swt;
        double[] cwt;
        double Ss2wt;
        double Sc2wt;
        double wtau;
        double swtau;
        double cwtau;
        double[] swttau;
        double[] cwttau;
        double swttau2;
        double cwttau2;
        wt = new double[dp.length];
        swt = new double[dp.length];
        cwt = new double[dp.length];
        swttau = new double[swt.length];
        cwttau = new double[swt.length];
        double part1;
        double part2;
        double count = 0;
        for(int i=0; i<nf; i++) {
            Ss2wt = 0;
            Sc2wt = 0;

            for(int j=0; j<wt.length; j++) {
                wt[j] = 2.0 * Math.PI * f[i] * dp[j].timestamp;
                swt[j] = .1;//Math.sin(wt[j]);
                cwt[j] = .1;//Math.cos(wt[j]);

                Ss2wt += cwt[j]*swt[j];
                Sc2wt += (cwt[j]-swt[j])*(cwt[j]+swt[j]);

            }
            Ss2wt *= 2;

            wtau = 0.1;//0.5 * Math.atan2(Ss2wt, Sc2wt);
            swtau = 0.1;//Math.sin(wtau);
            cwtau = 0.1;//Math.cos(wtau);


            swttau2 = 0;
            cwttau2 = 0;

            for(int j = 0; j<swt.length; j++) {
                swttau[j] = swt[j]*cwtau - cwt[j]*swtau;
                cwttau[j] = cwt[j]*cwtau - swt[j]*swtau;

                swttau2 += swttau[j]*swttau[j];
                cwttau2 += cwttau[j]*cwttau[j];
            }


            part1 = 0;
            part2 = 0;
            for(int j = 0; j<cwttau.length; j++) {
                part1 += (dp[j].value *cwttau[j])*(dp[j].value *cwttau[j]);
                part2 += (dp[j].value *swttau[j])*(dp[j].value *swttau[j]);
            }

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }




}
