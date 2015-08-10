package md2k.mCerebrum.cStress.legacyJava;


import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Structs.DataPoint;

import java.util.ArrayList;

import static md2k.mCerebrum.cStress.Library.window;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Kurt Plarre <kplarre@memphis.edu>
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
public class ECGQualityCalculation {

    private int[] envelBuff;
    private int envelHead;
    private int[] classBuff;
    private int classHead;
    private int ACCEPTABLE_OUTLIER_PERCENT;
    private int OUTLIER_THRESHOLD_HIGH;
    private int OUTLIER_THRESHOLD_LOW;
    private int BAD_SEGMENTS_THRESHOLD;
    private int ECK_THRESHOLD_BAND_LOOSE;

    private int large_stuck = 0;
    private int small_stuck = 0;
    private int large_flip = 0;
    private int small_flip = 0;
    private int max_value = 0;
    private int min_value = 0;
    private int segment_class = 0;

    private int SEGMENT_GOOD = 0;
    private int SEGMENT_BAD = 1;

    private int bad_segments = 0;
    private int amplitude_small = 0;

    //ECGQualityCalculation(3, 50, 4500, 20, 2, 47);
    public ECGQualityCalculation(int bufferLength, int acceptableOutlierPercent, int outlierThresholdHigh, int outlierThresholdLow, int badSegmentsThreshold, int ecgThresholdBandLoose) {
        ACCEPTABLE_OUTLIER_PERCENT = acceptableOutlierPercent;
        OUTLIER_THRESHOLD_HIGH = outlierThresholdHigh;
        OUTLIER_THRESHOLD_LOW = outlierThresholdLow;
        BAD_SEGMENTS_THRESHOLD = badSegmentsThreshold;
        ECK_THRESHOLD_BAND_LOOSE = ecgThresholdBandLoose;

        envelBuff = new int[bufferLength];
        classBuff = new int[bufferLength];
        for (int i = 0; i < bufferLength; i++) {
            envelBuff[i] = 2 * ECK_THRESHOLD_BAND_LOOSE;
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
            int im = ((i == 0) ? (data.length - 1) : (i - 1));
            int ip = ((i == data.length - 1) ? (0) : (i + 1));
            boolean stuck = ((data[i] == data[im]) && (data[i] == data[ip]));
            boolean flip = ((Math.abs(data[i] - data[im]) > 4000) || (Math.abs(data[i] - data[ip]) > 4000));

            if (data[i] > OUTLIER_THRESHOLD_HIGH) {
                if (stuck) large_stuck++;
                if (flip) large_flip++;
            } else if (data[i] < OUTLIER_THRESHOLD_LOW) {
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
        if (100 * outliers > ACCEPTABLE_OUTLIER_PERCENT * data.length) {
            segment_class = SEGMENT_BAD;
        } else {
            segment_class = SEGMENT_GOOD;
        }
    }


    private void classifyBuffer() {
        bad_segments = 0;
        amplitude_small = 0;
        for (int i = 1; i < envelBuff.length; i++) {
            if (classBuff[i] == SEGMENT_BAD) {
                bad_segments++;
            }
            if (envelBuff[i] < ECK_THRESHOLD_BAND_LOOSE) {
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

        if (bad_segments > BAD_SEGMENTS_THRESHOLD) {
            return AUTOSENSE.QUALITY_BAND_OFF;
        } else if (2 * amplitude_small > envelBuff.length) {
            return AUTOSENSE.QUALITY_BAND_LOOSE;
        }
        return AUTOSENSE.QUALITY_GOOD;
    }

    public boolean computeQuality(DataPoint[] ecg, int windowSize, double threshold) {

        ArrayList<DataPoint[]> windowedECG = window(ecg, windowSize);

        int resultCount = 0;

        for(DataPoint[] dpA: windowedECG) {
            int[] data = new int[dpA.length];
            int i=0;
            for(DataPoint s: dpA) {
                data[i++] = (int) s.value;
            }
            if (data.length > 0 && currentQuality(data) == AUTOSENSE.QUALITY_GOOD) {
                resultCount++;
            }
        }

        return ((resultCount/windowedECG.size()) > threshold);

    }

}

