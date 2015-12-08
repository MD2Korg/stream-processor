package md2k.mCerebrum.cStress.Library;

import md2k.mCerebrum.cStress.Library.Structs.DataPoint;

import java.util.ArrayList;
import java.util.Date;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class Time {
    public static long nextEpochTimestamp(long timestamp) {
        long previousMinute = timestamp / (60 * 1000);
        Date date = new Date((previousMinute + 1) * (60 * 1000));
        return date.getTime();
    }

    public static ArrayList<DataPoint[]> window(ArrayList<DataPoint> data, int size) {
        ArrayList<DataPoint[]> result = new ArrayList<DataPoint[]>();

        if(data.size() > 0) {
            long startTime = nextEpochTimestamp(data.get(0).timestamp) - 60 * 1000; //Get next minute window and subtract a minute to arrive at the appropriate startTime
            ArrayList<DataPoint> tempArray = new ArrayList<DataPoint>();
            DataPoint[] temp;
            for (DataPoint dp : data) {
                if (dp.timestamp < startTime + size) {
                    tempArray.add(dp);
                } else {
                    temp = new DataPoint[tempArray.size()];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = tempArray.get(i);
                    }
                    result.add(temp);
                    tempArray = new ArrayList<DataPoint>();
                    startTime += size;
                }
            }
            temp = new DataPoint[tempArray.size()];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = tempArray.get(i);
            }
            result.add(temp);
        }
        return result;
    }
}
