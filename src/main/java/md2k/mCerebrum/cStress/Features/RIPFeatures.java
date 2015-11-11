package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Statistics.BinnedStatistics;
import md2k.mCerebrum.cStress.Statistics.RunningStatistics;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.PeakValley;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;

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
     * @param rip
     * @param ecg
     * @param sc
     */
    public RIPFeatures(HashMap<String, DataStream> datastreams) {

        String pv = Core.peakvalley_v2(datastreams); //There is no trailing valley in this output.

        DataStream valleys = datastreams.get("org.md2k.cstress.data.rip.valleys.filtered");
        DataStream peaks = datastreams.get("org.md2k.cstress.data.rip.peaks.filtered");

        double activity = datastreams.get("org.md2k.cstress.data.accel.activity").data.get(0).value;


        if (!datastreams.containsKey("org.md2k.cstress.data.rip.inspduration")) {
            datastreams.put("org.md2k.cstress.data.rip.inspduration", new DataStream("RIP-InspDuration"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.exprduration")) {
            datastreams.put("org.md2k.cstress.data.rip.exprduration", new DataStream("RIP-exprDuration"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.respduration")) {
            datastreams.put("org.md2k.cstress.data.rip.respduration", new DataStream("RIP-respDuration"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.stretch")) {
            datastreams.put("org.md2k.cstress.data.rip.stretch", new DataStream("RIP-stretch"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.IERatio")) {
            datastreams.put("org.md2k.cstress.data.rip.IERatio", new DataStream("RIP-IERatio"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.RSA")) {
            datastreams.put("org.md2k.cstress.data.rip.RSA", new DataStream("RIP-RSA"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.BreathRate")) {
            datastreams.put("org.md2k.cstress.data.rip.BreathRate", new DataStream("RIP-BreathRate"));
        }
        if (!datastreams.containsKey("org.md2k.cstress.data.rip.MinuteVolume")) {
            datastreams.put("org.md2k.cstress.data.rip.MinuteVolume", new DataStream("RIP-MinuteVolume"));
        }
        
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


}
