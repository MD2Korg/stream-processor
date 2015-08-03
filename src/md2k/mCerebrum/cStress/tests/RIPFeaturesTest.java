package md2k.mCerebrum.cStress.tests;

import md2k.mCerebrum.cStress.Features.RIPFeatures;
import md2k.mCerebrum.cStress.Library;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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
public class RIPFeaturesTest {

    RIPFeatures ripFeature;

    @Before
    public void setUp() throws Exception {
        ripFeature = new RIPFeatures();
    }

    @After
    public void tearDown() throws Exception {

    }

//    @Test
//    public void testWindow() throws Exception {
//
//    }

//    @Test
//    public void testPeakvalley_v2() throws Exception {
//
//    }

    @Test
    public void testLocalMaxMin() throws Exception {


    }

//    @Test
//    public void testInterceptOutlierDetectorRIPLamia() throws Exception {
//
//
//
//    }

    @Test
    public void testSmooth() throws Exception {
        double[] inputdata = {1, 2, 3, 4, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5, -4, -3, -2, -1, 0, 3, 5, 3, 0, -3, -5, -3, 0};
        double[] correctResult = {1.0000, 2.0000, 3.0000, 3.6000, 3.8000, 3.6000, 3.0000, 2.0000, 1.0000, 0.0000, -1.0000, -2.0000, -3.0000, -3.6000, -3.8000, -3.6000, -3.0000, -2.0000, -0.6000, 1.0000, 2.0000, 2.2000, 1.6000, 0, -1.6000, -2.2000, -2.6667, 0};
        DataPoint[] inputdp = new DataPoint[inputdata.length];
        for(int i = 0; i<inputdata.length; i++) {
            inputdp[i] = new DataPoint(inputdata[i],i);
        }

        DataPoint[] result = Library.smooth(inputdp, 5);
        double sum = 0.0;
        for(int i=0; i< result.length; i++) {
            sum += Math.abs(result[i].value-correctResult[i]);
        }
        assertTrue(sum < 1e-4);

    }
}