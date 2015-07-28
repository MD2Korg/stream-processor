package md2k.mCerebrum;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE_PACKET;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
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
public class TOSParser implements Iterable<AUTOSENSE_PACKET> {

    private final TreeMap<Long, AUTOSENSE_PACKET> data;


    public TOSParser() {
        this.data = new TreeMap<>();
    }

    public void importData(String filename) {

        AUTOSENSE_PACKET tempPacket;

        String[] tokens;
        int[] data = new int[]{0, 0, 0, 0, 0};
        long timestamp;
        int channelID;

        File file = new File(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                tokens = scanner.nextLine().split(",");
                channelID = Integer.parseInt(tokens[0]);
                timestamp = Long.parseLong(tokens[6]);
                for (int i = 1; i < 6; i++) {
                    data[i - 1] = Integer.parseInt(tokens[i]);
                }

                tempPacket = new AUTOSENSE_PACKET(timestamp, channelID, data);
                this.data.put(tempPacket.timestamp, tempPacket);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Iterator<AUTOSENSE_PACKET> iterator() {
        return this.data.values().iterator();
    }
}
