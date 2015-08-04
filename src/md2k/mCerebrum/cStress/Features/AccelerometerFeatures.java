package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library;
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
public class AccelerometerFeatures {
    public boolean Activity;

//    public double MaxOfMean;
//    public double MeanOfMean;
//    public double MinOfMean;
//
//    public double MaxOfMin;
//    public double MinOfMin;
//    public double MaxOfMax;
//    public double MinOfMax;
//
//    public double MaxOfRange;
//    public double MinOfRange;
//
//    public double MaxOfStd;
//    public double MinOfStd;
//    public double MeanOfStd;
//
//    public double MaxVar;
//    public double MinVar;
//    public double MeanVar;
//
//    public double MaxOfMed;
//    public double MinOfMed;
//    public double MedOfMed;
//
//    public double MaxFD;
//    public double MinFD;
//    public double MeanFD;
//
//    public double MaxCrossing;
//    public double MeanCrossing;
//    public double MinCrossing;
//
//    public double TotalEnergy;
//    public double MaxStdEnergy;
//    public double MeanStdEnergy;
//
//    public double MinStdEnergy;
//    public double MaxEnergyLF;
//    public double MinEnergyLF;
//    public double MedianEnergyLF;
//
//    public double MaxEnergyHF;
//    public double MinEnergyHF;
//    public double MedianEnergyHF;
//
//    public double TotalEnergyLF;
//    public double TotalEnergyHF;
//
//    public double AvgEnergy;
//    public double AvgMagnitude;

    public double[] StdevMagnitude;

//    public double MagnitudeFFTOneHz;
//    public double MagnitudeFFTTWoHz;
//    public double MagnitudeFFTThreeHz;
//    public double MagnitudeFFTFourHz;
//    public double MagnitudeFFTFiveHz;
//
//    public double MagnitudeFFTHF;
//    public double MagnitudeFFTLF;
//
//    public double MaxMinVarRatio;
//    public double MaxMeanVarRatio;
//    public double MaxMedianVarRatio;
//
//    public double MaxOfmedianEnergy;
//    public double MinOfmedianEnergy;
//    public double MeanOfmedianEnergy;
//
//    public double StartTimestamp;
//    public double EndTimestamp;

