package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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
    /**
     * Core Respiration Features
     * Reference: ripFeature_Extraction.m
     *
     */
    public RIPFeatures(DataStreams datastreams) {

        String pv = peakvalley_v2(datastreams); //There is no trailing valley in this output.

        DataStream valleys = datastreams.get("org.md2k.cstress.data.rip.valleys.filtered");
        DataStream peaks = datastreams.get("org.md2k.cstress.data.rip.peaks.filtered");

        double activity = datastreams.get("org.md2k.cstress.data.accel.activity").data.get(0).value;

        
        if (activity == 0.0) {
            for(int i=0; i<valleys.data.size()-1; i++) {

                datastreams.get("org.md2k.cstress.data.rip.inspduration").add(new DataPoint(valleys.data.get(i).timestamp, peaks.data.get(i).timestamp - valleys.data.get(i).timestamp));
                datastreams.get("org.md2k.cstress.data.rip.exprduration").add(new DataPoint(peaks.data.get(i).timestamp, valleys.data.get(i+1).timestamp - peaks.data.get(i).timestamp));
                datastreams.get("org.md2k.cstress.data.rip.respduration").add(new DataPoint(valleys.data.get(i).timestamp, valleys.data.get(i+1).timestamp - valleys.data.get(i).timestamp));

                datastreams.get("org.md2k.cstress.data.rip.stretch").add(new DataPoint(valleys.data.get(i).timestamp, peaks.data.get(i).value - valleys.data.get(i).value));

                DataPoint inratio = datastreams.get("org.md2k.cstress.data.rip.inspduration").data.get(datastreams.get("org.md2k.cstress.data.rip.inspduration").data.size()-1);
                DataPoint exratio = datastreams.get("org.md2k.cstress.data.rip.exprduration").data.get(datastreams.get("org.md2k.cstress.data.rip.exprduration").data.size()-1);

                datastreams.get("org.md2k.cstress.data.rip.IERatio").add(new DataPoint(valleys.data.get(i).timestamp, inratio.value / exratio.value));

                DataPoint rsa = rsaCalculateCycle(valleys.data.get(i).timestamp,valleys.data.get(i+1).timestamp,datastreams.get("org.md2k.cstress.data.ecg.rr"));
                if(rsa.value != -1.0) { //Only add if a valid value
                    datastreams.get("org.md2k.cstress.data.rip.RSA").add(rsa);
                }

            }


            datastreams.get("org.md2k.cstress.data.rip.BreathRate").add(new DataPoint(datastreams.get("org.md2k.cstress.data.rip").data.get(datastreams.get("org.md2k.cstress.data.rip").data.size()-1).timestamp, valleys.data.size()-1));

            double minuteVentalation = 0.0;
            for(int i=0; i < valleys.data.size()-1; i++) {
                minuteVentalation += (peaks.data.get(i).timestamp - valleys.data.get(i).timestamp) / 1000.0 * (peaks.data.get(i).value - valleys.data.get(i).value) / 2.0;
            }
            //minuteVentalation *= (valleys.data.size()-1); //TODO: Check with experts that this should not be there

            datastreams.get("org.md2k.cstress.data.rip.MinuteVolume").add(new DataPoint(datastreams.get("org.md2k.cstress.data.rip").data.get(datastreams.get("org.md2k.cstress.data.rip").data.size()-1).timestamp, minuteVentalation));
        }



    }

    public String peakvalley_v2(DataStreams datastreams) {

        DataStream rip_smooth = datastreams.get("org.md2k.cstress.data.rip.smooth");

        ArrayList<DataPoint> sample = Core.smooth(datastreams.get("org.md2k.cstress.data.rip").data, AUTOSENSE.PEAK_VALLEY_SMOOTHING_SIZE);
        for (DataPoint dp : sample) {
            rip_smooth.add(dp);
        }


        int windowLength = (int) Math.round(AUTOSENSE.WINDOW_LENGTH_SECS * (Double) datastreams.get("org.md2k.cstress.data.rip").metadata.get("frequency"));

        DataStream rip_mac = datastreams.get("org.md2k.cstress.data.rip.mac");
        ArrayList<DataPoint> MAC = Core.smooth(rip_smooth.data, windowLength); //TWH: Replaced MAC with Smooth after discussion on 11/9/2015
        for (DataPoint dp : MAC) {
            rip_mac.add(dp);
        }


        for (int i = 1; i < rip_mac.data.size() - 1; i++) {
            if (rip_smooth.data.get(i - 1).value < rip_mac.data.get(i).value && rip_smooth.data.get(i + 1).value > rip_mac.data.get(i).value) {
                datastreams.get("org.md2k.cstress.data.rip.upIntercepts").add(rip_mac.data.get(i));
            } else if (rip_smooth.data.get(i - 1).value > rip_mac.data.get(i).value && rip_smooth.data.get(i + 1).value < rip_mac.data.get(i).value) {
                datastreams.get("org.md2k.cstress.data.rip.downIntercepts").add(rip_mac.data.get(i));
            }
        }


        DataStream upIntercepts = datastreams.get("org.md2k.cstress.data.rip.upIntercepts");
        DataStream downIntercepts = datastreams.get("org.md2k.cstress.data.rip.downIntercepts");

        DataStream upInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered");
        DataStream downInterceptsFiltered = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered");

        int upPointer = 0;
        int downPointer = 0;
        boolean updownstate = true; //True check for up intercept

        if(downIntercepts.data.size() > 0) {
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
        }

        DataStream upInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec");
        DataStream downInterceptsFiltered1sec = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec");


        for (int i = 1; i < downInterceptsFiltered.data.size(); i++) {
            if ((downInterceptsFiltered.data.get(i).timestamp - downInterceptsFiltered.data.get(i - 1).timestamp) > 1000.0) {
                downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(i - 1));
                upInterceptsFiltered1sec.add(upInterceptsFiltered.data.get(i - 1));
            }
        }
        downInterceptsFiltered1sec.add(downInterceptsFiltered.data.get(downInterceptsFiltered.data.size() - 1));

        DataStream upInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.upIntercepts.filtered.1sec.t20");
        DataStream downInterceptsFiltered1sect20 = datastreams.get("org.md2k.cstress.data.rip.downIntercepts.filtered.1sec.t20");


        if (downInterceptsFiltered1sec.data.size() > 0) {
            downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(0));
            for (int i = 0; i < upInterceptsFiltered1sec.data.size(); i++) {
                if ((downInterceptsFiltered1sec.data.get(i + 1).timestamp - upInterceptsFiltered1sec.data.get(i).timestamp) > (2.0 / 20.0)) {
                    downInterceptsFiltered1sect20.add(downInterceptsFiltered1sec.data.get(i + 1));
                    upInterceptsFiltered1sect20.add(upInterceptsFiltered.data.get(i));
                }
            }
        }


        for (int i = 0; i < upInterceptsFiltered1sect20.data.size() - 1; i++) {
            DataPoint peak = findPeak(upInterceptsFiltered1sect20.data.get(i), downInterceptsFiltered1sect20.data.get(i + 1), rip_smooth);
            if (peak.timestamp != 0) {
                datastreams.get("org.md2k.cstress.data.rip.peaks").add(peak);
            }
        }

        for (int i = 0; i < downInterceptsFiltered1sect20.data.size() - 1; i++) { //Don't check the last down intercept
            DataPoint valley = findValley(downInterceptsFiltered1sect20.data.get(i), upInterceptsFiltered1sect20.data.get(i), rip_smooth);
            if (valley.timestamp != 0) {
                datastreams.get("org.md2k.cstress.data.rip.valleys").add(valley);
            }
        }


        SummaryStatistics inspirationAmplitude = new SummaryStatistics();
        SummaryStatistics expirationAmplitude = new SummaryStatistics();

        DataStream valleys = datastreams.get("org.md2k.cstress.data.rip.valleys");
        DataStream peaks = datastreams.get("org.md2k.cstress.data.rip.peaks");

        for (int i = 0; i < Math.min(valleys.data.size() - 1,peaks.data.size()); i++) {
            double inspAmp = (peaks.data.get(i).value - valleys.data.get(i).value);
            datastreams.get("org.md2k.cstress.data.rip.inspirationAmplitude").add(new DataPoint(valleys.data.get(i).timestamp, inspAmp));
            inspirationAmplitude.addValue(inspAmp);
        }
        double meanInspirationAmplitude = inspirationAmplitude.getMean();

        for (int i = 0; i < Math.min(valleys.data.size() - 1,peaks.data.size()); i++) {
            double expAmp = (peaks.data.get(i).value - valleys.data.get(i + 1).value);
            datastreams.get("org.md2k.cstress.data.rip.expirationAmplitude").add(new DataPoint(peaks.data.get(i).timestamp, expAmp));
            expirationAmplitude.addValue(expAmp);
        }
        double meanExpirationAmplitude = expirationAmplitude.getMean();


        for (int i = 0; i < valleys.data.size() - 1; i++) {
            datastreams.get("org.md2k.cstress.data.rip.respirationDuration").add(new DataPoint(valleys.data.get(i).timestamp, valleys.data.get(i + 1).timestamp - valleys.data.get(i).timestamp));
        }

        for (int i = 0; i < Math.min(datastreams.get("org.md2k.cstress.data.rip.respirationDuration").data.size(),datastreams.get("org.md2k.cstress.data.rip.inspirationAmplitude").data.size()); i++) {
            double duration = datastreams.get("org.md2k.cstress.data.rip.respirationDuration").data.get(i).value / 1000.0;
            if (duration > 1.0 && duration < 12.0) { //Passes length test
                if (datastreams.get("org.md2k.cstress.data.rip.inspirationAmplitude").data.get(i).value > (AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanInspirationAmplitude)) { //Passes amplitude test
                    datastreams.get("org.md2k.cstress.data.rip.valleys.filtered").add(datastreams.get("org.md2k.cstress.data.rip.valleys").data.get(i));
                    datastreams.get("org.md2k.cstress.data.rip.peaks.filtered").add(datastreams.get("org.md2k.cstress.data.rip.peaks").data.get(i));
                }
            }
        }
        datastreams.get("org.md2k.cstress.data.rip.valleys.filtered").add(datastreams.get("org.md2k.cstress.data.rip.valleys").data.get(datastreams.get("org.md2k.cstress.data.rip.valleys").data.size() - 1)); //Add last valley that was skipped by loop

        return "org.md2k.success";
    }


    private DataPoint rsaCalculateCycle(long starttime, long endtime, DataStream rrintervals) {
        DataPoint result = new DataPoint(starttime,-1.0);

        DataPoint max = new DataPoint(0,0.0);
        DataPoint min = new DataPoint(0,0.0);
        boolean maxFound = false;
        boolean minFound = false;
        for(DataPoint dp: rrintervals.data) {
            if(dp.timestamp > starttime && dp.timestamp < endtime) {
                if(max.timestamp == 0 && min.timestamp == 0) {
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

        if(maxFound && minFound) {
            result.value = max.value - min.value; //RSA amplitude
        }
        return result;
    }

    private DataPoint findValley(DataPoint downIntercept, DataPoint upIntercept, DataStream data) {
        DataPoint result = new DataPoint(upIntercept);

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (downIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < upIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if (temp.size() > 0) {
            ArrayList<DataPoint> diff = Core.diff(temp);
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

    public DataPoint findPeak(DataPoint upIntercept, DataPoint downIntercept, DataStream data) {

        ArrayList<DataPoint> temp = new ArrayList<DataPoint>();
        for (int i = 0; i < data.data.size(); i++) { //Identify potential data points
            if (upIntercept.timestamp < data.data.get(i).timestamp && data.data.get(i).timestamp < downIntercept.timestamp) {
                temp.add(data.data.get(i));
            }
        }
        if(temp.size() > 0) {
            DataPoint max = temp.get(0);
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).value > max.value) {
                    max = temp.get(i);
                }
            }

            return max;
        } else {
            return new DataPoint(0,0.0);
        }
    }

}
