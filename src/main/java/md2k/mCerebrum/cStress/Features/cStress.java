package md2k.mCerebrum.cStress.features;

/*
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

import md2k.mCerebrum.cStress.library.DataArrayStream;
import md2k.mCerebrum.cStress.library.DataPointStream;
import md2k.mCerebrum.cStress.library.DataStreams;
import md2k.mCerebrum.cStress.library.structs.DataPointArray;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class cStress {

    public cStress(DataStreams datastreams) {
        try {
            DataPointArray fv = computeStressFeatures(datastreams);
            DataArrayStream fvStream = datastreams.getDataArrayStream("org.md2k.cstress.fv");
            fvStream.add(fv);
        } catch (IndexOutOfBoundsException e) {
            //Ignore this error
        }
    }

    /**
     * Extract and compute the 37 features that are needed for StreamProcessor's model
     *
     * @param datastreams Global DataStreams object
     * @return FV
     */
    private DataPointArray computeStressFeatures(DataStreams datastreams) {

        /* List of features for SVM model

         ECG - RR interval variance
         ECG - RR interval quartile deviation
         ECG - RR interval low frequency energy

         ECG - RR interval medium frequency energy
         *ECG - RR interval high frequency energy
         *ECG - RR interval low-high frequency energy ratio
         *ECG - RR interval mean
         ECG - RR interval median
         *ECG - RR interval 80th percentile
         ECG - RR interval 20th percentile
         ECG - RR interval heart-rate
         */

        DescriptiveStatistics RRint = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr_value").getNormalizedValues());
        double ECG_RR_Interval_Variance = RRint.getVariance();
        double ECG_RR_Interval_Quartile_Deviation = (RRint.getPercentile(75) - RRint.getPercentile(25)) / 2.0;

        DataPointStream lombLE = datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr.LombLowFrequencyEnergy");
        double ECG_RR_Interval_Low_Frequency_Energy = (lombLE.data.get(0).value - lombLE.stats.getMean()) / lombLE.stats.getStandardDeviation();

        DataPointStream lombME = datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr.LombMediumFrequencyEnergy");
        double ECG_RR_Interval_Medium_Frequency_Energy = (lombME.data.get(0).value - lombME.stats.getMean()) / lombME.stats.getStandardDeviation();

        DataPointStream lombHE = datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr.LombHighFrequencyEnergy");
        double ECG_RR_Interval_High_Frequency_Energy = (lombHE.data.get(0).value - lombHE.stats.getMean()) / lombHE.stats.getStandardDeviation();

        DataPointStream lombLH = datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr.LowHighFrequencyEnergyRatio");
        double ECG_RR_Interval_Low_High_Frequency_Energy_Ratio = (lombLH.data.get(0).value - lombLH.stats.getMean()) / lombLH.stats.getStandardDeviation();

        double ECG_RR_Interval_Mean = RRint.getMean();
        double ECG_RR_Interval_Median = RRint.getPercentile(50);
        double ECG_RR_Interval_80thPercentile = RRint.getPercentile(80);
        double ECG_RR_Interval_20thPercentile = RRint.getPercentile(20);

        DescriptiveStatistics heartrate = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr.heartrate").getNormalizedValues());
        double ECG_RR_Interval_Heart_Rate = heartrate.getMean();

         /*
         RIP - Inspiration Duration - quartile deviation
         RIP - Inspiration Duration - mean
         RIP - Inspiration Duration - median
         RIP - Inspiration Duration - 80th percentile
         */

        DescriptiveStatistics InspDuration = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.rip.inspduration").getNormalizedValues());

        double RIP_Inspiration_Duration_Quartile_Deviation = (InspDuration.getPercentile(75) - InspDuration.getPercentile(25)) / 2.0;
        double RIP_Inspiration_Duration_Mean = InspDuration.getMean();
        double RIP_Inspiration_Duration_Median = InspDuration.getPercentile(50);
        double RIP_Inspiration_Duration_80thPercentile = InspDuration.getPercentile(80);

         /*
         RIP - Expiration Duration - quartile deviation
         RIP - Expiration Duration - mean
         RIP - Expiration Duration - median
         RIP - Expiration Duration - 80th percentile
         */

        DescriptiveStatistics ExprDuration = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.rip.exprduration").getNormalizedValues());

        double RIP_Expiration_Duration_Quartile_Deviation = (ExprDuration.getPercentile(75) - ExprDuration.getPercentile(25)) / 2.0;
        double RIP_Expiration_Duration_Mean = ExprDuration.getMean();
        double RIP_Expiration_Duration_Median = ExprDuration.getPercentile(50);
        double RIP_Expiration_Duration_80thPercentile = ExprDuration.getPercentile(80);
         /*
         RIP - Respiration Duration - quartile deviation
         RIP - Respiration Duration - mean
         RIP - Respiration Duration - median
         RIP - Respiration Duration - 80th percentile
         */

        DescriptiveStatistics RespDuration = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.rip.respduration").getNormalizedValues());

        double RIP_Respiration_Duration_Quartile_Deviation = (RespDuration.getPercentile(75) - RespDuration.getPercentile(25)) / 2.0;
        double RIP_Respiration_Duration_Mean = RespDuration.getMean();
        double RIP_Respiration_Duration_Median = RespDuration.getPercentile(50);
        double RIP_Respiration_Duration_80thPercentile = RespDuration.getPercentile(80);

         /*
         RIP - Inspiration-Expiration Duration Ratio - quartile deviation
         *RIP - Inspiration-Expiration Duration Ratio - mean
         RIP - Inspiration-Expiration Duration Ratio - median
         RIP - Inspiration-Expiration Duration Ratio - 80th percentile
         */
        DescriptiveStatistics InspExprDuration = new DescriptiveStatistics((datastreams.getDataPointStream("org.md2k.cstress.data.rip.IERatio")).getNormalizedValues());

        double RIP_Inspiration_Expiration_Duration_Quartile_Deviation = (InspExprDuration.getPercentile(75) - InspExprDuration.getPercentile(25)) / 2.0;
        double RIP_Inspiration_Expiration_Duration_Mean = InspExprDuration.getMean();
        double RIP_Inspiration_Expiration_Duration_Median = InspExprDuration.getPercentile(50);
        double RIP_Inspiration_Expiration_Duration_80thPercentile = InspExprDuration.getPercentile(80);

         /*
         RIP - Stretch - quartile deviation
         RIP - Stretch - mean
         *RIP - Stretch - median
         RIP - Stretch - 80th percentile
         */
        DescriptiveStatistics Stretch = new DescriptiveStatistics(datastreams.getDataPointStream("org.md2k.cstress.data.rip.stretch").getNormalizedValues());

        double RIP_Stretch_Quartile_Deviation = (Stretch.getPercentile(75) - Stretch.getPercentile(25)) / 2.0;
        double RIP_Stretch_Mean = Stretch.getMean();
        double RIP_Stretch_Median = Stretch.getPercentile(50);
        double RIP_Stretch_80thPercentile = Stretch.getPercentile(80);
         /*
         *RIP - Breath-rate
         */
        DataPointStream breathRate = datastreams.getDataPointStream("org.md2k.cstress.data.rip.BreathRate");
        double RIP_Breath_Rate = (breathRate.data.get(0).value - breathRate.stats.getMean()) / breathRate.stats.getStandardDeviation();

         /*
         *RIP - Inspiration Minute Volume
         */
        DataPointStream minVent = datastreams.getDataPointStream("org.md2k.cstress.data.rip.MinuteVentilation");
        double RIP_Inspiration_Minute_Ventilation = (minVent.data.get(0).value - minVent.stats.getMean()) / minVent.stats.getStandardDeviation();

         /*
         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - quartile deviation
         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - mean
         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - median
         RIP+ECG - Respiratory Sinus Arrhythmia (RSA) - 80th percentile
         */

        DescriptiveStatistics RSA = new DescriptiveStatistics((datastreams.getDataPointStream("org.md2k.cstress.data.rip.RSA")).getNormalizedValues());

        double RSA_Quartile_Deviation = (RSA.getPercentile(75) - RSA.getPercentile(25)) / 2.0;
        double RSA_Mean = RSA.getMean();
        double RSA_Median = RSA.getPercentile(50);
        double RSA_80thPercentile = RSA.getPercentile(80);

        List<Double> featureVector = new ArrayList<Double>();


        featureVector.add(ECG_RR_Interval_Variance);// 1
        featureVector.add(ECG_RR_Interval_Low_High_Frequency_Energy_Ratio);// 2
        featureVector.add(ECG_RR_Interval_High_Frequency_Energy);// 3
        featureVector.add(ECG_RR_Interval_Medium_Frequency_Energy);// 4
        featureVector.add(ECG_RR_Interval_Low_Frequency_Energy);// 5
        featureVector.add(ECG_RR_Interval_Mean);// 6
        featureVector.add(ECG_RR_Interval_Median);// 7
        featureVector.add(ECG_RR_Interval_Quartile_Deviation);// 8
        featureVector.add(ECG_RR_Interval_80thPercentile);// 9
        featureVector.add(ECG_RR_Interval_20thPercentile);// 10
        featureVector.add(ECG_RR_Interval_Heart_Rate);// 11

        featureVector.add(RIP_Breath_Rate);// 12
        featureVector.add(RIP_Inspiration_Minute_Ventilation);// 13

        featureVector.add(RIP_Inspiration_Duration_Quartile_Deviation);// 14
        featureVector.add(RIP_Inspiration_Duration_Mean);// 15
        featureVector.add(RIP_Inspiration_Duration_Median);// 16
        featureVector.add(RIP_Inspiration_Duration_80thPercentile);// 17

        featureVector.add(RIP_Expiration_Duration_Quartile_Deviation);// 18
        featureVector.add(RIP_Expiration_Duration_Mean);// 19
        featureVector.add(RIP_Expiration_Duration_Median);// 20
        featureVector.add(RIP_Expiration_Duration_80thPercentile);// 21

        featureVector.add(RIP_Respiration_Duration_Quartile_Deviation);// 22
        featureVector.add(RIP_Respiration_Duration_Mean);// 23
        featureVector.add(RIP_Respiration_Duration_Median);// 24
        featureVector.add(RIP_Respiration_Duration_80thPercentile);// 25

        featureVector.add(RIP_Inspiration_Expiration_Duration_Quartile_Deviation);// 26
        featureVector.add(RIP_Inspiration_Expiration_Duration_Mean);// 27
        featureVector.add(RIP_Inspiration_Expiration_Duration_Median);// 28
        featureVector.add(RIP_Inspiration_Expiration_Duration_80thPercentile);// 29

        featureVector.add(RIP_Stretch_Quartile_Deviation);// 30
        featureVector.add(RIP_Stretch_Mean);// 31
        featureVector.add(RIP_Stretch_Median);// 32
        featureVector.add(RIP_Stretch_80thPercentile);// 33

        featureVector.add(RSA_Quartile_Deviation);// 34
        featureVector.add(RSA_Mean);// 35
        featureVector.add(RSA_Median);// 36
        featureVector.add(RSA_80thPercentile);// 37


        for (Double aFeatureVector : featureVector) {
            if (Double.isNaN(aFeatureVector)) {
                throw new NotANumberException();
            }
        }
        return new DataPointArray(datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, featureVector);
    }
}
