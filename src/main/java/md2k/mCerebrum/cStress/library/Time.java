package md2k.mcerebrum.cstress.library;

import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.ArrayList;
import java.util.Date;
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
 * Support routines for handline time
 */
public class Time {
    /**
     * Determine the next Epoch timestamp
     *
     * @param timestamp   Input timestamp
     * @param granularity Resolution in milliseconds on which to align the windows
     * @return Next time in milliseconds after timestamp
     */
    public static long nextEpochTimestamp(long timestamp, long granularity) {
        long previousMinute = timestamp / granularity;
        Date date = new Date((previousMinute + 1) * granularity);
        return date.getTime();
    }

    /**
     * Windowing function for DataPoint arrays
     *
     * @param data Input data array
     * @param size Time window size in milliseconds
     * @return ArrayList of data split by size
     */
    public static List<DataPoint[]> window(List<DataPoint> data, long size) {
        List<DataPoint[]> result = new ArrayList<DataPoint[]>();

        if (data.size() > 0) {
            long startTime = nextEpochTimestamp(data.get(0).timestamp, size) - size; //Get next minute window and subtract a minute to arrive at the appropriate startTime
            List<DataPoint> tempArray = new ArrayList<DataPoint>();
            DataPoint[] temp;
            for (DataPoint dp : data) {
                if (dp.timestamp < startTime + size) {
                    tempArray.add(dp);
                } else {
                    temp = new DataPoint[tempArray.size()];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = tempArray.get(i);
                    }
                    if (temp.length > 0) {
                        result.add(temp);
                    }
                    tempArray = new ArrayList<DataPoint>();
                    startTime += size;
                }
            }
            temp = new DataPoint[tempArray.size()];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = tempArray.get(i);
            }
            if (temp.length > 0) {
                result.add(temp);
            }
        }
        return result;
    }
}
