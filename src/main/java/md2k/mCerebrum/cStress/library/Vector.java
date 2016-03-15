package md2k.mcerebrum.cstress.library;

import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.ArrayList;
import java.util.List;

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
 * Support routines for handling vector computation
 */
public class Vector {
    /**
     * Convert inputs to a magnitude vector
     *
     * @param x X axis
     * @param y Y axis
     * @param z Z axis
     * @return Magnitude of (x,y,z) tuple
     */
    public static double[] magnitude(DataPoint[] x, DataPoint[] y, DataPoint[] z) {
        double[] result = new double[Math.min(Math.min(x.length, y.length), z.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.sqrt(Math.pow(x[i].value, 2) + Math.pow(y[i].value, 2) + Math.pow(z[i].value, 2));
        }
        return result;
    }

    /**
     * Compute a discrete derivative
     *
     * @param dp DataPoint array
     * @return Difference of each consecutive pair of DataPoints
     */
    public static List<DataPoint> diff(List<DataPoint> dp) {
        List<DataPoint> result = new ArrayList<DataPoint>();
        if (dp.size() != 0) {
            for (int i = 0; i < dp.size() - 1; i++) {
                result.add(new DataPoint(dp.get(i).timestamp, dp.get(i + 1).value - dp.get(i).value));
            }
        }
        return result;
    }


    /**
     * Compute the magnitude of a single (x,y,z) tuple
     * @param x
     * @param y
     * @param z
     * @return Magnitude
     */
    public static double getMagnitude(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Computes the magnitude of a list of tuples (x,y,z)
     * @param gyr_mag Output magnitude datastream
     * @param x List of DataPoints for the x-axis
     * @param y List of DataPoints for the y-axis
     * @param z List of DataPoints for the z-axis
     */
    public static void magnitude(DataPointStream gyr_mag, List<DataPoint> x, List<DataPoint> y, List<DataPoint> z) {
        for (int i = 0; i < x.size(); i++) {
            double mag = getMagnitude(x.get(i).value, y.get(i).value, z.get(i).value);
            gyr_mag.add(new DataPoint(x.get(i).timestamp, mag));
        }
    }
}
