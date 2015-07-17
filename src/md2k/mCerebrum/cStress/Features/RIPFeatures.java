package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.SensorConfiguration;
import md2k.mCerebrum.cStress.Structs.DataPoint;
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

    public RIPFeatures(DataPoint[] rip, ECGFeatures ecg, SensorConfiguration sc) {

        //Initialize statistics
        InspDuration = new DescriptiveStatistics();
        ExprDuration = new DescriptiveStatistics();
        RespDuration = new DescriptiveStatistics();
        Stretch = new DescriptiveStatistics();
        //StretchUp = new DescriptiveStatistics();
        //StretchDown = new DescriptiveStatistics();
        IERatio = new DescriptiveStatistics();
        RSA = new DescriptiveStatistics();

        sensorConfig = sc;


        ArrayList<DataPoint[]> windowedData = window(rip);

        for (DataPoint[] darray : windowedData) {
            DescriptiveStatistics statsSeg = new DescriptiveStatistics();
            for (DataPoint d : darray) {
                statsSeg.addValue(d.value);
            }
            double min = statsSeg.getMin();
            double max = statsSeg.getMax();

            //double median = ripMedian; //TODO: Is this the correct way to get median?  Currently it is using mean from a daily stats tracker.

            //TODO: This doesn't appear to be correct.  See peakvalley_v2.m
            int peakindex = 0;
            double peakValue = -1e9;
            for (int i = 0; i < darray.length; i++) {
                if (darray[i].value > peakValue) {
                    peakindex = i;
                }
            }

            InspDuration.addValue(darray[peakindex].timestamp - darray[0].timestamp);
            ExprDuration.addValue(darray[darray.length].timestamp - darray[peakindex].timestamp);
            RespDuration.addValue(darray[darray.length].timestamp - darray[0].timestamp);
            Stretch.addValue(max - min); //TODO: Verify what this is: BreathStretchCalculate.m
            //StretchUp.addValue(max - median);
            //StretchDown.addValue(median - min);
            IERatio.addValue( (darray[peakindex].timestamp - darray[0].timestamp) / (darray[darray.length].timestamp - darray[peakindex].timestamp)   );

            //RSA.addValue(rsaCalculateCycle(darray, ecg) );






        }


    }

    private ArrayList<DataPoint[]> window(DataPoint[] rip) {
        ArrayList<DataPoint[]> result = new ArrayList<>();

        //TODO: Windowing code here


        return result;
    }

    private PeakValley peakvalley_v2(DataPoint[] rip) {

        DataPoint[] sample = smooth(rip, 5);

        int windowLength= (int) Math.round(8.0 * sensorConfig.getFrequency("RIP"));

        DataPoint[] MAC = smooth(sample, windowLength); //TODO: Verify the purpose the MATLAB's moving average and the difference between smooth

        ArrayList<Integer> upInterceptIndex = new ArrayList<>();
        ArrayList<Integer> downInterceptIndex = new ArrayList<>();

        for(int i=1; i<MAC.length; i++) {
            if (sample[i-1].value <= MAC[i-1].value && sample[i].value > MAC[i].value) {
                upInterceptIndex.add(i-1);
            } else if (sample[i-1].value >= MAC[i-1].value && sample[i].value < MAC[i].value) {
                downInterceptIndex.add(i-1);
            }
        }

        Intercepts UIDI = InterceptOutlierDetectorRIPLamia(upInterceptIndex,downInterceptIndex,sample,windowLength);

        int[] UI = UIDI.UI;
        int[] DI = UIDI.DI;

        ArrayList<Integer> peakIndex = new ArrayList<>();
        ArrayList<Integer> valleyIndex = new ArrayList<>();

        for(int i=0; i<UI.length; i++) {

            int peakindex = 0;
            double peakValue = -1e9;
            for (int j = UI[i]; j < DI[i+1]; j++) {
                if (sample[j].value > peakValue) {
                    peakindex = j;
                }
            }
            peakIndex.add(peakindex);

            DataPoint[] temp = new DataPoint[UI[i]-DI[i]];
            System.arraycopy(sample,DI[i],temp,0,UI[i]-DI[i]);
            MaxMin maxMinTab = localMaxMin(temp, 1);

            if (maxMinTab.mintab.length == 0) {
                int valleyindex = 0;
                double valleyValue = 1e9;
                for (int j = DI[i]; j < UI[i]; j++) {
                    if (sample[j].value < valleyValue) {
                        valleyindex = j;
                    }
                }
                valleyIndex.add(valleyindex);
            } else {
                valleyIndex.add(DI[i]+(int)maxMinTab.mintab[maxMinTab.mintab.length-1].timestamp - 1); //timestamp is index in this case
            }
        }

        double[] inspirationAmplitude = new double[valleyIndex.size()];
        double[] expirationAmplitude = new double[valleyIndex.size()];

        double meanInspirationAmplitude = 0.0;
        double meanExpirationAmplitude = 0.0;

        for(int i=0; i<valleyIndex.size()-1; i++) {
            inspirationAmplitude[i] = sample[peakIndex.get(i)].value - sample[valleyIndex.get(i)].value;
            expirationAmplitude[i] = Math.abs(sample[valleyIndex.get(i+1)].value - sample[peakIndex.get(i)].value);

            meanInspirationAmplitude += inspirationAmplitude[i];
            meanExpirationAmplitude += expirationAmplitude[i];
        }
        meanInspirationAmplitude /= (valleyIndex.size()-1);
        meanExpirationAmplitude /= (valleyIndex.size()-1);

        ArrayList<Integer> finalPeakIndex = new ArrayList<>();
        ArrayList<Integer> finalValleyIndex = new ArrayList<>();

        for(int i=0; i<inspirationAmplitude.length; i++) {
            if( inspirationAmplitude[i] > 0.15*meanInspirationAmplitude ) {
                finalPeakIndex.add(peakIndex.get(i));
                finalValleyIndex.add(valleyIndex.get(i));
            }
        }

        ArrayList<Integer> resultPeakIndex = new ArrayList<>();
        ArrayList<Integer> resultValleyIndex = new ArrayList<>();

        resultValleyIndex.add(finalValleyIndex.get(0));

        for(int i=0; i<expirationAmplitude.length; i++) {
            if( expirationAmplitude[i] > 0.15*meanExpirationAmplitude ) {
                resultValleyIndex.add(finalValleyIndex.get(i+1));
                resultPeakIndex.add(finalPeakIndex.get(i));
            }
        }
        resultPeakIndex.add(finalPeakIndex.get(finalPeakIndex.size()-1));

        PeakValley result = new PeakValley();
        result.valleyIndex = resultValleyIndex;
        result.peakIndex = resultPeakIndex;

        return result;
    }

    private class MaxMin {
        public DataPoint[] maxtab;
        public DataPoint[] mintab;

        public MaxMin() {
            maxtab = new DataPoint[0];
            mintab = new DataPoint[0];
        }
    }

    private MaxMin localMaxMin(DataPoint[] temp, int delta) {
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

        for(int x=0; x<temp.length; x++) {
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
                if ( tempvalue < mx-delta ) {
                    DataPoint d = new DataPoint(mx, mxpos);
                    maxtab.add(d);
                    mn = tempvalue;
                    mnpos = x;
                    lookformax = false;
                }
            } else {
                if ( tempvalue > mn+delta ) {
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

    private Intercepts InterceptOutlierDetectorRIPLamia(ArrayList<Integer> upInterceptIndex, ArrayList<Integer> downInterceptIndex, DataPoint[] sample, int windowLength) {
        //TODO: Intercept_outlier_detector_RIP_lamia.m

        Intercepts result = new Intercepts();

        int minimumLength = Math.min(upInterceptIndex.size(), downInterceptIndex.size());
        if(upInterceptIndex.size() < minimumLength) {
            upInterceptIndex = (ArrayList<Integer>) upInterceptIndex.subList(0,minimumLength-1);
        }
        if(downInterceptIndex.size() < minimumLength) {
            downInterceptIndex = (ArrayList<Integer>) downInterceptIndex.subList(0,minimumLength-1);
        }

        //TODO: Needs work from Rummana


        return result;
    }

    private DataPoint[] smooth(DataPoint[] rip, int n) {
        DataPoint[] result = new DataPoint[rip.length];

        DataPoint output;
        double[] buffer = new double[n];
        double sum = 0.0;

        for(int i=0; i < rip.length; i++) {
            sum -= buffer[i % n];
            buffer[i % n] = rip[i].value;
            sum += rip[i].value;

            if (i > n) {
                output = new DataPoint(buffer[i%n]/(i+1), rip[i].timestamp);
            } else {
                output = new DataPoint(buffer[i%n]/n, rip[i].timestamp);
            }
            result[i] = output;
        }

        return result;
    }


//    private double rsaCalculateCycle(DataPoint[] seg, ECGFeatures ecgFeatures) {
//        //TODO: Fix me
//
//        DataPoint[] ecg_rr = ecgFeatures.computeRR();
//
//
//        return 0;
//    }


    private class Intercepts {
        public int[] UI;
        public int[] DI;
    }



    private class PeakValley {
        public ArrayList<Integer> peakIndex;
        public ArrayList<Integer> valleyIndex;
    }
}
