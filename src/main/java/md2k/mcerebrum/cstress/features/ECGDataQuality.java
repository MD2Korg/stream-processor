package md2k.mcerebrum.cstress.features;

/*
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

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.autosense.AUTOSENSE;
import md2k.mcerebrum.cstress.library.dataquality.autosense.ECGQualityCalculation;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.List;

/**
 * Wrapper class for ECGQualityCalculation
 */
public class ECGDataQuality {

    /**
     * Constructor
     * @param datastreams Global datastream object
     * @param qualityThreshold Input quality threshold
     */
    public ECGDataQuality(DataStreams datastreams, double qualityThreshold) {
        DataPointStream ecg = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG);
        DataPointStream ecgQuality = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_QUALITY);

        ECGQualityCalculation ecgComputation = new ECGQualityCalculation(3, 50, 4500, 20, 2, 47);
        List<DataPoint> quality = ecgComputation.computeQuality(ecg.data, 5000); //0.67

        double count = 0;
        for (DataPoint dp : quality) {
            ecgQuality.add(dp);
            if (dp.value == AUTOSENSE.QUALITY_GOOD) {
                count++;
            }
        }

        DataPointStream ecgWindowQuality = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ECG_WINDOW_QUALITY);

        if ((count / quality.size()) > qualityThreshold)
            ecgWindowQuality.add(new DataPoint(quality.get(0).timestamp, AUTOSENSE.QUALITY_GOOD));
        else
            ecgWindowQuality.add(new DataPoint(quality.get(0).timestamp, AUTOSENSE.QUALITY_BAD));


    }
}
