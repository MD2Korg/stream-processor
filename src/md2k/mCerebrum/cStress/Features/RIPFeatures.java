package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
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
public class RIPFeatures{

    //ripfeature_extraction.m

    public double InspDuration;
    public double ExprDuration;
    public double RespDuration;
    public double Stretch;
    public double StretchUp;
    public double StretchDown;
    public double IERatio;
    public double RSA;


    public RIPFeatures(DataPoint[] seg, ECGFeatures ecg) {

        DescriptiveStatistics statsSeg = new DescriptiveStatistics();
        for(DataPoint d: seg) {
            statsSeg.addValue(d.value);
        }
        double min = statsSeg.getMin();
        double max = statsSeg.getMax();

        double median = 0; //TODO: Figure out median;


        this.Stretch = max - min;
        this.StretchUp = max - median;
        this.StretchDown = median - min;

        int peakindex = 0;
        double peakValue = -1e9;
        for(int i=0; i<seg.length; i++) {
            if (seg[i].value > peakValue) {
                peakindex = i;
            }
        }

        this.InspDuration = seg[peakindex].timestamp - seg[0].timestamp;
        this.ExprDuration = seg[seg.length].timestamp - seg[peakindex].timestamp;
        this.RespDuration = seg[seg.length].timestamp - seg[0].timestamp;

        this.RSA = rsaCalculateCycle(seg, ecg);

        this.IERatio = InspDuration / ExprDuration;


    }

    private double rsaCalculateCycle(DataPoint[] seg, ECGFeatures ecgFeatures) {
        //TODO: Fix me

        DataPoint[] ecg_rr = ecgFeatures.computeRR();


        return 0;
    }



}
