package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Library.SignalProcessing.Smoothing;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Vector;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;

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
 * Respiration feature computation class
 */
public class RIPFeatures {
    /**
     * Core Respiration Features
     * Reference: ripFeature_Extraction.m
     *
     * @param datastreams Global data stream object
     */
    public RIPFeatures(DataStreams datastreams) {


        DataStream rip = datastreams.get("org.md2k.cstress.data.rip");
        DataStream rip_smooth = datastreams.get("org.md2k.cstress.data.rip.smooth");
        Smoothing.smooth(rip_smooth, rip, AUTOSENSE.PEAK_VALLEY_SMOOTHING_SIZE);

        int windowLength = (int) Math.round(AUTOSENSE.WINDOW_LENGTH_SECS * (Double) datastreams.get("org.md2k.cstress.data.rip").metadata.get("frequency"));
        DataStream rip_mac = datastreams.get("org.md2k.cstress.data.rip.mac");
        Smoothing.smooth(rip_mac, rip_smooth, windowLength); //TWH: Replaced MAC with Smooth after discussion on 11/9/2015

        DataStream upIntercepts = datastreams.get("org.md2k.cstress.data.rip.upIntercepts");
        DataStream downIntercepts = datastreams.get("org.md2k.cstress.data.rip.downIntercepts");
        generateIntercepts(upIntercepts, downIntercepts, rip_smooth, rip_mac);

        DataStream upInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered");
        DataStream downInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered");
        filterIntercepts(upInterceptsFiltered, downInterceptsFiltered, upIntercepts, downIntercepts);

        DataStream upInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec");
        DataStream downInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec");
        filter1Second(upInterceptsFiltered1sec, downInterceptsFiltered1sec, upInterceptsFiltered, downInterceptsFiltered);

        DataStream upInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec.t20");
        DataStream downInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec.t20");
        filtert20second(upInterceptsFiltered1sect20, downInterceptsFiltered1sect20, upInterceptsFiltered1sec, downInterceptsFiltered1sec);

        DataStream peaks = datastreams.get("org.md2k.cstress.data.rip.peaks");
        generatePeaks(peaks, upInterceptsFiltered1sect20, downInterceptsFiltered1sect20, rip_smooth);

        DataStream valleys = datastreams.get("org.md2k.cstress.data.rip.valleys");
        generateValleys(valleys, upInterceptsFiltered1sect20, downInterceptsFiltered1sect20, rip_smooth);

        DataStream inspirationAmplitude = datastreams.get("org.md2k.cstress.data.rip.inspirationAmplitude");
        double meanInspirationAmplitude = generateInspirationAmplitude(inspirationAmplitude, peaks, valleys);

        DataStream respirationDuration = datastreams.get("org.md2k.cstress.data.rip.respirationDuration");
        generateRespirationDuration(respirationDuration, valleys);

        DataStream valleysFiltered = datastreams.get("org.md2k.cstress.data.rip.valleys.filtered");
        DataStream peaksFiltered = datastreams.get("org.md2k.cstress.data.rip.peaks.filtered");
        filterPeaksAndValleys(peaksFiltered, valleysFiltered, respirationDuration, inspirationAmplitude, peaks, valleys, meanInspirationAmplitude);


        //Key features
        try {
            double activity = datastreams.get("org.md2k.cstress.data.accel.activity").data.get(0).value;
            if (activity == 0.0) {
                for (int i = 0; i < valleys.data.size() - 1; i++) {

                    datastreams.get("org.md2k.cstress.data.rip.inspduration").add(new DataPoint(valleys.data.get(i).timestamp, peaks.data.get(i).timestamp - valleys.data.get(i).timestamp));
                    datastreams.get("org.md2k.cstress.data.rip.exprduration").add(new DataPoint(peaks.data.get(i).timestamp, valleys.data.get(i + 1).timestamp - peaks.data.get(i).timestamp));
                    datastreams.get("org.md2k.cstress.data.rip.respduration").add(new DataPoint(valleys.data.get(i).timestamp, valleys.data.get(i + 1).timestamp - valleys.data.get(i).timestamp));

                    datastreams.get("org.md2k.cstress.data.rip.stretch").add(new DataPoint(valleys.data.get(i).timestamp, peaks.data.get(i).value - valleys.data.get(i).value));

                    DataPoint inratio = datastreams.get("org.md2k.cstress.data.rip.inspduration").data.get(datastreams.get("org.md2k.cstress.data.rip.inspduration").data.size() - 1);
                    DataPoint exratio = datastreams.get("org.md2k.cstress.data.rip.exprduration").data.get(datastreams.get("org.md2k.cstress.data.rip.exprduration").data.size() - 1);

                    datastreams.get("org.md2k.cstress.data.rip.IERatio").add(new DataPoint(valleys.data.get(i).timestamp, inratio.value / exratio.value));

                    DataPoint rsa = rsaCalculateCycle(valleys.data.get(i).timestamp, valleys.data.get(i + 1).timestamp, datastreams.get("org.md2k.cstress.data.ecg.rr"));
                    if (rsa.value != -1.0) { //Only add if a valid value
                        datastreams.get("org.md2k.cstress.data.rip.RSA").add(rsa);
                    }

                }


                datastreams.get("org.md2k.cstress.data.rip.BreathRate").add(new DataPoint(datastreams.get("org.md2k.cstress.data.rip").data.get(datastreams.get("org.md2k.cstress.data.rip").data.size() - 1).timestamp, valleys.data.size() - 1));

                double minuteVentilation = 0.0;
                for (int i = 0; i < valleys.data.size() - 1; i++) {
                    minuteVentilation += (peaks.data.get(i).timestamp - valleys.data.get(i).timestamp) / 1000.0 * (peaks.data.get(i).value - valleys.data.get(i).value) / 2.0;
                }
                //minuteVentilation *= (valleys.data.size()-1); //TODO: Check with experts that this should not be there

                datastreams.get("org.md2k.cstress.data.rip.MinuteVentilation").add(new DataPoint(datastreams.get("org.md2k.cstress.data.rip").data.get(datastreams.get("org.md2k.cstress.data.rip").data.size() - 1).timestamp, minuteVentilation));
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter peaks and valleys from data streams
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param peaksFiltered Filtered peaks output
     * @param valleysFiltered Filtered valleys output
     * @param respirationDuration Input respiration duration
     * @param inspirationAmplitude Input inspiration amplitude
     * @param peaks Peak datastream
     * @param valleys Valley datastream
     * @param meanInspirationAmplitude Average inspiration amplitude
     */
    private void filterPeaksAndValleys(DataStream peaksFiltered, DataStream valleysFiltered, DataStream respirationDuration, DataStream inspirationAmplitude, DataStream peaks, DataStream valleys, double meanInspirationAmplitude) {
        try {
            for (int i1 = 0; i1 < respirationDuration.data.size(); i1++) {
                double duration = respirationDuration.data.get(i1).value / 1000.0;
                if (duration > 1.0 && duration < 12.0) { //Passes length test
                    if (inspirationAmplitude.data.get(i1).value > (AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanInspirationAmplitude)) { //Passes amplitude test
                        valleysFiltered.add(valleys.data.get(i1));
                        peaksFiltered.add(peaks.data.get(i1));
                    }
                }
            }
            valleysFiltered.add(valleys.data.get(valleys.data.size() - 1)); //Add last valley that was skipped by loop
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute respiration duration from valley data stream
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param respirationDuration Output respiration durations
     * @param valleys Input valley datastream
     */
    private void generateRespirationDuration(DataStream respirationDuration, DataStream valleys) {
        try {
            for (int i1 = 0; i1 < valleys.data.size() - 1; i1++) {
                respirationDuration.add(new DataPoint(valleys.data.get(i1).timestamp, valleys.data.get(i1 + 1).timestamp - valleys.data.get(i1).timestamp));
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute inspiration amplitude from peaks and valleys
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param ia Inspiration amplitude datastream output
     * @param peaks Input peak datastream
     * @param valleys Input valleys datastream
     * @return
     */
    private double generateInspirationAmplitude(DataStream ia, DataStream peaks, DataStream valleys) {
        SummaryStatistics inspirationAmplitude = new SummaryStatistics();
        try {
            for (int i1 = 0; i1 < valleys.data.size() - 1; i1++) {
                double inspAmp = (peaks.data.get(i1).value - valleys.data.get(i1).value);
                ia.add(new DataPoint(valleys.data.get(i1).timestamp, inspAmp));
                inspirationAmplitude.addValue(inspAmp);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return inspirationAmplitude.getMean();
    }


    /**
     * Compute valleys in a respiration datastream
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param valleys Output valley datastream
     * @param upInterceptsFiltered1sect20 Up intercept datastream
     * @param downInterceptsFiltered1sect20 Down insercept datastream
     * @param rip_smooth Smoothed RIP datastream
     */
    private void generateValleys(DataStream valleys, DataStream upInterceptsFiltered1sect20, DataStream downInterceptsFiltered1sect20, DataStream rip_smooth) {
        try {
            for (int i1 = 0; i1 < upInterceptsFiltered1sect20.data.size() - 1; i1++) {
                DataPoint valley = findValley(downInterceptsFiltered1sect20.data.get(i1), upInterceptsFiltered1sect20.data.get(i1), rip_smooth);
                if (valley.timestamp != 0) {
                    valleys.add(valley);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute peaks in a respiration datastream
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param peaks Output valley datastream
     * @param upInterceptsFiltered1sect20 Up intercept datastream
     * @param downInterceptsFiltered1sect20 Down insercept datastream
     * @param rip_smooth Smoothed RIP datastream
     */
    private void generatePeaks(DataStream peaks, DataStream upInterceptsFiltered1sect20, DataStream downInterceptsFiltered1sect20, DataStream rip_smooth) {
        try {
            for (int i1 = 0; i1 < upInterceptsFiltered1sect20.data.size() - 1; i1++) {
                DataPoint peak = findPeak(upInterceptsFiltered1sect20.data.get(i1), downInterceptsFiltered1sect20.data.get(i1 + 1), rip_smooth);
                if (peak.timestamp != 0) {
                    peaks.add(peak);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter up and down intercepts based on 1/20 of a second
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param upInterceptsFiltered1sect20 Output filtered up intercepts
     * @param downInterceptsFiltered1sect20 Output filtered down intercepts
     * @param upInterceptsFiltered1sec Input up intercepts
     * @param downInterceptsFiltered1sec Input down intercepts
     */
    private void filtert20second(DataStream upInterceptsFiltered1sect20, DataStream downInterceptsFiltered1sect20, DataStream upInterceptsFiltered1sec, DataStream downInterceptsFiltered1sec) {
        try {
            if (downInterceptsFiltered1sec.data.size() > 0) {
                downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(0));
                for (int i1 = 0; i1 < upInterceptsFiltered1sec.data.size(); i1++) {
                    if ((downInterceptsFiltered1sec.data.get(i1 + 1).timestamp - upInterceptsFiltered1sec.data.get(i1).timestamp) > (2.0 / 20.0)) {
                        downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(i1 + 1));
                        upInterceptsFiltered1sect20.add(upInterceptsFiltered1sec.data.get(i1));
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter up and down intercepts based on 1 second window
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param upInterceptsFiltered1sec Output filtered up intercepts
     * @param downInterceptsFiltered1sec Output filtered down intercepts
     * @param upInterceptsFiltered Input up intercepts
     * @param downInterceptsFiltered Input down intercepts
     */
    private void filter1Second(DataStream upInterceptsFiltered1sec, DataStream downInterceptsFiltered1sec, DataStream upInterceptsFiltered, DataStream downInterceptsFiltered) {
        try {
            for (int i1 = 1; i1 < downInterceptsFiltered.data.size(); i1++) {
                if ((downInterceptsFiltered.data.get(i1).timestamp - downInterceptsFiltered.data.get(i1 - 1).timestamp) > 1000.0) {
                    downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(i1 - 1));
                    upInterceptsFiltered1sec.add(upInterceptsFiltered.data.get(i1 - 1));
                }
            }
            downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter up and down intercepts
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param upInterceptsFiltered Output filtered up intercepts
     * @param downInterceptsFiltered Output filtered down intercepts
     * @param upIntercepts Input up intercepts
     * @param downIntercepts Input down intercepts
     */
    private void filterIntercepts(DataStream upInterceptsFiltered, DataStream downInterceptsFiltered, DataStream upIntercepts, DataStream downIntercepts) {
        int upPointer = 0;
        int downPointer = 0;
        boolean updownstate = true; //True check for up intercept

        try {
            downInterceptsFiltered.add(downIntercepts.data.get(downPointer)); //Initialize with starting point

            while (downPointer != downIntercepts.data.size() && upPointer != upIntercepts.data.size()) {
                if (updownstate) { //Check for up intercept
                    if (downIntercepts.data.get(downPointer).timestamp < upIntercepts.data.get(upPointer).timestamp) {
                        //Replace down intercept
                        downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1).timestamp = downIntercepts.data.get(downPointer).timestamp;
                        downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1).value = downIntercepts.data.get(downPointer).value;
                        downPointer++;
                    } else {
                        //Found up intercept
                        upInterceptsFiltered.add(upIntercepts.data.get(upPointer));
                        upPointer++;
                        updownstate = false;
                    }
                } else { //Check for down intercept
                    if (downIntercepts.data.get(downPointer).timestamp > upIntercepts.data.get(upPointer).timestamp) {
                        //Replace up intercept
                        upInterceptsFiltered.data.get(upInterceptsFiltered.data.size() - 1).timestamp = upIntercepts.data.get(upPointer).timestamp;
                        upInterceptsFiltered.data.get(upInterceptsFiltered.data.size() - 1).value = upIntercepts.data.get(upPointer).value;
                        upPointer++;
                    } else {
                        //Found down intercept
                        downInterceptsFiltered.add(downIntercepts.data.get(downPointer));
                        downPointer++;
                        updownstate = true;
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute up and down intercepts from RIP signal
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param upIntercepts Output up intercept datastream
     * @param downIntercepts Output down intercept datastream
     * @param rip_smooth Smoothed RIP datastream
     * @param rip_mac RIP datastream
     */
    private void generateIntercepts(DataStream upIntercepts, DataStream downIntercepts, DataStream rip_smooth, DataStream rip_mac) {
        for (int i1 = 1; i1 < rip_mac.data.size() - 1; i1++) {
            try {
                if (rip_smooth.data.get(i1 - 1).value < rip_mac.data.get(i1).value && rip_smooth.data.get(i1 + 1).value > rip_mac.data.get(i1).value) {
                    upIntercepts.add(rip_mac.data.get(i1));
                } else if (rip_smooth.data.get(i1 - 1).value > rip_mac.data.get(i1).value && rip_smooth.data.get(i1 + 1).value < rip_mac.data.get(i1).value) {
                    downIntercepts.add(rip_mac.data.get(i1));
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Compute up and down intercepts from RIP signal
     * <p>
     *     Reference: Matlab code \\TODO
     *     <a href="https://en.wikipedia.org/wiki/Vagal_tone#Relation_to_respiratory_sinus_arrhythmia">https://en.wikipedia.org/wiki/Vagal_tone#Relation_to_respiratory_sinus_arrhythmia</a>
     * </p>
     * @param starttime Beginning time of data window
     * @param endtime Ending time of data window
     * @param rrintervals Input rr-interval datastream
     * @return Max - min of the ECG signal within the RSA window
     */
    private DataPoint rsaCalculateCycle(long starttime, long endtime, DataStream rrintervals) {
        DataPoint result = new DataPoint(starttime, -1.0);

        DataPoint max = new DataPoint(0, 0.0);
        DataPoint min = new DataPoint(0, 0.0);
        boolean maxFound = false;
        boolean minFound = false;
        for (DataPoint dp : rrintervals.data) {
            if (dp.timestamp > starttime && dp.timestamp < endtime) {
                if (max.timestamp == 0 && min.timestamp == 0) {
                    max = new DataPoint(dp);
                    min = new DataPoint(dp);
                } else {
                    if (dp.value > max.value) {
                        max = new DataPoint(dp);
                        maxFound = true;
                    }
                    if (dp.value < min.value) {
                        min = new DataPoint(dp);
                        minFound = true;
                    }
                }
            }
        }

        if (maxFound && minFound) {
            result.value = max.value - min.value; //RSA amplitude
        }
        return result;
    }

    /**
     * Identifies valleys in a datastream
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param downIntercept Down intercept DataPoint
     * @param upIntercept Up intercept DataPoint
     * @param data Input datastream
     * @return Valley point from data located between the downIntercept and upIntercept
     */
    private DataPoint findValley(DataPoint downIntercept, DataPoint upIntercept, DataStream data) {
        DataPoint result = new DataPoint(upIntercept);

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (downIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < upIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if (temp.size() > 1) {
            ArrayList<DataPoint> diff = Vector.diff(temp);
            boolean positiveSlope = false;
            if (diff.get(0).value > 0) {
                positiveSlope = true;
            }

            ArrayList<Integer> localMinCandidates = new ArrayList<Integer>();
            for (int i = 1; i < diff.size(); i++) {
                if (positiveSlope) {
                    if (diff.get(i).value < 0) {
                        //Local Max
                        positiveSlope = false;
                    }
                } else {
                    if (diff.get(i).value > 0) {
                        //Local Min
                        localMinCandidates.add(i);
                        positiveSlope = true;
                    }
                }
            }

            int maximumSlopeLength = 0;
            for (Integer i : localMinCandidates) {
                int tempLength = 0;
                for (int j = i; j < diff.size(); j++) {
                    if (diff.get(j).value > 0) {
                        tempLength++;
                    } else {
                        break;
                    }
                }
                if (tempLength > maximumSlopeLength) {
                    maximumSlopeLength = tempLength;
                    result = temp.get(i);
                }
            }
        }
        return result;
    }

    /**
     * Identifies peaks in a datastream
     * <p>
     *     Reference: Matlab code \\TODO
     * </p>
     * @param downIntercept Down intercept DataPoint
     * @param upIntercept Up intercept DataPoint
     * @param data Input datastream
     * @return Peak point from data located between the upIntercept and downIntercept
     */
    public DataPoint findPeak(DataPoint upIntercept, DataPoint downIntercept, DataStream data) {

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (upIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < downIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if (temp.size() > 0) {
            DataPoint max = temp.get(0);
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).value > max.value) {
                    max = temp.get(i);
                }
            }

            return max;
        } else {
            return new DataPoint(0, 0.0);
        }
    }

}
