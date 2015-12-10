package md2k.mCerebrum;

import md2k.mCerebrum.cStress.StreamProcessor;
import md2k.mCerebrum.cStress.autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.autosense.PUFFMARKER;
import md2k.mCerebrum.cStress.library.structs.CSVDataPoint;
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
public class WorkerThread implements Runnable {

    private String path;
    private String id;


    public WorkerThread(String path, String id) {
        this.path = path;
        this.id = id;
    }

    @Override
    public void run() {

        CSVParser tp = new CSVParser();
        tp.importData(path + id + "/rip.txt", AUTOSENSE.CHEST_RIP);
        tp.importData(path + id + "/ecg.txt", AUTOSENSE.CHEST_ECG);
        tp.importData(path + id + "/accelx.txt", AUTOSENSE.CHEST_ACCEL_X);
        tp.importData(path + id + "/accely.txt", AUTOSENSE.CHEST_ACCEL_Y);
        tp.importData(path + id + "/accelz.txt", AUTOSENSE.CHEST_ACCEL_Z);

        tp.importData(path + id + "/left-wrist-accelx.txt", PUFFMARKER.LEFTWRIST_ACCEL_X);
        tp.importData(path + id + "/left-wrist-accely.txt", PUFFMARKER.LEFTWRIST_ACCEL_Y);
        tp.importData(path + id + "/left-wrist-accelz.txt", PUFFMARKER.LEFTWRIST_ACCEL_Z);
        tp.importData(path + id + "/left-wrist-gyrox.txt", PUFFMARKER.LEFTWRIST_GYRO_X);
        tp.importData(path + id + "/left-wrist-gyroy.txt", PUFFMARKER.LEFTWRIST_GYRO_Y);
        tp.importData(path + id + "/left-wrist-gyroz.txt", PUFFMARKER.LEFTWRIST_GYRO_Z);

        tp.importData(path + id + "/right-wrist-accely.txt", PUFFMARKER.RIGHTWRIST_ACCEL_Y);
        tp.importData(path + id + "/right-wrist-accelx.txt", PUFFMARKER.RIGHTWRIST_ACCEL_X);
        tp.importData(path + id + "/right-wrist-accelz.txt", PUFFMARKER.RIGHTWRIST_ACCEL_Z);
        tp.importData(path + id + "/right-wrist-gyrox.txt", PUFFMARKER.RIGHTWRIST_GYRO_X);
        tp.importData(path + id + "/right-wrist-gyroy.txt", PUFFMARKER.RIGHTWRIST_GYRO_Y);
        tp.importData(path + id + "/right-wrist-gyroz.txt", PUFFMARKER.RIGHTWRIST_GYRO_Z);
        
        tp.sort();

        StreamProcessor stress = new StreamProcessor(60 * 1000, path, id);

        for (CSVDataPoint ap : tp) {
            DataPoint dp = new DataPoint(ap.timestamp, ap.value);
            stress.add(ap.channel, dp);
        }
    }
}
