package md2k.mcerebrum.cstress.features;

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

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.PUFFMARKER;
import md2k.mcerebrum.cstress.library.datastream.DataArrayStream;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.DataPointArray;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * PuffMarker computation class
 */
public class PuffMarker {


    /**
     * Constructor for PuffMarker features
     *
     * @param datastreams Global datastream object
     */
    public PuffMarker(DataStreams datastreams) {
        try {

            String[] wristList = new String[]{PUFFMARKER.LEFT_WRIST, PUFFMARKER.RIGHT_WRIST};
            for (String wrist : wristList) {

                DataPointStream gyr_intersections = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.intersections" + wrist);

                for (int i = 0; i < gyr_intersections.data.size(); i++) {
                    int startIndex = (int) gyr_intersections.data.get(i).timestamp;
                    int endIndex = (int) gyr_intersections.data.get(i).value;

                    DataPointArray fv = computePuffMarkerFeatures(datastreams, wrist, startIndex, endIndex);
                    if (fv != null) {

                        DataArrayStream fvStream = datastreams.getDataArrayStream(StreamConstants.ORG_MD2K_PUFFMARKER_FV);
                        fvStream.add(fv);
                    }
                }
            }

        } catch (IndexOutOfBoundsException e) {
            //Ignore this error
        }
    }


    /**
     * Validate roll and pitch from the sensors based on statically learned values
     *
     * @param roll  Input roll datastream
     * @param pitch Input roll datastream
     * @return True if error is below the threshold, False otherwise
     */
    public boolean checkValidRollPitch(DescriptiveStatistics roll, DescriptiveStatistics pitch) {
        double x = (pitch.getPercentile(50) - PUFFMARKER.PUFF_MARKER_PITCH_MEAN) / PUFFMARKER.PUFF_MARKER_PITCH_STD;
        double y = (roll.getPercentile(50) - PUFFMARKER.PUFF_MARKER_ROLL_MEAN) / PUFFMARKER.PUFF_MARKER_ROLL_STD; //TODO: Is this needed?
        double error = x * x;
        return (error < PUFFMARKER.PUFF_MARKER_TH[0]);
    }


    /**
     * Main computation routine for PuffMarker features
     *
     * @param datastreams Global datastream object
     * @param wrist       Which wrist to operate on
     * @param startIndex  Starting index of a window
     * @param endIndex    Ending index of a window
     * @return
     */
    private DataPointArray computePuffMarkerFeatures(DataStreams datastreams, String wrist, int startIndex, int endIndex) {

        /////////////// RESPIRATION FEATURES ////////////////////////
        double insp = 0;        //1
        double expr = 0;        //2
        double resp = 0;        //3
        double ieRatio = 0;     //4
        double stretch = 0;     //5
        double u_stretch = 0;      //  6. U_Stretch = max(sample[j])
        double l_stretch = 0;      //  7. L_Stretch = min(sample[j])
        double bd_insp = 0;        //10. BD_INSP = INSP(i)-INSP(i-1)
        double bd_expr = 0;        //11. BD_EXPR = EXPR(i)-EXPR(i-1)
        double bd_resp = 0;        //12. BD_RESP = RESP(i)-RESP(i-1)
        double bd_stretch = 0;     //14. BD_Stretch= Stretch(i)-Stretch(i-1)
        double fd_insp = 0;        //  19. FD_INSP = INSP(i)-INSP(i+1)
        double fd_expr = 0;        //  20. FD_EXPR = EXPR(i)-EXPR(i+1)
        double fd_resp = 0;        //  21. FD_RESP = RESP(i)-RESP(i+1)
        double fd_stretch = 0;     //  23. FD_Stretch= Stretch(i)-Stretch(i+1)
        double d5_expr = 0;         //  29. D5_EXPR(i) = EXPR(i) / avg(EXPR(i-2)...EXPR(i+2))
        double d5_stretch = 0;      //  32. D5_Stretch = Stretch(i) / avg(Stretch(i-2)...Stretch(i+2))
        double roc_max = 0;      //  8. ROC_MAX = max(sample[j]-sample[j-1])
        double roc_min = 0;      //  9. ROC_MIN = min(sample[j]-sample[j-1])


        DataPointStream valleys = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_VALLEYS);
        DataPointStream gyr_mag_stream = datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag" + wrist);

