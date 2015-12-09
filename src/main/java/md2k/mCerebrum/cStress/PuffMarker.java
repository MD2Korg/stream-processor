package md2k.mCerebrum.cStress;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Features.AccelGyroFeatures;
import md2k.mCerebrum.cStress.Library.DataPointStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Structs.DataPointArray;
import md2k.mCerebrum.cStress.Library.Structs.StressProbability;
import md2k.mCerebrum.cStress.util.PuffMarkerUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.Date;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleneen <nsleheen@memphis.edu>
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
public class PuffMarker {

    long windowStartTime = -1;

    long windowSize;
    private String participant;

    private DataStreams datastreams = new DataStreams();


    public PuffMarker(long windowSize, String svmModelFile, String featureVectorParameterFile, String participant) {
        this.windowSize = windowSize;
        this.participant = participant;

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

    public StressProbability process() {

        StressProbability probabilityOfStress = null;

//        RIPFeatures rf = new RIPFeatures(datastreams);

        String[] wristList = new String[]{PuffMarkerUtils.LEFT_WRIST, PuffMarkerUtils.RIGHT_WRIST};
        for (String wrist : wristList) {
            AccelGyroFeatures agf = new AccelGyroFeatures(datastreams, wrist);
            DataPointStream gyr_intersections = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.intersections" + wrist);
            for (int i = 0; i < gyr_intersections.data.size(); i++) {
                int startIndex = (int) gyr_intersections.data.get(i).timestamp;
                int endIndex = (int) gyr_intersections.data.get(i).value;
                DataPointArray fv = computeStressFeatures(datastreams, wrist, startIndex, endIndex);
            }
        }

//                probabilityOfStress = evaluteStressModel(accelFeatures, ecgFeatures, ripFeatures, AUTOSENSE.STRESS_PROBABILTY_THRESHOLD);

        return probabilityOfStress;
    }

    public boolean checkValidRollPitch(DataPointStream rolls, DataPointStream pitchs) {
        double x = (pitchs.descriptiveStats.getPercentile(50) - RP.PITCH_MEAN) / RP.PITCH_STD;
        double y = (rolls.descriptiveStats.getPercentile(50) - RP.ROLL_MEAN) / RP.ROLL_STD;
        double error = x * x; //;
//        double error = Math.sqrt(x * x+y*y); //;
        if (error > RP.TH[0])
            return false;
        return true;
    }

    private DataPointArray computeStressFeatures(DataStreams datastreams, String wrist, int startIndex, int endIndex) {

        try {
        /* List of features for SVM model
        */
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

            ArrayList<Double> featureVector = new ArrayList<>();


//            featureVector.add(ECG_RR_Interval_Variance);// 1
//            featureVector.add(ECG_RR_Interval_Low_High_Frequency_Energy_Ratio);// 2
//            featureVector.add(ECG_RR_Interval_High_Frequency_Energy);// 3
//            featureVector.add(ECG_RR_Interval_Medium_Frequency_Energy);// 4
//            featureVector.add(ECG_RR_Interval_Low_Frequency_Energy);// 5
//            featureVector.add(ECG_RR_Interval_Mean);// 6
//            featureVector.add(ECG_RR_Interval_Median);// 7
//            featureVector.add(ECG_RR_Interval_Quartile_Deviation);// 8
//            featureVector.add(ECG_RR_Interval_80thPercentile);// 9
//            featureVector.add(ECG_RR_Interval_20thPercentile);// 10
//            featureVector.add(ECG_RR_Interval_Heart_Rate);// 11

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
                    break;
                }
            }

            if (valid) {
                DataPointArray fv = new DataPointArray(windowStartTime, featureVector);
                return fv;
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }


        return null;
    }


    public StressProbability add(int channel, DataPoint dp) {
        StressProbability result = null;

        if (this.windowStartTime < 0)
            this.windowStartTime = nextEpochTimestamp(dp.timestamp);

        if ((dp.timestamp - windowStartTime) >= this.windowSize) { //Process the buffer every windowSize milliseconds
//            result = process();
//            resetDataStreams();
            this.windowStartTime += AUTOSENSE.SAMPLE_LENGTH_SECS * 1000; //Add 60 seconds to the timestamp
        }

//        if (dp.timestamp >= this.windowStartTime) {
        switch (channel) {
            case PuffMarkerUtils.RIP:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIP)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_ACCEL_X)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_ACCEL_Y)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_ACCEL_Z)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_GYRO_X)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_GYRO_Y)).add(dp);
                break;

            case PuffMarkerUtils.LEFTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_LEFTWRIST_GYRO_Z)).add(dp);
                break;


            case PuffMarkerUtils.RIGHTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_ACCEL_X)).add(dp);
                break;

            case PuffMarkerUtils.RIGHTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_ACCEL_Y)).add(dp);
                break;

            case PuffMarkerUtils.RIGHTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_ACCEL_Z)).add(dp);
                break;

            case PuffMarkerUtils.RIGHTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_GYRO_X)).add(dp);
                break;

            case PuffMarkerUtils.RIGHTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_GYRO_Y)).add(dp);
                break;

            case PuffMarkerUtils.RIGHTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PuffMarkerUtils.KEY_DATA_RIGHTWRIST_GYRO_Z)).add(dp);
                break;


            default:
                System.out.println("NOT INTERESTED: " + dp);
                break;
        }
//        }

        return result;
    }

    private long nextEpochTimestamp(long timestamp) {
        long previousMinute = timestamp / (60 * 1000);
        Date date = new Date((previousMinute + 1) * (60 * 1000));
        return date.getTime();
    }

    private void resetDataStreams() {
//        datastreams.persist("/Users/hnat/Downloads/processedrawdata/" + participant + "/");
        datastreams.reset();
    }
}


class RP {
    public static final double ROLL_MEAN = 26.7810;
    public static final double PITCH_MEAN = -80.3673;
    public static final double ROLL_STD = 13.9753;
    public static final double PITCH_STD = 13.4698;
    public static final double MEAN = 26.7810;
    public static final double[][] SIGMA = new double[][]{{195.3085, -92.7786}, {-92.7786, 181.4359}};
    public static final double[] TH = new double[]{10.1511, 7.8746, 11.7729, 11.2226};
/*
    public static final double ROLL_MEAN=30.3067;
    public static final double PITCH_MEAN=-79.8684;
    public static final double ROLL_STD=11.6764;
    public static final double PITCH_STD=13.3340;
    public static final double MEAN=13.3340;
    public static final double[][] SIGMA = new double[][]{{136.3388, -51.6343}, {-51.6343, 177.7954}};
    public static final double[] TH = new double[]{13.9489, 8.3384, 17.3435, 14.2775};
*/
}

