package md2k.mCerebrum.cStress;

import md2k.mCerebrum.cStress.autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.autosense.PUFFMARKER;
import md2k.mCerebrum.cStress.features.*;
import md2k.mCerebrum.cStress.library.datastream.DataStreams;
import md2k.mCerebrum.cStress.library.structs.DataPoint;
import org.apache.commons.math3.exception.NotANumberException;


/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
 * - Nazir Saleneen <nsleheen@memphis.edu>
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
 * Main class that implements StreamProcessor and controls the data processing pipeline
 */
public class StreamProcessor {

    public DataPointInterface dpInterface;
    private long windowSize;
    private String path;
    private DataStreams datastreams = new DataStreams();
    /**
     * Main constructor for StreamProcessor
     *
     * @param windowSize  Time in milliseconds to segment and buffer data before processing
     * @param path        Location where data will be persisted on disk
     * @param participant A participant identifier that should identify a directory within 'path'
     */
    public StreamProcessor(long windowSize) {
        this.windowSize = windowSize;

        configureDataStreams();
    }

    private void configureDataStreams() {
        //Configure Data Streams
        datastreams.getDataPointStream("org.md2k.cstress.data.ecg").metadata.put("frequency", 64.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.ecg").metadata.put("channelID", AUTOSENSE.CHEST_ECG);

        datastreams.getDataPointStream("org.md2k.cstress.data.rip").metadata.put("frequency", 64.0 / 3.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.rip").metadata.put("channelID", AUTOSENSE.CHEST_RIP);

        datastreams.getDataPointStream("org.md2k.cstress.data.accelx").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accelx").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream("org.md2k.cstress.data.accely").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accely").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);

        datastreams.getDataPointStream("org.md2k.cstress.data.accelz").metadata.put("frequency", 64.0 / 6.0);
        datastreams.getDataPointStream("org.md2k.cstress.data.accelz").metadata.put("channelID", AUTOSENSE.CHEST_ACCEL_X);
    }


    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Main computation loop that processes all buffered data through several different classes
     */
    public void process() {

        try {
            //Data quality computations
            ECGDataQuality ecgDQ = new ECGDataQuality(datastreams, AUTOSENSE.AUTOSENSE_ECG_QUALITY);
            RIPDataQuality ripDQ = new RIPDataQuality(datastreams, AUTOSENSE.AUTOSENSE_RIP_QUALITY);

            //AutoSense features
            AccelerometerFeatures af = new AccelerometerFeatures(datastreams, AUTOSENSE.ACTIVITY_THRESHOLD, AUTOSENSE.ACCEL_WINDOW_SIZE);
            ECGFeatures ef = new ECGFeatures(datastreams);
            RIPFeatures rf = new RIPFeatures(datastreams);

            //AutoSense wrist features
            AccelGyroFeatures leftWrist = new AccelGyroFeatures(datastreams, PUFFMARKER.LEFT_WRIST);
            AccelGyroFeatures rightWrist = new AccelGyroFeatures(datastreams, PUFFMARKER.RIGHT_WRIST);


        } catch (IndexOutOfBoundsException e) {
            System.err.println("Stress Exception Handler: IndexOutOfBoundsException");
            e.printStackTrace();
        }


    }

    /**
     * Main processing method for computing results after basic datastream processing
     */
    public void generateResults() {
        try {
            cStress cs = new cStress(datastreams);
            PuffMarker pm = new PuffMarker(datastreams);

        } catch (NotANumberException e) {
            System.err.println("Generate result error");
        }
    }

    public void go() {
        process();
        generateResults();
        evaluateModel();
        resetDataStreams();
    }

    private void evaluateModel() {

    }


    /**
     * Add new DataPoint to the buffers
     *
     * @param channel Identifies which sensor stream the DataPoint is associated with
     * @param dp      DataPoint containing a timestamp and value
     */
    public void add(int channel, DataPoint dp) {
        switch (channel) {
            case AUTOSENSE.CHEST_ECG:
                datastreams.getDataPointStream("org.md2k.cstress.data.ecg").add(dp);
                break;

            case AUTOSENSE.CHEST_RIP:
                datastreams.getDataPointStream("org.md2k.cstress.data.rip").add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_X:
                datastreams.getDataPointStream("org.md2k.cstress.data.accelx").add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_Y:
                datastreams.getDataPointStream("org.md2k.cstress.data.accely").add(dp);
                break;

            case AUTOSENSE.CHEST_ACCEL_Z:
                datastreams.getDataPointStream("org.md2k.cstress.data.accelz").add(dp);
                break;


            case PUFFMARKER.LEFTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_X)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_Y)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_ACCEL_Z)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_X)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_Y)).add(dp);
                break;

            case PUFFMARKER.LEFTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_LEFTWRIST_GYRO_Z)).add(dp);
                break;


            case PUFFMARKER.RIGHTWRIST_ACCEL_X:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_X)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_ACCEL_Y:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_Y)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_ACCEL_Z:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_ACCEL_Z)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_X:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_X)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_Y:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_Y)).add(dp);
                break;

            case PUFFMARKER.RIGHTWRIST_GYRO_Z:
                (datastreams.getDataPointStream(PUFFMARKER.KEY_DATA_RIGHTWRIST_GYRO_Z)).add(dp);
                break;


            default:
                System.out.println("NOT INTERESTED: " + dp);
                break;
        }
    }

    /**
     * Persist and reset all datastreams
     */
    private void resetDataStreams() {
        datastreams.persist(path + "/");
        datastreams.reset();
    }


    /**
     * Handle registration of a callback interface
     * @param s Stream identifier
     */
    public void registerCallbackDataStream(String s) {
        datastreams.registerDataPointInterface(s, dpInterface);
    }


    /**
     * Handle registration of a callback interface
     * @param s Stream identifier
     */
    public void registerCallbackDataArrayStream(String s) {
        datastreams.registerDataArrayInterface(s, dpInterface);
    }
}
