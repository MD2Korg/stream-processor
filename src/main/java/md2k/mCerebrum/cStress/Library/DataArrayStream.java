package md2k.mCerebrum.cStress.Library;


import md2k.mCerebrum.cStress.Library.Structs.DataPointArray;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
public class DataArrayStream {

    public HashMap<String,Object> metadata;
    public ArrayList<DataPointArray> data;

    public boolean preserve;

    public DataArrayStream(String name) {
        data = new ArrayList<DataPointArray>();
        metadata = new HashMap<String, Object>();
        metadata.put("name", name);
        preserve = false;
    }

    public DataArrayStream(DataArrayStream other) {
        this.data = new ArrayList<DataPointArray>(other.data);
        this.metadata = other.metadata;
        this.preserve = other.preserve;
    }


    public void setPreservedLastInsert(boolean state) {
        preserve = state;
    }

    public void persist(String filename) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, true), "utf-8"));
            for(DataPointArray dp: this.data) {
                writer.write(dp.timestamp);
                for (Double d: dp.value) {
                    writer.write("," + d);
                }
                writer.write("\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        if(!preserve) {
            data.clear();
        } else {
            if (data.size() > 0) {
                DataPointArray temp = data.get(data.size() - 1);
                data.clear();
                data.add(temp);
            } else {
                data.clear();
            }
        }
    }

    public void add(DataPointArray dp) {
        data.add(new DataPointArray(dp));
    }

    public String getName() {
        return (String) metadata.get("name");
    }

}
