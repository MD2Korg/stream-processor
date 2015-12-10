package md2k.mCerebrum.cStress.features;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleneen <nsleheen@memphis.edu>
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

import md2k.mCerebrum.cStress.autosense.PUFFMARKER;
import md2k.mCerebrum.cStress.library.DataArrayStream;
import md2k.mCerebrum.cStress.library.DataPointStream;
import md2k.mCerebrum.cStress.library.DataStreams;
import md2k.mCerebrum.cStress.library.structs.DataPointArray;
import org.apache.commons.math3.exception.NotANumberException;

import java.util.ArrayList;
import java.util.List;

public class PuffMarker {


    public PuffMarker(DataStreams datastreams) {
        try {

            String[] wristList = new String[]{PUFFMARKER.LEFT_WRIST, PUFFMARKER.RIGHT_WRIST};
            for (String wrist : wristList) {
                // AccelGyroFeatures agf = new AccelGyroFeatures(datastreams, wrist); //TODO: Is this needed?

                DataPointStream gyr_intersections = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.intersections" + wrist);
                for (int i = 0; i < gyr_intersections.data.size(); i++) {
                    int startIndex = (int) gyr_intersections.data.get(i).timestamp;
                    int endIndex = (int) gyr_intersections.data.get(i).value;

                    DataPointArray fv = computePuffMarkerFeatures(datastreams, wrist, startIndex, endIndex);
                    DataArrayStream fvStream = datastreams.getDataArrayStream("org.md2k.puffMarkers.fv");
                    fvStream.add(fv);
                }
            }

        } catch (IndexOutOfBoundsException e) {
            //Ignore this error
        }
    }


    public boolean checkValidRollPitch(DataPointStream rolls, DataPointStream pitchs) {
        double x = (pitchs.descriptiveStats.getPercentile(50) - PUFFMARKER.PUFFMARKER_PITCH_MEAN) / PUFFMARKER.PUFFMARKER_PITCH_STD;
        double y = (rolls.descriptiveStats.getPercentile(50) - PUFFMARKER.PUFFMARKER_ROLL_MEAN) / PUFFMARKER.PUFFMARKER_ROLL_STD; //TODO: Is this needed?
        double error = x * x;
        return (error < PUFFMARKER.PUFFMARKER_TH[0]);
    }


    private DataPointArray computePuffMarkerFeatures(DataStreams datastreams, String wrist, int startIndex, int endIndex) {

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


        List<Double> featureVector = new ArrayList<Double>();

        featureVector.add(GYRO_Magnitude_Mean);
        featureVector.add(GYRO_Magnitude_Median);
        featureVector.add(GYRO_Magnitude_SD);
        featureVector.add(GYRO_Magnitude_Quartile_Deviation);

        featureVector.add(Pitch_Mean);
        featureVector.add(Pitch_Median);
        featureVector.add(Pitch_SD);
        featureVector.add(Pitch_Quartile_Deviation);

        featureVector.add(Roll_Mean);
        featureVector.add(Roll_Median);
        featureVector.add(Roll_SD);
        featureVector.add(Roll_Quartile_Deviation);

        for (Double aFeatureVector : featureVector) {
            if (Double.isNaN(aFeatureVector)) {
                throw new NotANumberException();
            }
        }
        return new DataPointArray(gyr_mag.data.get(0).timestamp, featureVector);
    }
}
