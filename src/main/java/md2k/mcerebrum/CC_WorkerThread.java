package md2k.mcerebrum;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.StreamProcessor;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.library.Time;
import md2k.mcerebrum.cstress.library.datastream.DataPointInterface;
import md2k.mcerebrum.cstress.library.structs.DataPoint;
import md2k.mcerebrum.cstress.library.structs.DataPointArray;

import java.io.File;
import java.io.FilenameFilter;

/*
 * Copyright (c) 2017, The University of Memphis, MD2K Center
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
public class CC_WorkerThread implements Runnable {

    private String path;
    private String cStressModelPath;
    private String cStressRIPModelPath;
    private String puffMarkerModelPath;

    public CC_WorkerThread(String path) {
        this.path = path;
    }

    public CC_WorkerThread(String path, String cStressModelPath, String cStressRIPModelPath, String puffMarkerModelPath) {
        this.path = path;
        this.cStressModelPath = cStressModelPath;
        this.cStressRIPModelPath = cStressRIPModelPath;
        this.puffMarkerModelPath = puffMarkerModelPath;
    }

    private String fileMatcher(String basepath, String inputRegex) {
        final String regex = inputRegex;
        File file = new File(basepath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                return (f.isFile() && f.getName().matches(regex));
            }
        });
        if (directories.length > 0)
            return basepath + directories[0];
        else
            return "None";
    }

    @Override
    public void run() {

        CC_CSVParser tp = new CC_CSVParser();


        tp.importData(fileMatcher(path, ".*org.md2k.autosense+RESPIRATION.*.bz2"), AUTOSENSE.CHEST_RIP);
        tp.importData(fileMatcher(path, ".*org.md2k.autosense+ECG.*.bz2"), AUTOSENSE.CHEST_ECG);
        tp.importData(fileMatcher(path, ".*org.md2k.autosense+ACCELEROMETER_X.*.bz2"), AUTOSENSE.CHEST_ACCEL_X);
        tp.importData(fileMatcher(path, ".*org.md2k.autosense+ACCELEROMETER_Y.*.bz2"), AUTOSENSE.CHEST_ACCEL_Y);
        tp.importData(fileMatcher(path, ".*org.md2k.autosense+ACCELEROMETER_Z.*.bz2"), AUTOSENSE.CHEST_ACCEL_Z);

//        tp.importData(path + "/left-wrist-accelx.csv", PUFFMARKER.LEFTWRIST_ACCEL_X);
//        tp.importData(path + "/left-wrist-accely.csv", PUFFMARKER.LEFTWRIST_ACCEL_Y);
//        tp.importData(path + "/left-wrist-accelz.csv", PUFFMARKER.LEFTWRIST_ACCEL_Z);
//        tp.importData(path + "/left-wrist-gyrox.csv", PUFFMARKER.LEFTWRIST_GYRO_X);
//        tp.importData(path + "/left-wrist-gyroy.csv", PUFFMARKER.LEFTWRIST_GYRO_Y);
//        tp.importData(path + "/left-wrist-gyroz.csv", PUFFMARKER.LEFTWRIST_GYRO_Z);
//
//        tp.importData(path + "/right-wrist-accely.csv", PUFFMARKER.RIGHTWRIST_ACCEL_Y);
//        tp.importData(path + "/right-wrist-accelx.csv", PUFFMARKER.RIGHTWRIST_ACCEL_X);
//        tp.importData(path + "/right-wrist-accelz.csv", PUFFMARKER.RIGHTWRIST_ACCEL_Z);
//        tp.importData(path + "/right-wrist-gyrox.csv", PUFFMARKER.RIGHTWRIST_GYRO_X);
//        tp.importData(path + "/right-wrist-gyroy.csv", PUFFMARKER.RIGHTWRIST_GYRO_Y);
//        tp.importData(path + "/right-wrist-gyroz.csv", PUFFMARKER.RIGHTWRIST_GYRO_Z);

        tp.sort();

        int windowSize = 60000;

        StreamProcessor streamProcessor = new StreamProcessor(windowSize);
//        streamProcessor.importDatastreams(path + "/streamProcessor.serialized");
        streamProcessor.setPath(path);
        streamProcessor.loadModel("cStressModel", cStressModelPath);
        streamProcessor.loadModel("cStressRIPModel", cStressRIPModelPath);
        streamProcessor.loadModel("puffMarkerModel", puffMarkerModelPath);

        streamProcessor.dpInterface = new DataPointInterface() {
            @Override
            public void dataPointHandler(String stream, DataPoint dp) {
                System.out.println(path + "/" + stream + " " + dp);
            }

            @Override
            public void dataPointArrayHandler(String stream, DataPointArray dp) {
                System.out.println(path + "/" + stream + " " + dp);
            }
        };

//        streamProcessor.registerCallbackDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_FV);
//        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_STRESSLABEL);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_PUFFMARKER_PROBABILITY);
        streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL);

        long windowStartTime = -1;
        for (CSVDataPoint ap : tp) {
            DataPoint dp = new DataPoint(ap.timestamp, ap.value);

            if (windowStartTime < 0) {
                windowStartTime = Time.nextEpochTimestamp(dp.timestamp, windowSize);
            }

            if ((dp.timestamp - windowStartTime) >= windowSize) { //Process the buffer every windowSize milliseconds
                streamProcessor.go();
                windowStartTime += windowSize;
//                streamProcessor.exportDatastreams(path + "/streamProcessor.serialized");
            }

            streamProcessor.add(ap.channel, dp);

        }
        streamProcessor.go();
    }


}