        boolean isRIPPresent = false;
        int candidateRespirationValley = 0;
        for (int i = 0; i < valleys.data.size() - 2; i++) {
            double respCycleMaxAmplitude = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MAX_AMPLITUDE).data.get(i).value;
            if (valleys.data.get(i).timestamp > gyr_mag_stream.data.get(startIndex).timestamp && valleys.data.get(i).timestamp < gyr_mag_stream.data.get(endIndex).timestamp
                    && respCycleMaxAmplitude > u_stretch
                    ) {
                isRIPPresent = true;
                candidateRespirationValley = i;
                insp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_INSPDURATION).data.get(i).value;
                resp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_RESPDURATION).data.get(i).value;

                ieRatio = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_IERATIO).data.get(i).value;
                expr = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_EXPRDURATION).data.get(i).value;
                stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP_STRETCH).data.get(i).value;

                bd_insp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_INSPDURATION_BACK_DIFFERENCE).data.get(i).value;
                fd_insp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_INSPDURATION_FORWARD_DIFFERENCE).data.get(i).value;

                bd_expr = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_EXPRDURATION_BACK_DIFFERENCE).data.get(i).value;
                fd_expr = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_EXPRDURATION_FORWARD_DIFFERENCE).data.get(i).value;

                bd_resp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_RESPDURATION_BACK_DIFFERENCE).data.get(i).value;
                fd_resp = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_RESPDURATION_FORWARD_DIFFERENCE).data.get(i).value;

                bd_stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_STRETCH_BACK_DIFFERENCE).data.get(i).value;
                fd_stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_STRETCH_FORWARD_DIFFERENCE).data.get(i).value;

                d5_expr = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_D5_STRETCH).data.get(i).value;
                d5_stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_D5_EXPRDURATION).data.get(i).value;

                u_stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MAX_AMPLITUDE).data.get(i).value;
                l_stretch = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MIN_AMPLITUDE).data.get(i).value;
                roc_max = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MAX_RATE_OF_CHANGE).data.get(i).value;
                roc_min = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_DATA_RIP_MIN_RATE_OF_CHANGE).data.get(i).value;

            }

        }

        if (!isRIPPresent) return null;

        /////////////// WRIST FEATURES ////////////////////////

        List<DataPoint> gyr_mag_800 = (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag_800" + wrist)).data.subList(startIndex, endIndex);
        DescriptiveStatistics mag800stats = new DescriptiveStatistics();
        for (DataPoint dp : gyr_mag_800) {
            mag800stats.addValue(dp.value);
        }

        List<DataPoint> gyr_mag_8000 = (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag_8000" + wrist)).data.subList(startIndex, endIndex);
        DescriptiveStatistics mag8000stats = new DescriptiveStatistics();
        for (DataPoint dp : gyr_mag_8000) {
            mag8000stats.addValue(dp.value);
        }
        List<DataPoint> rolls = (datastreams.getDataPointStream("org.md2k.cstress.data.roll" + wrist)).data.subList(startIndex, endIndex);
        DescriptiveStatistics rollstats = new DescriptiveStatistics();
        for (DataPoint dp : rolls) {
            rollstats.addValue(dp.value);
        }

        List<DataPoint> pitches = (datastreams.getDataPointStream("org.md2k.cstress.data.pitch" + wrist)).data.subList(startIndex, endIndex);
        DescriptiveStatistics pitchstats = new DescriptiveStatistics();
        for (DataPoint dp : pitches) {
            pitchstats.addValue(dp.value);
        }

        /*
        Three filtering criteria
         */
        if (gyr_mag_8000.size() == 0) return null;
        double meanHeight = mag800stats.getMean() - mag8000stats.getMean();
        double duration = gyr_mag_8000.get(gyr_mag_8000.size() - 1).timestamp - gyr_mag_8000.get(0).timestamp;
        boolean isValidRollPitch = checkValidRollPitchSimple(rollstats, pitchstats);

        if (!(duration >= PUFFMARKER.MINIMUM_CANDIDATE_WINDOW_DURATION_ && duration <= PUFFMARKER.MAXIMUM_CANDIDATE_WINDOW_DURATION_) || !isValidRollPitch)
            return null;

        /*
            WRIST - GYRO MAGNITUDE - mean
            WRIST - GYRO MAGNITUDE - median
            WRIST - GYRO MAGNITUDE - std deviation
            WRIST - GYRO MAGNITUDE - quartile deviation
         */
        List<DataPoint> gyr_mag = (datastreams.getDataPointStream("org.md2k.cstress.data.gyr.mag" + wrist)).data.subList(startIndex, endIndex);
        DescriptiveStatistics magstats = new DescriptiveStatistics();
        for (DataPoint dp : gyr_mag) {
            magstats.addValue(dp.value);
        }
        double GYRO_Magnitude_Mean = magstats.getMean();
        double GYRO_Magnitude_Median = magstats.getPercentile(50);
        double GYRO_Magnitude_SD = magstats.getStandardDeviation();
        double GYRO_Magnitude_Quartile_Deviation = magstats.getPercentile(75) - magstats.getPercentile(25);

         /*
            WRIST - PITCH - mean
            WRIST - PITCH - median
            WRIST - PITCH - std deviation
            WRIST - PITCH - quartile deviation
         */

        double Pitch_Mean = pitchstats.getMean();
        double Pitch_Median = pitchstats.getPercentile(50);
        double Pitch_SD = pitchstats.getStandardDeviation();
        double Pitch_Quartile_Deviation = pitchstats.getPercentile(75) - pitchstats.getPercentile(25);

         /*
            WRIST - ROLL - mean
            WRIST - ROLL - median
            WRIST - ROLL - std deviation
            WRIST - ROLL - quartile deviation
         */
        double Roll_Mean = rollstats.getMean();
        double Roll_Median = rollstats.getPercentile(50);
        double Roll_SD = rollstats.getStandardDeviation();
        double Roll_Quartile_Deviation = rollstats.getPercentile(75) - rollstats.getPercentile(25);

        /*
            Respiration - Start time
            Respiration - End time
            Wrist - Start time
            Wrist - End time
            features : 'RStime-WStime','REtime-WStime','RStime-WEtime','REtime-WEtime','RPStime-WEtime'
         */
        long rStime = valleys.data.get(candidateRespirationValley).timestamp;
        long rEtime = valleys.data.get(candidateRespirationValley+1).timestamp;
        long wStime = gyr_mag_stream.data.get(startIndex).timestamp;
        long wEtime = gyr_mag_stream.data.get(endIndex).timestamp;

        List<Double> featureVector = new ArrayList<Double>();

        featureVector.add(insp);
        featureVector.add(expr);
        featureVector.add(resp);
        featureVector.add(ieRatio);
        featureVector.add(stretch);//5
        featureVector.add(u_stretch);
        featureVector.add(l_stretch);
        featureVector.add(bd_insp);
        featureVector.add(bd_expr);
        featureVector.add(bd_resp);//10
        featureVector.add(bd_stretch);
        featureVector.add(fd_insp);
        featureVector.add(fd_expr);
        featureVector.add(fd_resp);
        featureVector.add(fd_stretch);//15
        featureVector.add(d5_expr);
        featureVector.add(d5_stretch);
        featureVector.add(roc_max);
        featureVector.add(roc_min); //19

        featureVector.add(GYRO_Magnitude_Mean);
        featureVector.add(GYRO_Magnitude_Median);
        featureVector.add(GYRO_Magnitude_SD);
        featureVector.add(GYRO_Magnitude_Quartile_Deviation);

        featureVector.add((double) (wEtime - wStime)); //24

        featureVector.add(Pitch_Mean);
        featureVector.add(Pitch_Median);
        featureVector.add(Pitch_SD);
        featureVector.add(Pitch_Quartile_Deviation);

        featureVector.add(Roll_Mean);
        featureVector.add(Roll_Median);
        featureVector.add(Roll_SD);
        featureVector.add(Roll_Quartile_Deviation);

        featureVector.add((double) rStime - wStime);
        featureVector.add((double) rEtime - wStime);
        featureVector.add((double) rStime - wEtime);
        featureVector.add((double) rEtime - wEtime);

        return new DataPointArray(gyr_mag.get(0).timestamp, featureVector);
    }

    private boolean checkValidRollPitchSimple(DescriptiveStatistics roll, DescriptiveStatistics pitch) {
        double r = roll.getMean();
        double p = pitch.getMean();
        return r > -20 && r <= 60 && p >= -130 && p <= -50;

    }
}
