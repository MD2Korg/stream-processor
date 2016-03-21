package md2k.mcerebrum.cstress.library.signalprocessing;

import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

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
 * Data smoothing library routines
 */
public class Smoothing {
    /**
     * Normalization routine
     * <p>
     * Normalize an input data stream based on mean and standard deviation
     * </p>
     *
     * @param input  Input datastream object
     * @param output Output datastream object
     */
    public static void normalize(DataPointStream output, DataPointStream input) {
        for (DataPoint dp : input.data) {
            output.add(new DataPoint(dp.timestamp, (dp.value - input.getMean()) / input.getStandardDeviation()));
        }
    }

    /**
     * Reimplementation of Matlab's smooth function
     * <p>
     * Reference Matlab's implementation in smooth.m
     * </p>
     *
     * @param output Output datastream object
     * @param input  Input datastream object
     * @param n      Windows size
     */
    public static void smooth(DataPointStream output, DataPointStream input, int n) {
        int windowSize = 1;
        double sum;
        for (int i = 0; i < input.data.size(); i++) {
            sum = 0.0;
            int startingPoint;
            if ((input.data.size() - i + 1) < n) {
                startingPoint = input.data.size() - windowSize;
            } else {
                startingPoint = (int) Math.max(Math.floor(i - n / 2), 0);
            }
            for (int j = startingPoint; j < startingPoint + windowSize; j++) {
                sum += input.data.get(j).value;
            }
            sum /= (double) windowSize;

            output.add(new DataPoint(input.data.get(i).timestamp, sum));

            if (windowSize < n && (input.data.size() - i) > n) { //Increase windowSize until n
                windowSize += 2;
            } else if ((input.data.size() - i + 1) < n) {
                windowSize -= 2;
            }
        }
    }


    /**
     * Exponentially Weighted Moving Average
     * <p>
     * Reference: https://en.wikipedia.org/wiki/Moving_average
     * </p>
     *
     * @param x     Newest sample value
     * @param y     Historical value
     * @param alpha Degree of weighting between 0 and 1
     * @return New weighted average
     */
    public static double ewma(double x, double y, double alpha) {
        return alpha * x + (1 - alpha) * y;
    }
}
