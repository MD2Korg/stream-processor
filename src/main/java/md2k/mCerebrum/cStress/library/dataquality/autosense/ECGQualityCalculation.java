package md2k.mcerebrum.cstress.library.dataquality.autosense;


import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.library.Time;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.ArrayList;
import java.util.List;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Kurt Plarre <kplarre@memphis.edu>
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

public class ECGQualityCalculation {

    private int[] envelBuff;
    private int envelHead;
    private int[] classBuff;
    private int classHead;
    private int acceptableOutlierPercent;
    private int outlierThresholdHigh;
    private int outlierThresholdLow;
    private int badSegmentsThreshold;
    private int ecgThresholdBandLoose;

    private int large_stuck = 0;
    private int small_stuck = 0;
    private int large_flip = 0;
    private int small_flip = 0;
    private int max_value = 0;
    private int min_value = 0;
    private int segment_class = 0;

    private int bad_segments = 0;
    private int amplitude_small = 0;

    //ECGQualityCalculation(3, 50, 4500, 20, 2, 47);
    public ECGQualityCalculation(int bufferLength,
                                 int acceptableOutlierPercent,
                                 int outlierThresholdHigh,
                                 int outlierThresholdLow,
                                 int badSegmentsThreshold,
                                 int ecgThresholdBandLoose) {

        this.acceptableOutlierPercent = acceptableOutlierPercent;
        this.outlierThresholdHigh = outlierThresholdHigh;
        this.outlierThresholdLow = outlierThresholdLow;
        this.badSegmentsThreshold = badSegmentsThreshold;
        this.ecgThresholdBandLoose = ecgThresholdBandLoose;

        envelBuff = new int[bufferLength];
        classBuff = new int[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
            envelBuff[i] = 2 * ecgThresholdBandLoose;
            classBuff[i] = 0;
        }
        envelHead = 0;
        classHead = 0;
    }


    private void classifyDataPoints(int[] data) {
        large_stuck = 0;
        small_stuck = 0;
        large_flip = 0;
        small_flip = 0;
        max_value = data[0];
        min_value = data[0];

        for (int i = 0; i < data.length; i++) {
            int im;
            if (i == 0) {
                im = (data.length - 1);
            } else {
                im = (i - 1);
            }
            int ip;
            if (i == data.length - 1) {
                ip = (0);
            } else {
                ip = (i + 1);
            }
            boolean stuck = ((data[i] == data[im]) && (data[i] == data[ip]));
            boolean flip = ((Math.abs(data[i] - data[im]) > 4000) || (Math.abs(data[i] - data[ip]) > 4000));

            if (data[i] > outlierThresholdHigh) {
                if (stuck) large_stuck++;
                if (flip) large_flip++;
            } else if (data[i] < outlierThresholdLow) {
                if (stuck) small_stuck++;
                if (flip) small_flip++;
            } else {
                if (data[i] > max_value) max_value = data[i];
                if (data[i] < min_value) min_value = data[i];
            }
        }
    }


    private void classifySegment(int[] data) {
        int outliers = large_stuck + large_flip + small_stuck + small_flip;
        if (100 * outliers > acceptableOutlierPercent * data.length) {
            segment_class = AUTOSENSE.SEGMENT_BAD;
        } else {
            segment_class = AUTOSENSE.SEGMENT_GOOD;
        }
    }


    private void classifyBuffer() {
        bad_segments = 0;
        amplitude_small = 0;
        for (int i = 1; i < envelBuff.length; i++) {
            if (classBuff[i] == AUTOSENSE.SEGMENT_BAD) {
                bad_segments++;
            }
            if (envelBuff[i] < ecgThresholdBandLoose) {
                amplitude_small++;
            }
        }
    }


    public int currentQuality(int[] data) {
        classifyDataPoints(data);
        classifySegment(data);

        classBuff[(classHead++) % classBuff.length] = segment_class;
        envelBuff[(envelHead++) % envelBuff.length] = max_value - min_value;
        classifyBuffer();

        if (bad_segments > badSegmentsThreshold) {
            return AUTOSENSE.QUALITY_BAND_OFF;
        } else if (2 * amplitude_small > envelBuff.length) {
            return AUTOSENSE.QUALITY_BAND_LOOSE;
        }
        return AUTOSENSE.QUALITY_GOOD;
    }

    /**
     * Interface routine between the old Java AutoSense quality calculation and stream-processor
     * @param ecg Input ECG DataPoint list
     * @param windowSize The size of a window in milliseconds
     * @return DataPoint list of qualities
     */
    public List<DataPoint> computeQuality(List<DataPoint> ecg, long windowSize) {

        List<DataPoint[]> windowedECG = Time.window(ecg, windowSize);
        List<DataPoint> result = new ArrayList<DataPoint>();

        for (DataPoint[] dpA : windowedECG) {
            int[] data = new int[dpA.length];
            int i = 0;
            for (DataPoint s : dpA) {
                data[i++] = (int) s.value;
            }
            if (data.length > 0) {
                result.add(new DataPoint(dpA[0].timestamp, currentQuality(data)));
            }
        }


        return result;

    }

}

