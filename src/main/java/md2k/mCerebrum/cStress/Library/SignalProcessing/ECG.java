package md2k.mCerebrum.cStress.Library.SignalProcessing;

import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Structs.Lomb;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.List;

/*
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

/**
 * Electrocardiogram (ECG) signal processing routines
 */
public class ECG {
    /**
     * Heart rate Low Frequency - High Frequency ratio
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param P        Lomb P result
     * @param f        Lomb f result
     * @param lowRate  Defines the rate below which is consider low frequency
     * @param highRate Defines the rate above which is consider high frequency
     * @return Double value that is the ratio between low and and high frequencies
     */
    public static double heartRateLFHF(double[] P, double[] f, double lowRate, double highRate) {
        double result1 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] < lowRate) {
                result1 += P[i];
            }
        }
        double result2 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowRate && f[i] <= highRate) {
                result2 += P[i];
            }
        }
        return result1 / result2;
    }

    /**
     * Heart Rate Power function
     * <p>
     * Reference: Matlab code \\TODO
     * </p>
     *
     * @param P             Lomb P result
     * @param f             Lomb f result
     * @param lowFrequency  Defines the value below which is consider low frequency
     * @param highFrequency Defines the value above which is consider high frequency
     * @return Sum of all values between the low and high frequencies
     */
    public static double heartRatePower(double[] P, double[] f, double lowFrequency, double highFrequency) {
        double result = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowFrequency && f[i] <= highFrequency) {
                result += P[i];
            }
        }

        return result;
    }

    /**
     * Lomb–Scargle periodogram implementation
     * <p>
     * Reference: https://en.wikipedia.org/wiki/Least-squares_spectral_analysis#The_Lomb.E2.80.93Scargle_periodogram
     * Matlab HeartRateLomb.m
     * </p>
     *
     * @param dp DataPoint array of values
     * @return Lomb object containing P and f
     */
    public static Lomb lomb(DataPoint[] dp) {
        double T = dp[dp.length - 1].timestamp - dp[0].timestamp;
        int nf = (int) Math.round(0.5 * 4.0 * 1.0 * dp.length);
        double[] f = new double[nf];

        for (int i = 0; i < nf; i++) {
            f[i] = (i + 1) / (T * 4);
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

        for (int i = 0; i < nf; i++) {
            Ss2wt = 0;
            Sc2wt = 0;

            for (int j = 0; j < wt.length; j++) {
                wt[j] = 2.0 * Math.PI * f[i] * dp[j].timestamp;
                swt[j] = Math.sin(wt[j]);
                cwt[j] = Math.cos(wt[j]);

                Ss2wt += cwt[j] * swt[j];
                Sc2wt += (cwt[j] - swt[j]) * (cwt[j] + swt[j]);

            }
            Ss2wt *= 2;

            wtau = 0.5 * Math.atan2(Ss2wt, Sc2wt);
            swtau = Math.sin(wtau);
            cwtau = Math.cos(wtau);


            swttau2 = 0;
            cwttau2 = 0;

            for (int j = 0; j < swt.length; j++) {
                swttau[j] = swt[j] * cwtau - cwt[j] * swtau;
                cwttau[j] = cwt[j] * cwtau + swt[j] * swtau;

                swttau2 += swttau[j] * swttau[j];
                cwttau2 += cwttau[j] * cwttau[j];
            }


            part1 = 0;
            part2 = 0;
            for (int j = 0; j < cwttau.length; j++) {
                part1 += (dp[j].value * cwttau[j]);
                part2 += (dp[j].value * swttau[j]);
            }
            part1 = part1 * part1; //Square result
            part2 = part2 * part2; //Square result

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }

    /**
     * Compute the signal energy using FFTs
     * <p>
     * Reference: Matlab \\TODO
     * </p>
     *
     * @param data Input data array
     * @param inc  Increment
     * @return Energy
     */
    public static double computeEnergy(double[] data, int inc) {
        double result = 0;

        int NFFT = nextPower2(data.length);

        double[] buffer = new double[NFFT];
        System.arraycopy(data, 0, buffer, 0, data.length);

        FastFourierTransformer f = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftC = f.transform(buffer, TransformType.FORWARD);

        for (Complex aFftC : fftC) {
            result += (aFftC.abs() / inc) * (aFftC.abs() / inc);
        }
        return result;
    }


    /**
     * Determine the next power of 2 larger than length
     * <p>
     * Reference: Matlab implementation
     * </p>
     *
     * @param length Input
     * @return The next largest power of 2 greater than length
     */
    public static int nextPower2(int length) {
        if (length == 0)
            return 0;
        else
            return (int) Math.ceil(Math.log(length) / Math.log(2));
    }

    /**
     * Mean crossing algorithm
     * <p>
     * Reference: Matlab? \\TODO
     * </p>
     *
     * @param x    Input DataPoint array
     * @param mean Value which to compute crossings based on
     * @return Array containing the indexes of x that are cross mean
     */
    public static double[] crossing(DataPoint[] x, double mean) {
        List<Double> crossings = new ArrayList<Double>();

        for (int i = 0; i < x.length - 1; i++) {
            if ((x[i].value > mean && x[i + 1].value <= mean) || x[i].value < mean && x[i + 1].value >= mean) {
                crossings.add((double) (i + 1));
            }
        }
        double[] result = new double[crossings.size()];
        for (int i = 0; i < crossings.size(); i++) {
            result[i] = crossings.get(i);
        }
        return result;
    }
}
