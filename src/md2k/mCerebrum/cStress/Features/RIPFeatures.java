package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.SensorConfiguration;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Intercepts;
import md2k.mCerebrum.cStress.Structs.MaxMin;
import md2k.mCerebrum.cStress.Structs.PeakValley;
import org.apache.commons.math3.analysis.function.Exp;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
public class RIPFeatures {

    //ripfeature_extraction.m

    public DescriptiveStatistics InspDuration;
    public DescriptiveStatistics ExprDuration;
    public DescriptiveStatistics RespDuration;
    public DescriptiveStatistics Stretch;
    //public DescriptiveStatistics StretchUp;
    //public DescriptiveStatistics StretchDown;
    public DescriptiveStatistics IERatio;
    public DescriptiveStatistics RSA;

    //ripfeature_extraction_by_window
    private SensorConfiguration sensorConfig;

    public RIPFeatures() {

    }

    /**
     * Core Respiration Features
     * @param rip
     * @param ecg
     * @param sc
     */
    public RIPFeatures(DataPoint[] rip, ECGFeatures ecg, SensorConfiguration sc) {

        //Initialize statistics
        InspDuration = new DescriptiveStatistics();
        ExprDuration = new DescriptiveStatistics();
        RespDuration = new DescriptiveStatistics();
        Stretch = new DescriptiveStatistics();
        IERatio = new DescriptiveStatistics();
        RSA = new DescriptiveStatistics();

        sensorConfig = sc;


        PeakValley pvData = peakvalley_v2(rip); //There is no trailing valley in this output.


        for(int i=0; i<pvData.valleyIndex.size()-1; i++) {

            InspDuration.addValue(rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
            ExprDuration.addValue(rip[pvData.valleyIndex.get(i+1)].timestamp - rip[pvData.peakIndex.get(i)].timestamp);
            RespDuration.addValue(rip[pvData.valleyIndex.get(i+1)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
            Stretch.addValue(rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value);
            IERatio.addValue(InspDuration.getElement((int)InspDuration.getN()-1)-ExprDuration.getElement((int)ExprDuration.getN()-1));
            RSA.addValue(rsaCalculateCycle(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i+1)].timestamp, ecg) );
        }


    }

    private double rsaCalculateCycle(long starttime, long endtime, ECGFeatures ecg) {
        ArrayList<Integer> cInd = new ArrayList<>();
        for(int i=0; i<ecg.RRStatsTimestamps.size(); i++) {
            if (ecg.RRStatsTimestamps.get(i)>=starttime && ecg.RRStatsTimestamps.get(i)<endtime) {
                cInd.add(i);
            }
        }

        double max = ecg.RRStats.getElement(cInd.get(0));
        double min = ecg.RRStats.getElement(cInd.get(0));

        for(int i=0; i<cInd.size(); i++) {
            if (ecg.RRStats.getElement(i) > max) {
                max = ecg.RRStats.getElement(i);
            }
            if (ecg.RRStats.getElement(i) < min) {
                min = ecg.RRStats.getElement(i);
            }
        }

        return max-min;
    }


    public  PeakValley peakvalley_v2(DataPoint[] rip) {

        DataPoint[] sample = smooth(rip, 5);

        int windowLength = (int) Math.round(8.0 * sensorConfig.getFrequency("RIP"));

        DataPoint[] MAC = mac(sample, windowLength);

        ArrayList<Integer> upInterceptIndex = new ArrayList<>();
        ArrayList<Integer> downInterceptIndex = new ArrayList<>();

        for (int i = 1; i < MAC.length; i++) {
            if (sample[i - 1].value <= MAC[i - 1].value && sample[i].value > MAC[i].value) {
                upInterceptIndex.add(i - 1);
            } else if (sample[i - 1].value >= MAC[i - 1].value && sample[i].value < MAC[i].value) {
                downInterceptIndex.add(i - 1);
            }
        }

        Intercepts UIDI = InterceptOutlierDetectorRIPLamia(upInterceptIndex, downInterceptIndex, sample, windowLength);

        int[] UI = UIDI.UI;
        int[] DI = UIDI.DI;

        ArrayList<Integer> peakIndex = new ArrayList<>();
        ArrayList<Integer> valleyIndex = new ArrayList<>();

        for (int i = 0; i < DI.length-1; i++) {

            int peakindex = 0;
            double peakValue = -1e9;
            for (int j = UI[i]; j < DI[i + 1]; j++) {
                if (sample[j].value > peakValue) {
                    peakindex = j;
                    peakValue = sample[j].value;
                }
            }
            peakIndex.add(peakindex);

            DataPoint[] temp = new DataPoint[UI[i] - DI[i]];
            System.arraycopy(sample, DI[i], temp, 0, UI[i] - DI[i]);
                MaxMin maxMinTab = localMaxMin(temp, 1);

            if (maxMinTab.mintab.length == 0) {
                int valleyindex = 0;
                double valleyValue = 1e9;
                for (int j = DI[i]; j < UI[i]; j++) {
                    if (sample[j].value < valleyValue) {
                        valleyindex = j;
                        valleyValue = sample[j].value;
                    }
                }
                valleyIndex.add(valleyindex);
            } else {
                valleyIndex.add(DI[i] + (int) maxMinTab.mintab[maxMinTab.mintab.length - 1].timestamp - 1); //timestamp is index in this case
            }
        }

        double[] inspirationAmplitude = new double[valleyIndex.size()];
        double[] expirationAmplitude;

        double meanInspirationAmplitude = 0.0;
        double meanExpirationAmplitude;

        for (int i = 0; i < valleyIndex.size() - 1; i++) {
            inspirationAmplitude[i] = sample[peakIndex.get(i)].value - sample[valleyIndex.get(i)].value;

            meanInspirationAmplitude += inspirationAmplitude[i];
        }
        meanInspirationAmplitude /= (valleyIndex.size() - 1);

        ArrayList<Integer> finalPeakIndex = new ArrayList<>();
        ArrayList<Integer> finalValleyIndex = new ArrayList<>();

        for (int i = 0; i < inspirationAmplitude.length; i++) {
            if (inspirationAmplitude[i] > 0.15 * meanInspirationAmplitude) {
                finalPeakIndex.add(peakIndex.get(i));
                finalValleyIndex.add(valleyIndex.get(i));
            }
        }


        expirationAmplitude = new double[finalValleyIndex.size()-1];
        meanExpirationAmplitude = 0.0;
        for(int i=0; i<finalValleyIndex.size()-1; i++) {
            expirationAmplitude[i] = Math.abs(sample[finalValleyIndex.get(i+1)].value-sample[finalPeakIndex.get(i)].value);
            meanExpirationAmplitude += expirationAmplitude[i];
        }
        meanExpirationAmplitude /= (finalValleyIndex.size()-1);



        ArrayList<Integer> resultPeakIndex = new ArrayList<>();
        ArrayList<Integer> resultValleyIndex = new ArrayList<>();

        resultValleyIndex.add(finalValleyIndex.get(0));

        for (int i = 0; i < expirationAmplitude.length; i++) {
            if (expirationAmplitude[i] > 0.15 * meanExpirationAmplitude) {
                resultValleyIndex.add(finalValleyIndex.get(i + 1));
                resultPeakIndex.add(finalPeakIndex.get(i));
            }
        }
        resultPeakIndex.add(finalPeakIndex.get(finalPeakIndex.size() - 1));

        PeakValley result = new PeakValley();
        result.valleyIndex = resultValleyIndex;
        result.peakIndex = resultPeakIndex;

        return result;
    }

    /**
     * Moving Average Curve
     * @param sample
     * @param windowLength
     * @return
     */
    private DataPoint[] mac(DataPoint[] sample, int windowLength) {

        DataPoint[] result = new DataPoint[sample.length-2*windowLength];

        for(int i=windowLength;i < sample.length-windowLength; i++) {
            result[i-windowLength] = new DataPoint(0.0,0);
            for(int j=-windowLength; j<windowLength; j++) {
                result[i-windowLength].value += sample[i+j].value;
            }
            result[i-windowLength].value /= (2.0*windowLength); //Compute mean
            result[i-windowLength].timestamp = sample[i+windowLength].timestamp;

        }
        return result;
    }


    public MaxMin localMaxMin(DataPoint[] temp, int delta) {
        /*
        %PEAKDET Detect peaks in a vector
        %        [MAXTAB, MINTAB] = PEAKDET(V, DELTA) finds the local
        %        maxima and minima ("peaks") in the vector V.
        %        MAXTAB and MINTAB consists of two columns. Column 1
        %        contains indices in V, and column 2 the found values.
        %
        %        With [MAXTAB, MINTAB] = PEAKDET(V, DELTA, X) the indices
        %        in MAXTAB and MINTAB are replaced with the corresponding
        %        X-values.
        %
        %        A point is considered a maximum peak if it has the maximal
        %        value, and was preceded (to the left) by a value lower by
        %        DELTA.

        % Eli Billauer, 3.4.05 (Explicitly not copyrighted).
        % Timothy Hnat <twhnat@memphis.edu>, Reimplemented in Java
        % This function is released to the public domain; Any use is allowed.
         */
        MaxMin result = new MaxMin();
        ArrayList<DataPoint> maxtab = new ArrayList<>();
        ArrayList<DataPoint> mintab = new ArrayList<>();

        double mn = 1e9;
        double mx = -1e9;

        boolean lookformax = true;

        double tempvalue;
        int mxpos = -1;
        int mnpos = -1;

        for (int x = 0; x < temp.length; x++) {
            tempvalue = temp[x].value;
            if (tempvalue > mx) {
                mx = tempvalue;
                mxpos = x;
            }
            if (tempvalue < mn) {
                mn = tempvalue;
                mnpos = x;
            }

            if (lookformax) {
                if (tempvalue < mx - delta) {
                    DataPoint d = new DataPoint(mx, mxpos);
                    maxtab.add(d);
                    mn = tempvalue;
                    mnpos = x;
                    lookformax = false;
                }
            } else {
                if (tempvalue > mn + delta) {
                    DataPoint d = new DataPoint(mn, mnpos);
                    mintab.add(d);
                    mx = tempvalue;
                    mxpos = x;
                    lookformax = true;
                }
            }
        }


        maxtab.toArray(result.maxtab);
        mintab.toArray(result.mintab);

        return result;
    }

    /**
     * Intercept Outlier Detector
     * Reference: Intercept_outlier_detector_RIP_lamia.m
     * @param upInterceptIndex
     * @param downInterceptIndex
     * @param sample
     * @param windowLength
     * @return
     */
    public  Intercepts InterceptOutlierDetectorRIPLamia(ArrayList<Integer> upInterceptIndex, ArrayList<Integer> downInterceptIndex, DataPoint[] sample, int windowLength) {
        Intercepts result = new Intercepts();

        int minimumLength = Math.min(upInterceptIndex.size(), downInterceptIndex.size());

        ArrayList<Integer> D = new ArrayList<>();
        ArrayList<Integer> U = new ArrayList<>();
        for(int i=0; i<minimumLength; i++) {
            U.add(upInterceptIndex.get(i));
            D.add(downInterceptIndex.get(i));
        }

        ArrayList<Integer> UI = new ArrayList<>();
        ArrayList<Integer> DI = new ArrayList<>();

        int i = 0;
        int j = 0;
        while(i < U.size()-2) {
            if (j > (D.size()-1)) {
                break;
            }

            while(j < D.size()-1) {
               if(U.get(0) < D.get(0)) {
                   if (i == U.size() || j == D.size()) {
                       break;
                   }

                   if (U.get(i) < D.get(j) && D.get(j) < U.get(i+1)) {
                       UI.add(U.get(i));
                       ArrayList<Integer> ind = new ArrayList<>();
                       for (Integer aD : D) {
                           if ((aD > D.get(j)) && (aD < U.get(i+1))) {
                               ind.add(aD);
                           }
                       }
                       if (ind.size() == 0) {
                           DI.add(D.get(j));
                           j++;
                       } else {
                           DI.add(ind.get(ind.size()-1));
                           j = ind.get(ind.size()-1)+1;
                       }
                       i++;
                   } else if (U.get(i) < D.get(j) && D.get(j) > U.get(i+1)) {
                       DI.add(D.get(i));
                       ArrayList<Integer> ind = new ArrayList<>();
                       for (Integer aU : U) {
                           if ((aU > U.get(i)) && (aU < D.get(j))) {
                               ind.add(aU);
                           }
                       }
                       if (ind.size() == 0) {
                           UI.add(U.get(i));
                           i++;
                       } else {
                           UI.add(ind.get(ind.size()-1));
                           i = ind.get(ind.size()-1)+1;
                       }
                       j++;
                   }
               } else if (D.get(0) < U.get(0)) {
                   if (i == D.size() || j == U.size()) {
                       break;
                   }

                   if (D.get(i) < U.get(j) && U.get(j) < D.get(i+1)) {
                       DI.add(D.get(i));
                       ArrayList<Integer> ind = new ArrayList<>();
                       for (Integer aU : U) {
                           if ((aU > U.get(j)) && (aU < D.get(i+1))) {
                               ind.add(aU);
                           }
                       }
                       if (ind.size() == 0) {
                           UI.add(U.get(j));
                           j++;
                       } else {
                           UI.add(ind.get(ind.size()-1));
                           j = ind.get(ind.size()-1)+1;
                       }
                       i++;
                   } else if (D.get(i) < U.get(j) && U.get(j) > D.get(i+1)) {
                       UI.add(U.get(i));
                       ArrayList<Integer> ind = new ArrayList<>();
                       for (Integer aD : D) {
                           if ((aD > D.get(i)) && (aD < U.get(j))) {
                               ind.add(aD);
                           }
                       }
                       if (ind.size() == 0) {
                           DI.add(D.get(i));
                           i++;
                       } else {
                           DI.add(ind.get(ind.size()-1));
                           i = ind.get(ind.size()-1)+1;
                       }
                       j++;
                   }
               }
            }
        }

        if (UI.size() ==0 && DI.size() == 0) {
            return result;
        }
        if (UI.get(0) < DI.get(0)) {
            UI.remove(0);
        }

        minimumLength = Math.min(UI.size(),DI.size());
        while(UI.size() > minimumLength) {
            UI.remove(UI.size()-1);
        }
        while(DI.size() > minimumLength) {
            DI.remove(DI.size()-1);
        }


        ArrayList<Integer> DownIntercept = new ArrayList<>();
        ArrayList<Integer> UpIntercept = new ArrayList<>();
        double fr;
        for(int ii=0; ii<DI.size()-1; ii++) {
            fr = 60000.0 / (sample[DI.get(ii+1)].timestamp - sample[DI.get(ii)].timestamp);
            if (fr >= 8.0 && fr <= 65) {
                DownIntercept.add(DI.get(ii));
                UpIntercept.add(UI.get(ii));
            }
        }


        ArrayList<Integer> DownIntercept2 = new ArrayList<>();
        ArrayList<Integer> UpIntercept2 = new ArrayList<>();
        double equivalentSamplePoints = 8.0/20.0 * sensorConfig.getFrequency("RIP");
        double upToDownDistance;
        for(int ii=0; ii<DownIntercept.size()-1; ii++) {
            upToDownDistance = DownIntercept.get(ii+1)-UpIntercept.get(ii)+1;
            if(upToDownDistance > equivalentSamplePoints) {
                UpIntercept2.add(UpIntercept.get(ii));
                DownIntercept2.add(DownIntercept.get(ii));
            }
        }


        result.UI = new int[UpIntercept2.size()];
        result.DI = new int[UpIntercept2.size()];
        for(int ii=0; ii<UpIntercept2.size(); ii++) {
            result.UI[ii] = UpIntercept2.get(ii);
            result.DI[ii] = DownIntercept2.get(ii);
        }

        return result;
    }

    public  DataPoint[] smooth(DataPoint[] rip, int n) {
        //Reimplementation of Matlab's smooth function
        DataPoint[] result = new DataPoint[rip.length];

        int windowSize = 1;
        double sum;
        for(int i=0; i<rip.length; i++) {
            sum = 0.0;
            int startingPoint;
            if( (rip.length-i+1) < n ) {
                startingPoint = rip.length-windowSize;
            } else {
                startingPoint = (int) Math.max(Math.floor(i - n / 2), 0);
            }
            for(int j=startingPoint; j<startingPoint+windowSize; j++) {
                sum += rip[j].value;
            }
            sum /= (double) windowSize;

            result[i] = new DataPoint(sum, rip[i].timestamp);

            if(windowSize < n && (rip.length-i) > n) { //Increase windowSize until n
                windowSize += 2;
            } else if ( (rip.length-i+1) < n) {
                windowSize -= 2;
            }
        }


        return result;
    }

}
