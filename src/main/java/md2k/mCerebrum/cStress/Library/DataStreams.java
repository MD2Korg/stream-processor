package md2k.mCerebrum.cStress.Library;

import java.util.TreeMap;

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
 * Main object that contains all data streams in this library
 */
public class DataStreams {

    private TreeMap<String, DataStream> datastreams;

    /**
     * Constructor
     */
    public DataStreams() {
        datastreams = new TreeMap<String, DataStream>();
    }


    /**
     * Retrieve a DataPointStream from the DataStreams object
     * <p>
     * Will create the DataPointStream if it does not exist
     *
     * @param stream String identifier of the stream to retrieve
     * @return DataPointStream
     */
    public DataStream get(String stream) {
//        if (!datastreams.containsKey(stream)) {
//            datastreams.put(stream, new DataStream(stream));
//        }
        return datastreams.get(stream);
    }


    /**
     * Iterate through all data streams and persist them to disk
     *
     * @param filebase Based directory where data streams are persisted
     */
    public void persist(String filebase) {
        for (String key : datastreams.keySet()) {
            datastreams.get(key).persist(filebase + datastreams.get(key).getName() + ".csv");
        }
    }

    /**
     * Reset all data streams
     */
    public void reset() {
        for (String key : datastreams.keySet()) {
            datastreams.get(key).reset();
        }
    }
}
