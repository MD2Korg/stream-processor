package md2k.mCerebrum.cStress.Library;


import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
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
 * Core data stream object on which most computation is based in this framework
 */
public class DataPointStream extends DataStream {

    public List<DataPoint> data;

    public SummaryStatistics stats;
    public DescriptiveStatistics descriptiveStats;


    /**
     * Constructor
     *
     * @param name Unique name of the DataPoint object
     */
    public DataPointStream(String name) {
        data = new ArrayList<DataPoint>();
        metadata = new HashMap<String, Object>();
        metadata.put("name", name);
        preserve = false;
        stats = new SummaryStatistics();
        descriptiveStats = new DescriptiveStatistics(10000);
    }

    public DataPointStream(String name, List<DataPoint> dataPoints) {
        this(name);
        for (DataPoint dp : dataPoints) {
            this.add(dp);
        }
    }

    /**
     * Copy Constructor
     *
     * @param other DataPointStream object to copy
     */
    public DataPointStream(DataPointStream other) {
        this.data = new ArrayList<DataPoint>(other.data);
        this.metadata = other.metadata;
        this.stats = other.stats;
        this.descriptiveStats = other.descriptiveStats;
        this.preserve = other.preserve;
    }

    /**
     * Set method for data stream preservation
     *
     * @param state True to preserve last inserted value after a reset
     */
    public void setPreservedLastInsert(boolean state) {
        preserve = state;
    }

    /**
     * Persist the data stream to the local file system
     *
     * @param filename File name and path where to append the data stream.
     */
    @Override
    public void persist(String filename) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "utf-8"));
            for (DataPoint dp : this.data) {
                writer.write(dp.timestamp + ", " + dp.value + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the data stream array for the next interval.  Preserve stats and descriptiveStats if preserve is set
     */
    @Override
    public void reset() {
        if (!preserve) {
            data.clear();
        } else {
            if (data.size() > 0) {
                DataPoint temp = data.get(data.size() - 1);
                data.clear();
                data.add(temp);
            } else {
                data.clear();
            }
        }
    }


    /**
     * Main method to add DataPoint to the data stream.  Updates stats and descriptiveStats and checks for invalid data
     * values.
     *
     * @param dp New DataPoint to add to the data stream
     */
    public void add(DataPoint dp) {
        if (!Double.isNaN(dp.value) && !Double.isInfinite(dp.value)) {
            data.add(new DataPoint(dp));
            stats.addValue(dp.value);
            descriptiveStats.addValue(dp.value);
        }
    }


    /**
     * Percentile computation based on a buffer of datapoints
     *
     * @param i The percentile to retrieve
     * @return Computed percentile
     */
    public double getPercentile(int i) {
        return descriptiveStats.getPercentile(i);
    }

    /**
     * Mean computation based on an online algorithm
     *
     * @return Mean of all DataPoints up to the current time
     */
    public double getMean() {
        return stats.getMean();
    }

    /**
     * Standard deviation computation based on an online algorithm
     *
     * @return Standard deviation of all DataPoints up to the current time
     */
    public double getStandardDeviation() {
        return stats.getStandardDeviation();
    }

    /**
     * Retrieves all values in the current data window
     *
     * @return Array of double values
     */
    public double[] getValues() {
        double result[] = new double[data.size()];
        for (int i = 0; i < data.size(); i++)
            result[i] = data.get(i).value;
        return result;
    }

    /**
     * Retrieves all values in the current data window
     *
     * @return Array of double values normalized based on the mean and standard deviation
     */
    public double[] getNormalizedValues() {
        double result[] = new double[data.size()];
        for (int i = 0; i < data.size(); i++)
            result[i] = (data.get(i).value - stats.getMean()) / stats.getStandardDeviation();
        return result;
    }
}
