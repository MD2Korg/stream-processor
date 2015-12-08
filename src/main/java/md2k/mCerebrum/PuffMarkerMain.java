package md2k.mCerebrum;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.PuffMarker;
import md2k.mCerebrum.cStress.Library.Structs.CSVDataPoint;
import md2k.mCerebrum.cStress.Library.Structs.DataPoint;
import md2k.mCerebrum.cStress.Library.Structs.StressProbability;
import md2k.mCerebrum.cStress.cStress;
import md2k.mCerebrum.cStress.util.PuffMarkerUtils;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleneen <nsleheen@memphis.edu>
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

public class PuffMarkerMain {
    public static void main(String[] args) {
//        String path = "/Users/hnat/Downloads/processedrawdata/SI";
        String path = "D:\\smoking_memphis\\data\\Memphis_Smoking_Lab\\dataset_csv\\";
//        String[] pIds = {"p01", "p02", "p03", "p04", "p05", "p06"};
//        String[][] sIds = {{"s02", "s03", "s04", "s05"}, {"s03", "s04", "s05", "s06"}, {"s01", "s02", "s03"}, {"s01"}, {"s01"}, {"s01"}};
        String[] pIds = {"p01"};
        String[][] sIds = {{"s02"}};
        int ii = 0;
        System.out.println("Left/Right,Total Segment,Height(filtered),Duration (filtered),Roll-Pitch(filtered),TP,Total Puffs");
        for (String pId : pIds) {
            for (String sId : sIds[ii++]) {
                CSVParser tp = new CSVParser();
                String person = pId;
                String session = sId;
                for (int id : PuffMarkerUtils.IDs) {
                    String sensorId = String.format("%02d", id);
                    String fileName = path + person + "_" + session + "_" + sensorId + ".csv";
                    tp.importData(fileName, id);

                }
                PuffMarker puffMarker = new PuffMarker(6000*1000, null, null, person);
                StressProbability output;
                for (CSVDataPoint ap : tp) {
                    DataPoint dp = new DataPoint(ap.timestamp, ap.value);
                    output = puffMarker.add(ap.channel, dp);
                    if (output != null) {
                        System.out.println(output.label + " " + output.probability);
                    }
                }
                puffMarker.process();
            }
        }

    }
}