    /** Accelerometer Feature Computation
     * Reference: accelerometerfeature_extraction.m
     * @param segx
     * @param segy
     * @param segz
     * @param samplingFreq
     */
    public AccelerometerFeatures(DataPoint[] segx, DataPoint[] segy, DataPoint[] segz, double samplingFreq, RunningStatistics MagnitudeStats) {
        int windowSize = 10*1000;

        ArrayList<DataPoint[]> segxWindowed = Library.window(segx, windowSize);
        ArrayList<DataPoint[]> segyWindowed = Library.window(segy, windowSize);
        ArrayList<DataPoint[]> segzWindowed = Library.window(segz, windowSize);





//        DescriptiveStatistics statsX = new DescriptiveStatistics();
//        DescriptiveStatistics statsY = new DescriptiveStatistics();
//        DescriptiveStatistics statsZ = new DescriptiveStatistics();
//
//        for (DataPoint aData : segx) {
//            statsX.addValue(aData.value);
//        }
//        for (DataPoint aData : segy) {
//            statsY.addValue(aData.value);
//        }
//        for (DataPoint aData : segz) {
//            statsZ.addValue(aData.value);
//        }
//
//        double minX = statsX.getMin();
//        double maxX = statsX.getMax();
//        double rangeX = maxX - minX;
//
//        double minY = statsY.getMin();
//        double maxY = statsY.getMax();
//        double rangeY = maxY - minY;
//
//        double minZ = statsZ.getMin();
//        double maxZ = statsZ.getMax();
//        double rangeZ = maxZ - minZ;
//
//        double minOfmin = Math.min(minX, minY);
//        minOfmin = Math.min(minOfmin, minZ);
//
//        double maxOfmax = Math.max(maxX, maxY);
//        maxOfmax = Math.max(maxOfmax, maxZ);
//
//        double maxOfmin = Math.max(minX, minY);
//        maxOfmin = Math.max(maxOfmin, minZ);
//
//        double minOfmax = Math.min(maxX, maxY);
//        minOfmax = Math.min(minOfmax, maxZ);
//
//        double maxRange = Math.max(rangeX, rangeY);
//        maxRange = Math.max(maxRange, rangeZ);
//
//        double minRange = Math.min(rangeX, rangeY);
//        minRange = Math.min(minRange, rangeZ);
//
//
//        double meanX = statsX.getMean();
//        double meanY = statsY.getMean();
//        double meanZ = statsZ.getMean();
//
//        double maxOfmean = Math.max(Math.max(meanX, meanY), meanZ);
//        double minOfmean = Math.min(Math.min(meanX, meanY), meanZ);
//        double meanOfmean = (meanX + meanY + meanZ) / 3.0;
//
//        double stdevX = statsX.getStandardDeviation();
//        double stdevY = statsY.getStandardDeviation();
//        double stdevZ = statsZ.getStandardDeviation();
//
//        double maxOfstd = Math.max(Math.max(stdevX, stdevY), stdevZ);
//        double minOfstd = Math.min(Math.min(stdevX, stdevY), stdevZ);
//        double meanOfstd = (stdevX + stdevY + stdevZ) / 3.0;
//
//
//        double varX = statsX.getVariance();
//        double varY = statsY.getVariance();
//        double varZ = statsZ.getVariance();
//
//        DescriptiveStatistics statsvar = new DescriptiveStatistics();
//        statsvar.addValue(varX);
//        statsvar.addValue(varY);
//        statsvar.addValue(varZ);
//
//        double maxVar = statsvar.getMax();
//        double minVar = statsvar.getMin();
//        double meanVar = statsvar.getMean();
//        double medianVar = statsvar.getPercentile(50);
//
//        double maxVmin = maxVar / minVar;
//        double maxVmean = maxVar / meanVar;
//        double maxVmedian = maxVar / medianVar;
//
//
//        double medX = statsX.getPercentile(50);
//        double medY = statsY.getPercentile(50);
//        double medZ = statsZ.getPercentile(50);
//
//        DescriptiveStatistics statsmed = new DescriptiveStatistics();
//        statsmed.addValue(medX);
//        statsmed.addValue(medY);
//        statsmed.addValue(medZ);
//
//        double maxOfmed = statsmed.getMax();
//        double minOfmed = statsmed.getMin();
//        double medOfmed = statsmed.getPercentile(50);
//
//        double[] diffX = diff(segx);
//        double[] diffY = diff(segy);
//        double[] diffZ = diff(segz);
//
//        DescriptiveStatistics statsDiffX = new DescriptiveStatistics();
//        DescriptiveStatistics statsDiffY = new DescriptiveStatistics();
//        DescriptiveStatistics statsDiffZ = new DescriptiveStatistics();
//
//        for (double d : diffX) {
//            statsDiffX.addValue(Math.abs(d));
//        }
//        for (double d : diffY) {
//            statsDiffY.addValue(Math.abs(d));
//        }
//        for (double d : diffZ) {
//            statsDiffZ.addValue(Math.abs(d));
//        }
//
//        double fdX = statsDiffX.getPercentile(50);
//        double fdY = statsDiffY.getPercentile(50);
//        double fdZ = statsDiffZ.getPercentile(50);
//
//        double maxFD = Math.max(Math.max(fdX, fdY), fdZ);
//        double minFD = Math.min(Math.min(fdX, fdY), fdZ);
//        double meanFD = (fdX + fdY + fdZ) / 3.0;
//
//        double[] crossingX = crossing(segx, statsX.getMean());
//        double[] crossingY = crossing(segy, statsY.getMean());
//        double[] crossingZ = crossing(segz, statsZ.getMean());
//
//        DescriptiveStatistics statsCrossing = new DescriptiveStatistics();
//        statsCrossing.addValue(crossingX.length);
//        statsCrossing.addValue(crossingY.length);
//        statsCrossing.addValue(crossingZ.length);
//
//        double maxOfCrossing = statsCrossing.getMax();
//        double minOfCrossing = statsCrossing.getMin();
//        double meanOfCrossing = statsCrossing.getPercentile(50);

        ArrayList<Double> stdMagnitudeArray = new ArrayList<>();
        for(int i=0; i<segxWindowed.size(); i++) {

            DataPoint[] wx = segxWindowed.get(i);
            DataPoint[] wy = segyWindowed.get(i);
            DataPoint[] wz = segzWindowed.get(i);


            double[] magnitude = Library.magnitude(wx, wy, wz);

            DescriptiveStatistics statsMagnitude = new DescriptiveStatistics();
            for (double d : magnitude) {
                statsMagnitude.addValue(d);
                MagnitudeStats.add(d);
            }
//        double avgMagnitude = statsMagnitude.getPercentile(50);
            stdMagnitudeArray.add(statsMagnitude.getStandardDeviation());
        }
        StdevMagnitude = new double[stdMagnitudeArray.size()];
        for(int i=0; i<StdevMagnitude.length; i++) {
            StdevMagnitude[i] = stdMagnitudeArray.get(i);
        }

        this.Activity = activityAnalysis(this.StdevMagnitude, MagnitudeStats); //From Autosense Matlab code


//        int NFFT = nextPower2(magnitude.length);
//
//        double[] bufferMagnitude = new double[NFFT];
//        System.arraycopy(magnitude, 0, bufferMagnitude, 0, magnitude.length);
//
//        FastFourierTransformer f = new FastFourierTransformer(DftNormalization.STANDARD);
//        Complex[] fftC = f.transform(bufferMagnitude, TransformType.FORWARD);
//        double[] fftMag = new double[fftC.length - 5];
//        for (int i = 5; i < fftC.length; i++) {
//            fftMag[i - 5] = fftC[i].abs() / NFFT;
//        }
//        double sum_fft_hf = 0;
//        double sum_fft_lf = 0;
//        if (bufferMagnitude.length > 16) {
//            int mid = (int) Math.floor(fftMag.length / 4.0);
//            for (int i = 0; i < mid; i++) {
//                sum_fft_lf += fftMag[i];
//            }
//            for (int i = mid; i < 2 * mid; i++) {
//                sum_fft_hf += fftMag[i];
//            }
//        }
//
//
//        double sum_fft_onehz = 0;
//        double sum_fft_twohz = 0;
//        double sum_fft_threehz = 0;
//        double sum_fft_fourhz = 0;
//        double sum_fft_fivehz = 0;
//        double ff;
//
//        for (int i = 0; i < NFFT / 2; i++) {
//            ff = samplingFreq / NFFT * i;
//            if (ff > 0 && ff <= 1)
//                sum_fft_onehz += fftMag[i];
//            if (ff > 1 && ff <= 2)
//                sum_fft_twohz += fftMag[i];
//            if (ff > 2 && ff <= 3)
//                sum_fft_threehz += fftMag[i];
//            if (ff > 3 && ff <= 4)
//                sum_fft_fourhz += fftMag[i];
//            if (ff > 4 && ff <= 5)
//                sum_fft_fivehz += fftMag[i];
//        }
//
//        double[] tempx = new double[nextPower2(segx.length)];
//        for (int i = 0; i < segx.length; i++) {
//            tempx[i] = segx[i].value;
//        }
//
//        double[] tempy = new double[nextPower2(segy.length)];
//        for (int i = 0; i < segy.length; i++) {
//            tempy[i] = segy[i].value;
//        }
//
//        double[] tempz = new double[nextPower2(segz.length)];
//        for (int i = 0; i < segz.length; i++) {
//            tempz[i] = segz[i].value;
//        }
//
//
//        Complex[] fftCompX = f.transform(tempx, TransformType.FORWARD); //Must be sized to a power of 2
//        Complex[] fftCompY = f.transform(tempy, TransformType.FORWARD); //Must be sized to a power of 2
//        Complex[] fftCompZ = f.transform(tempz, TransformType.FORWARD); //Must be sized to a power of 2
//
//        for (int i = 0; i < fftCompX.length; i++) {
//            fftCompX[i] = fftCompX[i].divide(segx.length);
//        }
//        for (int i = 0; i < fftCompY.length; i++) {
//            fftCompY[i] = fftCompY[i].divide(segy.length);
//        }
//        for (int i = 0; i < fftCompZ.length; i++) {
//            fftCompZ[i] = fftCompZ[i].divide(segz.length);
//        }
//
//
//        double compXSum = 0;
//        for (int i = 5; i < fftCompX.length; i++) {
//            compXSum += fftCompX[i].abs() * fftCompX[i].abs();
//        }
//
//        double compYSum = 0;
//        for (int i = 5; i < fftCompY.length; i++) {
//            compYSum += fftCompY[i].abs() * fftCompY[i].abs();
//        }
//
//        double compZSum = 0;
//        for (int i = 5; i < fftCompZ.length; i++) {
//            compZSum += fftCompZ[i].abs() * fftCompZ[i].abs();
//        }
//
//        DescriptiveStatistics statCompFFT = new DescriptiveStatistics();
//        statCompFFT.addValue(compXSum);
//        statCompFFT.addValue(compYSum);
//        statCompFFT.addValue(compZSum);
//
//        double avgEnergy = statCompFFT.getPercentile(50);
//
//        double energyX = compXSum;
//        double energyY = compYSum;
//        double energyZ = compZSum;
//
//        double totalEnergy = energyX + energyY + energyZ;
//
//        int inc = (int) Math.floor(magnitude.length / 10);
//        DescriptiveStatistics statEnergyX = new DescriptiveStatistics();
//        double[] data = new double[inc];
//        for (int i = 0; i < 10; i++) {
//            System.arraycopy(tempx, i * inc, data, 0, (i + 1) * inc - i * inc);
//            statEnergyX.addValue(computeEnergy(data, inc));
//        }
//        double medianEnergyX = statEnergyX.getPercentile(50);
//        double stdEnergyX = statEnergyX.getStandardDeviation();
//
//        DescriptiveStatistics statEnergyY = new DescriptiveStatistics();
//        data = new double[inc];
//        for (int i = 0; i < 10; i++) {
//            System.arraycopy(tempy, i * inc, data, 0, (i + 1) * inc - i * inc);
//            statEnergyY.addValue(computeEnergy(data, inc));
//        }
//        double medianEnergyY = statEnergyY.getPercentile(50);
//        double stdEnergyY = statEnergyY.getStandardDeviation();
//
//        DescriptiveStatistics statEnergyZ = new DescriptiveStatistics();
//        data = new double[inc];
//        for (int i = 0; i < 10; i++) {
//            System.arraycopy(tempz, i * inc, data, 0, (i + 1) * inc - i * inc);
//            statEnergyZ.addValue(computeEnergy(data, inc));
//        }
//        double medianEnergyZ = statEnergyZ.getPercentile(50);
//        double stdEnergyZ = statEnergyZ.getStandardDeviation();
//
//        DescriptiveStatistics statsStdEnergy = new DescriptiveStatistics();
//        statsStdEnergy.addValue(stdEnergyX);
//        statsStdEnergy.addValue(stdEnergyY);
//        statsStdEnergy.addValue(stdEnergyZ);
//
//        double maxStdEnergy = statsStdEnergy.getMax();
//        double meanStdEnergy = statsStdEnergy.getMean();
//        double minStdEnergy = statsStdEnergy.getMin();
//
//        DescriptiveStatistics statsMedianEnergy = new DescriptiveStatistics();
//        statsMedianEnergy.addValue(medianEnergyX);
//        statsMedianEnergy.addValue(medianEnergyY);
//        statsMedianEnergy.addValue(medianEnergyZ);
//
//        double maxOfmedianEnergy = statsMedianEnergy.getMax();
//        double meanOfmedianEnergy = statsMedianEnergy.getMean();
//        double minOfmedianEnergy = statsMedianEnergy.getMin();
//
//
//        double sumEnergyHFX = 0;
//        double sumEnergyLFX = 0;
//        double sumEnergyHFY = 0;
//        double sumEnergyLFY = 0;
//        double sumEnergyHFZ = 0;
//        double sumEnergyLFZ = 0;
//
//        if (segx.length > 16) {
//
//            NFFT = nextPower2(segx.length);
//
//            double[] bufferSegX = new double[NFFT];
//            System.arraycopy(tempx, 0, bufferSegX, 0, segx.length);
//
//            Complex[] fftXtemp = f.transform(bufferSegX, TransformType.FORWARD);
//            double[] fftx = new double[fftXtemp.length];
//            for (int i = 0; i < fftx.length; i++) {
//                fftx[i] = fftXtemp[i].abs();
//            }
//
//            int midx = (int) Math.floor(fftx.length / 4.0);
//            for (int i = 1; i < midx; i++) {
//                sumEnergyLFX += fftx[i];
//            }
//            for (int i = midx; i < 2 * midx; i++) {
//                sumEnergyHFX += fftx[i];
//            }
//        }
//
//        if (segy.length > 16) {
//
//            NFFT = nextPower2(segy.length);
//
//            double[] bufferSegY = new double[NFFT];
//            System.arraycopy(tempy, 0, bufferSegY, 0, segy.length);
//
//            Complex[] fftYtemp = f.transform(bufferSegY, TransformType.FORWARD);
//            double[] ffty = new double[fftYtemp.length];
//            for (int i = 0; i < ffty.length; i++) {
//                ffty[i] = fftYtemp[i].abs();
//            }
//
//            int midx = (int) Math.floor(ffty.length / 4.0);
//            for (int i = 1; i < midx; i++) {
//                sumEnergyLFY += ffty[i];
//            }
//            for (int i = midx; i < 2 * midx; i++) {
//                sumEnergyHFY += ffty[i];
//            }
//        }
//
//        if (segz.length > 16) {
//
//            NFFT = nextPower2(segz.length);
//
//            double[] bufferSegZ = new double[NFFT];
//            System.arraycopy(tempz, 0, bufferSegZ, 0, segz.length);
//
//            Complex[] fftZtemp = f.transform(bufferSegZ, TransformType.FORWARD);
//            double[] fftz = new double[fftZtemp.length];
//            for (int i = 0; i < fftz.length; i++) {
//                fftz[i] = fftZtemp[i].abs();
//            }
//
//            int midx = (int) Math.floor(fftz.length / 4.0);
//            for (int i = 1; i < midx; i++) {
//                sumEnergyLFZ += fftz[i];
//            }
//            for (int i = midx; i < 2 * midx; i++) {
//                sumEnergyHFZ += fftz[i];
//            }
//        }
//
//        DescriptiveStatistics statsEnergyLF = new DescriptiveStatistics();
//        statsEnergyLF.addValue(sumEnergyLFX);
//        statsEnergyLF.addValue(sumEnergyLFY);
//        statsEnergyLF.addValue(sumEnergyLFZ);
//
//        double maxEnergyLF = statsEnergyLF.getMax();
//        double minEnergyLF = statsEnergyLF.getMin();
//        double medianEnergyLF = statsEnergyLF.getPercentile(50);
//
//        DescriptiveStatistics statsEnergyHF = new DescriptiveStatistics();
//        statsEnergyHF.addValue(sumEnergyHFX);
//        statsEnergyHF.addValue(sumEnergyHFY);
//        statsEnergyHF.addValue(sumEnergyHFZ);
//
//        double maxEnergyHF = statsEnergyHF.getMax();
//        double minEnergyHF = statsEnergyHF.getMin();
//        double medianEnergyHF = statsEnergyHF.getPercentile(50);
//
//        double totalLFenergy = sumEnergyLFX + sumEnergyLFY + sumEnergyLFZ;
//        double totalHFenergy = sumEnergyHFX + sumEnergyHFY + sumEnergyHFZ;


        //Write to class member variables

//        this.MaxOfMean = maxOfmean;
//        this.MeanOfMean = meanOfmean;
//        this.MinOfMean = minOfmean;
//
//        this.MaxOfMin = maxOfmin;
//        this.MinOfMin = minOfmin;
//        this.MaxOfMax = maxOfmax;
//        this.MinOfMax = minOfmax;
//
//        this.MaxOfRange = maxRange;
//        this.MinOfRange = minRange;
//
//        this.MaxOfStd = maxOfstd;
//        this.MinOfStd = minOfstd;
//        this.MeanOfStd = meanOfstd;
//
//        this.MaxVar = maxVar;
//        this.MinVar = minVar;
//        this.MeanVar = meanVar;
//
//        this.MaxOfMed = maxOfmed;
//        this.MinOfMed = minOfmed;
//        this.MedOfMed = medOfmed;
//
//        this.MaxFD = maxFD;
//        this.MinFD = minFD;
//        this.MeanFD = meanFD;
//
//        this.MaxCrossing = maxOfCrossing;
//        this.MeanCrossing = meanOfCrossing;
//        this.MinCrossing = minOfCrossing;
//
//        this.TotalEnergy = totalEnergy;
//        this.MaxStdEnergy = maxStdEnergy;
//        this.MeanStdEnergy = meanStdEnergy;
//
//        this.MinStdEnergy = minStdEnergy;
//        this.MaxEnergyLF = maxEnergyLF;
//        this.MinEnergyLF = minEnergyLF;
//        this.MedianEnergyLF = medianEnergyLF;
//
//        this.MaxEnergyHF = maxEnergyHF;
//        this.MinEnergyHF = minEnergyHF;
//        this.MedianEnergyHF = medianEnergyHF;
//
//        this.TotalEnergyLF = totalLFenergy;
//        this.TotalEnergyHF = totalHFenergy;
//
//        this.AvgEnergy = avgEnergy;
//        this.AvgMagnitude = avgMagnitude;





//        this.MagnitudeFFTOneHz = sum_fft_onehz;
//        this.MagnitudeFFTTWoHz = sum_fft_twohz;
//        this.MagnitudeFFTThreeHz = sum_fft_threehz;
//        this.MagnitudeFFTFourHz = sum_fft_fourhz;
//        this.MagnitudeFFTFiveHz = sum_fft_fivehz;
//
//        this.MagnitudeFFTHF = sum_fft_hf;
//        this.MagnitudeFFTLF = sum_fft_lf;
//
//        this.MaxMinVarRatio = maxVmin;
//        this.MaxMeanVarRatio = maxVmean;
//        this.MaxMedianVarRatio = maxVmedian;
//
//        this.MaxOfmedianEnergy = maxOfmedianEnergy;
//        this.MinOfmedianEnergy = minOfmedianEnergy;
//        this.MeanOfmedianEnergy = meanOfmedianEnergy;
//
//        this.StartTimestamp = segx[0].timestamp;
//        this.EndTimestamp = segx[segx.length-1].timestamp;

    }

    public boolean activityAnalysis(double[] accelFeature, RunningStatistics magnitudeStats) {

        double lowlimit = magnitudeStats.getMean() - 3.0 * magnitudeStats.getStdev(); //Using this instead of percentile01
        double highlimit = magnitudeStats.getMean() + 3.0 * magnitudeStats.getStdev(); //Using this instead of percentile99
        double range = highlimit-lowlimit;

        boolean[] activityOrNot = new boolean[accelFeature.length];
        for(int i=0; i<accelFeature.length; i++) {
            activityOrNot[i] = accelFeature[i] > (lowlimit + 0.35 * range);
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
