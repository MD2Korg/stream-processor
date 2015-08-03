package md2k.mCerebrum.cStress.tests;

import md2k.mCerebrum.cStress.Library;
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
public class ECGFeaturesTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

//    @Test
//    public void testComputeRR() throws Exception {
//
//    }
//
//    @Test
//    public void testDetect_outlier_v2() throws Exception {
//
//    }
//
//    @Test
//    public void testDetect_Rpeak() throws Exception {
//
//    }
//
//    @Test
//    public void testRr_ave_update() throws Exception {
//
//    }

    @Test
    public void testBlackman() throws Exception {
        double[] trueBlackman10;
        double[] result;
        double sum;

        //Test window of length 10
        int windowLength = 10;
        trueBlackman10 = new double[]{ 0, 0.0509, 0.2580, 0.6300, 0.9511, 0.9511, 0.6300, 0.2580, 0.0509, 0};

        result = Library.blackman(windowLength);

        sum = 0.0;
        for(int i=0; i<result.length; i++) {
            sum += Math.abs(result[i]-trueBlackman10[i]);
        }
        assertEquals(sum,0.0,1e-3);

        //Test window of length 0
        windowLength = 0;
        result = Library.blackman(windowLength);
        assertArrayEquals(result,new double[0],1e-4);
    }

    @Test
    public void testFirls() throws Exception {

    }

    @Test
    public void testConv() throws Exception {
        double[] signal = {1,2,3,2,1};
        double[] kernel = {1,0,1};
        double[] trueAnswer = {2,4,4,4,2};

        double[] result = Library.conv(signal, kernel);

        assertArrayEquals(result,trueAnswer,1e-3);
    }

//    @Test
//    public void testHeartRateLFHF() throws Exception {
//
//    }
//
//    @Test
//    public void testHeartRatePower() throws Exception {
//
//    }
//
//    @Test
//    public void testLomb() throws Exception {
//
//    }
}