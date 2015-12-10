package md2k.mCerebrum.cStress.library.signalprocessing;

import md2k.mCerebrum.cStress.library.DataPointStream;
import md2k.mCerebrum.cStress.library.structs.DataPoint;

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
 * AutoSense specific signal processing algorithms.
 */
public class AutoSense {

    /**
     * Applies a filter and normalizes the result
     *
     * @param input               Input DataPointStream
     * @param output              Output DataPointStream
     * @param outputNormalized    Output normalized DataPointStream
     * @param filter              Filter array
     * @param normalizePercentile What percentile to utilize for normalization
     */
    public static void applyFilterNormalize(DataPointStream input, DataPointStream output, DataPointStream outputNormalized, double[] filter, int normalizePercentile) {
        double[] sample = new double[input.data.size()];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = input.data.get(i).value;
        }
        double[] result = Filter.conv(sample, filter);

        //Add value to datastream for computing percentiles
        for (int i = 0; i < result.length; i++) {
            output.add(new DataPoint(input.data.get(i).timestamp, result[i]));
        }
        //Normalized based on percentiles
        for (int i = 0; i < sample.length; i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp, output.data.get(i).value / output.getPercentile(normalizePercentile)));
        }
    }

    /**
     * Applies the filter (n^2) and normalizes the result
     *
     * @param input               Input DataPointStream
     * @param output              Output DataPointStream
     * @param outputNormalized    Output normalized DataPointStream
     * @param normalizePercentile What percentile to utilize for normalization
     */
    public static void applySquareFilterNormalize(DataPointStream input, DataPointStream output, DataPointStream outputNormalized,
                                                  int normalizePercentile) {
        //Add value to datastream for computing percentiles
        for (int i = 0; i < input.data.size(); i++) {
            output.add(new DataPoint(input.data.get(i).timestamp, input.data.get(i).value * input.data.get(i).value));
        }
        //Normalized based on percentiles
        for (int i = 0; i < output.data.size(); i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp, output.data.get(i).value
                    / output.getPercentile(normalizePercentile)));
        }
    }
}
