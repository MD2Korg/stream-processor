package md2k.mCerebrum.cStress;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.PUFFMARKER;
import md2k.mCerebrum.cStress.Features.AccelGyroFeatures;
import md2k.mCerebrum.cStress.Features.AccelerometerFeatures;
import md2k.mCerebrum.cStress.Features.ECGFeatures;
import md2k.mCerebrum.cStress.Features.RIPFeatures;
import md2k.mCerebrum.cStress.Library.DataArrayStream;
import md2k.mCerebrum.cStress.Library.DataPointStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Structs.DataPointArray;
import md2k.mCerebrum.cStress.Library.Time;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;


/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
 * - Nazir Saleneen <nsleheen@memphis.edu>
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
 * Main class that implements StreamProcessor and controls the data processing pipeline
 */
public class StreamProcessor {

    long windowStartTime = -1;

    long windowSize;
    private String participant;
    private String path;

    private DataStreams datastreams = new DataStreams();


    /**
     * Main constructor for StreamProcessor
     *
     * @param windowSize  Time in milliseconds to segment and buffer data before processing
     * @param path        Location where data will be persisted on disk
     * @param participant A participant identifier that should identify a directory within 'path'
     */
    public StreamProcessor(long windowSize, String path, String participant) {
        this.windowSize = windowSize;
        this.participant = participant;
        this.path = path;

        //Configure Data Streams
        datastreams.getDataPointStream("org.md2k.cstress.data.ecg").metadata.put("frequency", 64.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.ecg").metadata.put("channelID", AUTOSENSE.CHEST_ECG);

        datastreams.getDataPointStream("org.md2k.cstress.data.rip").metadata.put("frequency", 64.0 / 3.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.rip").metadata.put("channelID", AUTOSENSE.CHEST_RIP);

        datastreams.getDataPointStream("org.md2k.cstress.data.accelx").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accelx").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream("org.md2k.cstress.data.accely").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accely").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream("org.md2k.cstress.data.accelz").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accelz").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        resetDataStreams();

    }

    /**
     * Main computation loop that processes all buffered data, computes a feature vector, and evaluates stress
     *
     * @return Probability of stress
     */
    public void process() {

        //TODO: This should be moved outside of the stress applications to intercept data before arriving here
//            //This check must happen before any normalization.  It operates on the RAW signals.
//            RipQualityCalculation ripQuality = new RipQualityCalculation(5, 50, 4500, 20, 2, 20, 150);
//            ECGQualityCalculation ecgQuality = new ECGQualityCalculation(3, 50, 4500, 20, 2, 47);
//
//            if (!ripQuality.computeQuality(rip, 5 * 1000, 0.67) || !ecgQuality.computeQuality(ecg, 5 * 1000, 0.67)) { //Check for 67% of the data to be of Quality within 5 second windows.
//                return probabilityOfStress; //data quality failure
//            }
//
//

        try {
            AccelerometerFeatures af = new AccelerometerFeatures(datastreams, AUTOSENSE.ACTIVITY_THRESHOLD, AUTOSENSE.ACCEL_WINDOW_SIZE);
            ECGFeatures ef = new ECGFeatures(datastreams);
            RIPFeatures rf = new RIPFeatures(datastreams);


            String[] wristList = new String[]{PUFFMARKER.LEFT_WRIST, PUFFMARKER.RIGHT_WRIST};
            for (String wrist : wristList) {
                AccelGyroFeatures agf = new AccelGyroFeatures(datastreams, wrist);
                DataPointStream gyr_intersections = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.intersections" + wrist);
                for (int i = 0; i < gyr_intersections.data.size(); i++) {
                    int startIndex = (int) gyr_intersections.data.get(i).timestamp;
                    int endIndex = (int) gyr_intersections.data.get(i).value;
                    DataPointArray fv = computePuffMarkerFeatures(datasreams, wrist, startIndex, endIndex);
                }
            }


        } catch (IndexOutOfBoundsException e) {
            System.err.println("Stress Exception Handler: IndexOutOfBoundsException");
            //e.printStackTrace();
        }


    }

    public boolean checkValidRollPitch(DataPointStream rolls, DataPointStream pitchs) {
        double x = (pitchs.descriptiveStats.getPercentile(50) - PUFFMARKER.PUFFMARKER_PITCH_MEAN) / PUFFMARKER.PUFFMARKER_PITCH_STD;
        double y = (rolls.descriptiveStats.getPercentile(50) - PUFFMARKER.PUFFMARKER_ROLL_MEAN) / PUFFMARKER.PUFFMARKER_ROLL_STD;
        double error = x * x; //;
//        double error = Math.sqrt(x * x+y*y); //;
        if (error > PUFFMARKER.PUFFMARKER_TH[0])
            return false;
        return true;
    }

    public void generateResults() {
        try {
            DataPointArray fv = computeStressFeatures(datastreams);
            DataArrayStream fvStream = datastreams.getDataArrayStream("org.md2k.cstress.fv");
            fvStream.add(fv);
        } catch (NotANumberException e) {
            System.err.println("Feature vector generation error");
        }

        //TODO Stress should be written out as a datastream
//                probabilityOfStress = evaluteStressModel(accelFeatures, ecgFeatures, ripFeatures, AUTOSENSE.STRESS_PROBABILTY_THRESHOLD);

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

        DescriptiveStatistics RRint = new DescriptiveStatistics((datastreams.getDataPointStream("org.md2k.cstress.data.ecg.rr_value")).getNormalizedValues());
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


        boolean valid = true;
        for (int i = 0; i < featureVector.size(); i++) {
            if (Double.isNaN(featureVector.get(i))) {
                valid = false;
                throw new NotANumberException();
            }
        }
        return new DataPointArray(windowStartTime, featureVector);
    }


    private DataPointArray computePuffMarkerFeatures(DataStreams datastreams) {

        /////////////// WRIST FEATURES ////////////////////////
        DataPointStream gyr_mag = new DataPointStream("org.md2k.cstress.data.gyr.mag" + wrist + ".segment", (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag" + wrist)).data.subList(startIndex, endIndex));
        DataPointStream gyr_mag_800 = new DataPointStream("org.md2k.cstress.data.gyr.mag_800" + wrist + ".segment", (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag_800" + wrist)).data.subList(startIndex, endIndex));
        DataPointStream gyr_mag_8000 = new DataPointStream("org.md2k.cstress.data.gyr.mag_8000" + wrist + ".segment", (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag_8000" + wrist)).data.subList(startIndex, endIndex));

        DataPointStream rolls = new DataPointStream("org.md2k.cstress.data.roll" + wrist + ".segment", (datastreams.getDataPointStream("org.md2k.cstress.data.roll" + wrist)).data.subList(startIndex, endIndex));
        DataPointStream pitchs = new DataPointStream("org.md2k.cstress.data.pitch" + wrist + ".segment", (datastreams.getDataPointStream("org.md2k.cstress.data.pitch" + wrist)).data.subList(startIndex, endIndex));

            /*
            Three filtering criteria
             */
        double meanHeight = gyr_mag_800.descriptiveStats.getMean() - gyr_mag_8000.descriptiveStats.getMean();
        double duration = gyr_mag_8000.data.get(gyr_mag_8000.data.size() - 1).timestamp - gyr_mag_8000.data.get(0).timestamp;
        boolean isValidRollPitch = checkValidRollPitch(rolls, pitchs);


            /*
                WRIST - GYRO MAGNITUDE - mean
                WRIST - GYRO MAGNITUDE - median
                WRIST - GYRO MAGNITUDE - std deviation
                WRIST - GYRO MAGNITUDE - quartile deviation
             */
        double GYRO_Magnitude_Mean = gyr_mag.descriptiveStats.getMean();
        double GYRO_Magnitude_Median = gyr_mag.descriptiveStats.getPercentile(50);
        double GYRO_Magnitude_SD = gyr_mag.descriptiveStats.getStandardDeviation();
        double GYRO_Magnitude_Quartile_Deviation = gyr_mag.descriptiveStats.getPercentile(75) - gyr_mag.descriptiveStats.getPercentile(25);

             /*
                WRIST - PITCH - mean
                WRIST - PITCH - median
                WRIST - PITCH - std deviation
                WRIST - PITCH - quartile deviation
             */
        double Pitch_Mean = pitchs.descriptiveStats.getMean();
        double Pitch_Median = pitchs.descriptiveStats.getPercentile(50);
        double Pitch_SD = pitchs.descriptiveStats.getStandardDeviation();
        double Pitch_Quartile_Deviation = pitchs.descriptiveStats.getPercentile(75) - gyr_mag.descriptiveStats.getPercentile(25);

             /*
                WRIST - ROLL - mean
                WRIST - ROLL - median
                WRIST - ROLL - std deviation
                WRIST - ROLL - quartile deviation
             */
        double Roll_Mean = rolls.descriptiveStats.getMean();
        double Roll_Median = rolls.descriptiveStats.getPercentile(50);
        double Roll_SD = rolls.descriptiveStats.getStandardDeviation();
        double Roll_Quartile_Deviation = rolls.descriptiveStats.getPercentile(75) - gyr_mag.descriptiveStats.getPercentile(25);
    }

    /**
     * Add new DataPoint to the buffers
     *
     * @param channel Identifies which sensor stream the DataPoint is associated with
     * @param dp      DataPoint containing a timestamp and value
     * @return Stress probability if it is computed, otherwise Null
     */
    public void add(int channel, DataPoint dp) {

        if (this.windowStartTime < 0)
            this.windowStartTime = Time.nextEpochTimestamp(dp.timestamp, this.windowSize);

        if ((dp.timestamp - windowStartTime) >= this.windowSize) { //Process the buffer every windowSize milliseconds
            process();
            generateResults();
            resetDataStreams();
            this.windowStartTime += AUTOSENSE.SAMPLE_LENGTH_SECS * 1000; //Add 60 seconds to the timestamp
        }

        if (dp.timestamp >= this.windowStartTime) {
            switch (channel) {
                case AUTOSENSE.CHEST_ECG:
                    datastreams.getDataPointStream("org.md2k.cstress.data.ecg").add(dp);
                    break;

                case AUTOSENSE.CHEST_RIP:
                    datastreams.getDataPointStream("org.md2k.cstress.data.rip").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_X:
                    datastreams.getDataPointStream("org.md2k.cstress.data.accelx").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_Y:
                    datastreams.getDataPointStream("org.md2k.cstress.data.accely").add(dp);
                    break;

                case AUTOSENSE.CHEST_ACCEL_Z:
                    datastreams.getDataPointStream("org.md2k.cstress.data.accelz").add(dp);
                    break;


                case PUFFMARKER.LEFTWRIST_ACCEL_X:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_X)).add(dp);
                    break;

                case PUFFMARKER.LEFTWRIST_ACCEL_Y:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_Y)).add(dp);
                    break;

                case PUFFMARKER.LEFTWRIST_ACCEL_Z:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_Z)).add(dp);
                    break;

                case PUFFMARKER.LEFTWRIST_GYRO_X:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_X)).add(dp);
                    break;

                case PUFFMARKER.LEFTWRIST_GYRO_Y:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_Y)).add(dp);
                    break;

                case PUFFMARKER.LEFTWRIST_GYRO_Z:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_Z)).add(dp);
                    break;


                case PUFFMARKER.RIGHTWRIST_ACCEL_X:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_X)).add(dp);
                    break;

                case PUFFMARKER.RIGHTWRIST_ACCEL_Y:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_Y)).add(dp);
                    break;

                case PUFFMARKER.RIGHTWRIST_ACCEL_Z:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_Z)).add(dp);
                    break;

                case PUFFMARKER.RIGHTWRIST_GYRO_X:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_X)).add(dp);
                    break;

                case PUFFMARKER.RIGHTWRIST_GYRO_Y:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_Y)).add(dp);
                    break;

                case PUFFMARKER.RIGHTWRIST_GYRO_Z:
                    (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_Z)).add(dp);
                    break;


                default:
                    System.out.println("NOT INTERESTED: " + dp);
                    break;
            }
        }

    }

    /**
     * Persist and reset all datastreams
     */
    private void resetDataStreams() {
        datastreams.persist(path + participant + "/");
        datastreams.reset();
    }


}
