package md2k.mCerebrum;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.AUTOSENSE_PACKET;
import md2k.mCerebrum.cStress.Structs.CSVDataPoint;
import md2k.mCerebrum.cStress.cStress;

/**
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
public class Main {

    public static void main(String[] args) {

        String path = "/Users/hnat/Downloads/csvData/";

        String[] p = {"p10","p11","p12","p14","p15","p16"};

        for(String person: p) {

            CSVParser tp = new CSVParser();
            tp.importData(path + person + "_rip.csv", AUTOSENSE.CHEST_RIP);
            tp.importData(path + person + "_ecg.csv", AUTOSENSE.CHEST_ECG);
            tp.importData(path + person + "_accelx.csv", AUTOSENSE.CHEST_ACCEL_X);
            tp.importData(path + person + "_accely.csv", AUTOSENSE.CHEST_ACCEL_Y);
            tp.importData(path + person + "_accelz.csv", AUTOSENSE.CHEST_ACCEL_Z);

            cStress stress = new cStress(60 * 1000, "ecg_rip_accel_60_realtime.model", "ecg_rip_accel_60_realtime_meanstdev.dat");

            for (CSVDataPoint ap : tp) {
                stress.add(ap);
            }

        }
    }
}
