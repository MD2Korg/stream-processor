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

import java.util.HashMap;

public class DataStream {

    public HashMap<String, Object> metadata;
    public boolean preserve;


    /**
     * Constructor
     * @param name Unique name of the DataPoint object
     */
    public DataStream(String name) {
        data = new ArrayList<DataPoint>();
        metadata = new HashMap<String, Object>();
        metadata.put("name", name);
        preserve = false;
        stats = new SummaryStatistics();
        descriptiveStats = new DescriptiveStatistics(10000);
    }

    /**
     * Copy Constructor
     * @param other DataStream object to copy
     */
    public DataStream(DataStream other) {
        this.data = new ArrayList<DataPoint>(other.data);
        this.metadata = other.metadata;
        this.stats = other.stats;
        this.descriptiveStats = other.descriptiveStats;
        this.preserve = other.preserve;
    }

    /**
     * Set method for data stream preservation
     * @param state True to preserve last inserted value after a reset
     */
    public DataStream(String name, DataPoint[] data) {
        this(name);
        for(DataPoint dp: data) {
            this.add(dp);
        }
    }

    public DataStream(String name, List<DataPoint> dataPoints) {
        this(name);
        for(DataPoint dp: dataPoints) {
            this.add(dp);
        }
    }

    public void setPreservedLastInsert(boolean state) {
        preserve = state;
    }

    /**
     * Persist the data stream to the local file system
     * @param filename File name and path where to append the data stream.
     */
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
     * Retrieve stream name
     *
     * @return The unique stream name
     */
    public String getName() {
        return (String) metadata.get("name");
    }
}
