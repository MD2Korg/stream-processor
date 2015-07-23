package md2k.mCerebrum.cStress.tests;

import md2k.mCerebrum.cStress.Features.AccelerometerFeatures;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import org.junit.Test;

import static org.junit.Assert.*;

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
public class AccelerometerFeaturesTest {

//    @Test
//    public void testComputeEnergy() throws Exception {
//
//    }

    @Test
    public void testNextPower2() throws Exception {
        assertTrue(AccelerometerFeatures.nextPower2(0)==1);
        assertTrue(AccelerometerFeatures.nextPower2(1)==1);
        assertTrue(AccelerometerFeatures.nextPower2(200)==256);
        assertTrue(AccelerometerFeatures.nextPower2(255)==256);
        assertTrue(AccelerometerFeatures.nextPower2(1000)==1024);
    }

    @Test
    public void testMagnitude() throws Exception {
        DataPoint[] x = new DataPoint[5];
        DataPoint[] y = new DataPoint[5];
        DataPoint[] z = new DataPoint[5];
        for(int i=0; i<x.length; i++) {
            x[i] = new DataPoint(i,i);
            y[i] = new DataPoint(i*i,i);
            z[i] = new DataPoint(i*i*i,i);
        }
        double[] result = AccelerometerFeatures.magnitude(x,y,z);

        double[] correctResult = {0, 1.73205080756888, 9.16515138991168, 28.6181760425084, 66.0908465674332};
        assertArrayEquals(result,correctResult,1e-3);

        DataPoint[] input = new DataPoint[0];
        double[] emptyresult = {};
        result = AccelerometerFeatures.magnitude(input,y,z);
        assertArrayEquals(result, emptyresult,1e-9);


    }

    @Test
    public void testCrossing() throws Exception {
        DataPoint[] input = new DataPoint[10]; //{0,1,4,9,16,25,36,49,64,81}
        input[0] = new DataPoint(1,0);
        input[1] = new DataPoint(3,1);
        input[2] = new DataPoint(4,2);
        input[3] = new DataPoint(-1,3);
        input[4] = new DataPoint(-4,4);
        input[5] = new DataPoint(7,5);
        input[6] = new DataPoint(3,6);
        input[7] = new DataPoint(1,7);
        input[8] = new DataPoint(3,8);
        input[9] = new DataPoint(9,9);

        double mean = 2.0;

        double[] result = AccelerometerFeatures.crossing(input,mean);

        double[] correctResult = {1,3,5,7,8};
        assertArrayEquals(result, correctResult,1e-9);

        result = AccelerometerFeatures.crossing(input,-100);
        double[] emptyresult = {};
        assertArrayEquals(result, emptyresult,1e-9);

        input = new DataPoint[0];
        result = AccelerometerFeatures.crossing(input,mean);
        assertArrayEquals(result, emptyresult,1e-9);


    }

    @Test
    public void testDiff() throws Exception {
        DataPoint[] input = new DataPoint[10]; //{0,1,4,9,16,25,36,49,64,81}
        for(int i=0; i<input.length; i++) {
            input[i] = new DataPoint(i*i,i);
        }

        double[] result = AccelerometerFeatures.diff(input);

        double[] correctResult = {1,3,5,7,9,11,13,15,17};
        assertArrayEquals(result, correctResult,1e-9);

        DataPoint[] zeroinput = new DataPoint[0];
        result = AccelerometerFeatures.diff(zeroinput);
        assertTrue(result.length == 0);

    }
}