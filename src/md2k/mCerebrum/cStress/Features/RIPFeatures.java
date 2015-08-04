package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Library;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Intercepts;
import md2k.mCerebrum.cStress.Structs.PeakValley;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;

/**
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
public class RIPFeatures {
    public double MinuteVolume;
    public double BreathRate;

    public DescriptiveStatistics InspDuration;
    public DescriptiveStatistics ExprDuration;
    public DescriptiveStatistics RespDuration;
    public DescriptiveStatistics Stretch;
    public DescriptiveStatistics IERatio;
    public DescriptiveStatistics RSA;

    private SensorConfiguration sensorConfig;

    public RIPFeatures() {

    }

    /**
     * Core Respiration Features
     * Reference: ripFeature_Extraction.m
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

        //TS correction here...
        //Data Quality here...
        //Interpolation...
        //TODO: winsorization using activity input? Insp, Expr, Stretch, MinuteVentilation(Volume)

        PeakValley pvData = Library.peakvalley_v2(rip, sensorConfig); //There is no trailing valley in this output.


        for(int i=0; i<pvData.valleyIndex.size()-1; i++) {

            InspDuration.addValue(rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
            ExprDuration.addValue(rip[pvData.valleyIndex.get(i+1)].timestamp - rip[pvData.peakIndex.get(i)].timestamp);
            RespDuration.addValue(rip[pvData.valleyIndex.get(i+1)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp);
            Stretch.addValue(rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value);
            IERatio.addValue(InspDuration.getElement((int)InspDuration.getN()-1)-ExprDuration.getElement((int)ExprDuration.getN()-1));
            RSA.addValue(rsaCalculateCycle(rip[pvData.valleyIndex.get(i)].timestamp, rip[pvData.valleyIndex.get(i+1)].timestamp, ecg) );
        }

        BreathRate = pvData.valleyIndex.size();

        MinuteVolume = 0.0;
        for(int i=0; i<pvData.valleyIndex.size(); i++) {
            MinuteVolume += (rip[pvData.peakIndex.get(i)].timestamp - rip[pvData.valleyIndex.get(i)].timestamp) / 1000.0 * (rip[pvData.peakIndex.get(i)].value - rip[pvData.valleyIndex.get(i)].value) / 2.0;
        }
        MinuteVolume *= pvData.valleyIndex.size();
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


}
