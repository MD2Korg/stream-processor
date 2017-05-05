package md2k.mcerebrum;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;

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
public class CC_CSVParser {

    private ArrayList<BufferedReader> readers;
    private ArrayList<Integer> channels;
    private ArrayList<CSVDataPoint> oldestValues;

    public CC_CSVParser() {
        this.readers = new ArrayList<BufferedReader>();
        this.channels = new ArrayList<Integer>();
        this.oldestValues = new ArrayList<CSVDataPoint>();
    }

    public CSVDataPoint getNextValue() {
        long oldest = this.oldestValues.get(0).timestamp;

        int index = -1;
        for (int i = 0; i < this.oldestValues.size(); i++) {
            if (this.oldestValues.get(i).timestamp > 0 && oldest >= this.oldestValues.get(i).timestamp) {
                index = i;
            }
        }
        if (index == -1) {
            return new CSVDataPoint(-1,-1,-1);
        }

        CSVDataPoint result = new CSVDataPoint(this.oldestValues.get(index).channel,this.oldestValues.get(index).timestamp,this.oldestValues.get(index).value);

        this.oldestValues.set(index, parseLine(this.readers.get(index), this.channels.get(index)));

        return result;
    }

    CSVDataPoint parseLine(BufferedReader reader, int channel) {
        String line;

        CSVDataPoint tempPacket;
        String[] tokens;
        double data;
        long timestamp;

        try {
            line = reader.readLine();
            if (line != null) {

                tokens = line.split(",");
                double ts = Double.parseDouble(tokens[0]);
                timestamp = (long) ts;
                data = Double.parseDouble(tokens[2]);

                tempPacket = new CSVDataPoint(channel, timestamp, data);
                return tempPacket;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CSVDataPoint(0,-1,0);
    }


    public void importData(String filename, int channel) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(filename))));
            this.readers.add(reader);
            this.channels.add(channel);

            this.oldestValues.add(parseLine(reader, channel));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
