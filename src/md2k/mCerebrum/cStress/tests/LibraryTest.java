package md2k.mCerebrum.cStress.tests;

import md2k.mCerebrum.cStress.Library;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.MaxMin;
import org.junit.After;
import org.junit.Before;
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
public class LibraryTest {


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

//    @Test
//    public void testComputeEnergy() throws Exception {
//
//    }

    @Test
    public void testNextPower2() throws Exception {
        assertTrue(Library.nextPower2(0)==1);
        assertTrue(Library.nextPower2(1)==1);
        assertTrue(Library.nextPower2(200)==256);
        assertTrue(Library.nextPower2(255)==256);
        assertTrue(Library.nextPower2(1000)==1024);
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
        double[] result = Library.magnitude(x, y, z);

        double[] correctResult = {0, 1.73205080756888, 9.16515138991168, 28.6181760425084, 66.0908465674332};
        assertArrayEquals(result,correctResult,1e-3);

        DataPoint[] input = new DataPoint[0];
        double[] emptyresult = {};
        result = Library.magnitude(input, y, z);
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

        double[] result = Library.crossing(input, mean);

        double[] correctResult = {1,3,5,7,8};
        assertArrayEquals(result, correctResult,1e-9);

        result = Library.crossing(input, -100);
        double[] emptyresult = {};
        assertArrayEquals(result, emptyresult,1e-9);

        input = new DataPoint[0];
        result = Library.crossing(input, mean);
        assertArrayEquals(result, emptyresult,1e-9);
    }

    @Test
    public void testDiff() throws Exception {
        DataPoint[] input = new DataPoint[10];
        for(int i=0; i<input.length; i++) {
            input[i] = new DataPoint(i*i,i);
        }

        double[] result = Library.diff(input);

        double[] correctResult = {1,3,5,7,9,11,13,15,17};
        assertArrayEquals(result, correctResult,1e-9);

        DataPoint[] zeroinput = new DataPoint[0];
        result = Library.diff(zeroinput);
        assertTrue(result.length == 0);
    }

    @Test
    public void testApplyFilterNormalize() throws Exception {
        double[] data = new double[]{
                0.2077, 0.3012, 0.4709, 0.2305, 0.8443, 0.1948, 0.2259, 0.1707, 0.2277, 0.4357,
                0.3111, 0.9234, 0.4302, 0.1848, 0.9049, 0.9797, 0.4389, 0.1111, 0.2581, 0.4087,
                0.5949, 0.2622, 0.6028, 0.7112, 0.2217, 0.1174, 0.2967, 0.3188, 0.4242, 0.5079,
                0.0855, 0.2625, 0.8010, 0.0292, 0.9289, 0.7303, 0.4886, 0.5785, 0.2373, 2.4588,
                2.9631, 2.5468, 2.5211, 2.2316, 2.4889, 2.6241, 2.6791, 2.3955, 2.3674, 2.9880,
                2.0377, 2.8852, 2.9133, 2.7962, 2.0987, 2.2619, 2.3354, 2.6797, 2.1366, 2.7212,
                0.1068, 0.6538, 0.4942, 0.7791, 0.7150, 0.9037, 0.8909, 0.3342, 0.6987, 0.1978,
                0.0305, 0.7441, 0.5000, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.5005, 0.4711,
                0.0596, 0.6820, 0.0424, 0.0714, 0.5216, 0.0967, 0.8181, 0.8175, 0.7224, 0.1499};

        double[] filter = new double[]{0,0,0,0,1,1,1,1,1,0,0,0,0};

        double[] correctResult = new double[]{
                0.0784, 0.0968, 0.1643, 0.1633, 0.1572, 0.1332, 0.1330, 0.1003, 0.1096, 0.1654,
                0.1862, 0.1827, 0.2202, 0.2737, 0.2350, 0.2094, 0.2153, 0.1756, 0.1449, 0.1307,
                0.1701, 0.2063, 0.1913, 0.1532, 0.1559, 0.1332, 0.1102, 0.1331, 0.1306, 0.1278,
                0.1664, 0.1348, 0.1685, 0.2200, 0.2381, 0.2203, 0.2370, 0.3593, 0.5378, 0.7024,
                0.8577, 1.0172, 1.0196, 0.9925, 1.0031, 0.9930, 1.0039, 1.0438, 0.9969, 1.0134,
                1.0548, 1.0891, 1.0180, 1.0359, 0.9919, 0.9733, 0.9205, 0.9703, 0.7980, 0.6635,
                0.4888, 0.3802, 0.2198, 0.2835, 0.3025, 0.2897, 0.2833, 0.2419, 0.1721, 0.1603,
                0.1736, 0.1561, 0.2126, 0.2590, 0.2489, 0.2776, 0.3036, 0.2774, 0.2433, 0.2131,
                0.2152, 0.1531, 0.1462, 0.1450, 0.2040, 0.1901, 0.2279, 0.2264, 0.2177, 0.1940,
                0.1404, 0.1061, 0.1101, 0.1131, 0.1240, 0.1859, 0.2380, 0.2083, 0.2005, 0.1351};
            
        double[] result = Library.applyFilterNormalize(data,filter,90);
        assertArrayEquals(correctResult,result,1e-4);
    }

    @Test
    public void testApplySquareFilterNormalize() throws Exception {
        double[] data = new double[]{
                0.2077, 0.3012, 0.4709, 0.2305, 0.8443, 0.1948, 0.2259, 0.1707, 0.2277, 0.4357,
                0.3111, 0.9234, 0.4302, 0.1848, 0.9049, 0.9797, 0.4389, 0.1111, 0.2581, 0.4087,
                0.5949, 0.2622, 0.6028, 0.7112, 0.2217, 0.1174, 0.2967, 0.3188, 0.4242, 0.5079,
                0.0855, 0.2625, 0.8010, 0.0292, 0.9289, 0.7303, 0.4886, 0.5785, 0.2373, 2.4588,
                2.9631, 2.5468, 2.5211, 2.2316, 2.4889, 2.6241, 2.6791, 2.3955, 2.3674, 2.9880,
                2.0377, 2.8852, 2.9133, 2.7962, 2.0987, 2.2619, 2.3354, 2.6797, 2.1366, 2.7212,
                0.1068, 0.6538, 0.4942, 0.7791, 0.7150, 0.9037, 0.8909, 0.3342, 0.6987, 0.1978,
                0.0305, 0.7441, 0.5000, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.5005, 0.4711,
                0.0596, 0.6820, 0.0424, 0.0714, 0.5216, 0.0967, 0.8181, 0.8175, 0.7224, 0.1499};

        double[] correctResult = new double[]{
                0.0067, 0.0141, 0.0345, 0.0083, 0.1110, 0.0059, 0.0079, 0.0045, 0.0081, 0.0296, 0.0151, 0.1328, 0.0288, 0.0053, 0.1275, 0.1495, 0.0300, 0.0019, 0.0104, 0.0260, 0.0551, 0.0107, 0.0566, 0.0788, 0.0077, 0.0021, 0.0137, 0.0158, 0.0280, 0.0402, 0.0011, 0.0107, 0.0999, 0.0001, 0.1344, 0.0831, 0.0372, 0.0521, 0.0088, 0.9416, 1.3673, 1.0101, 0.9899, 0.7756, 0.9647, 1.0723, 1.1178, 0.8937, 0.8729, 1.3904, 0.6467, 1.2964, 1.3218, 1.2176, 0.6859, 0.7967, 0.8494, 1.1183, 0.7109, 1.1532, 0.0018, 0.0666, 0.0380, 0.0945, 0.0796, 0.1272, 0.1236, 0.0174, 0.0760, 0.0061, 0.0001, 0.0862, 0.0389, 0.0359, 0.1275, 0.0579, 0.0594, 0.1150, 0.1010, 0.0518, 0.0052, 0.0090, 0.1224, 0.0001, 0.0374, 0.0044, 0.1492, 0.0791, 0.0390, 0.0346, 0.0006, 0.0724, 0.0003, 0.0008, 0.0424, 0.0015, 0.1042, 0.1041, 0.0813, 0.0035};

        double[] result = Library.applySquareFilterNormalize(data,90);
        assertArrayEquals(correctResult,result,1e-4);
    }

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
        //Currently has a hard-coded filter output
    }

    @Test
    public void testConv() throws Exception {
        double[] signal = {1,2,3,2,1};
        double[] kernel = {1,0,1};
        double[] trueAnswer = {2,4,4,4,2};

        double[] result = Library.conv(signal, kernel);

        assertArrayEquals(result,trueAnswer,1e-3);
    }

    @Test
    public void testLocalMaxMin() throws Exception {
        double[] data = new double[]{
                0.2077, 0.3012, 0.4709, 0.2305, 0.8443, 0.1948, 0.2259, 0.1707, 0.2277, 0.4357,
                0.3111, 0.9234, 0.4302, 0.1848, 0.9049, 0.9797, 0.4389, 0.1111, 0.2581, 0.4087,
                0.5949, 0.2622, 0.6028, 0.7112, 0.2217, 0.1174, 0.2967, 0.3188, 0.4242, 0.5079,
                0.0855, 0.2625, 0.8010, 0.0292, 0.9289, 0.7303, 0.4886, 0.5785, 0.2373, 2.4588,
                2.9631, 2.5468, 2.5211, 2.2316, 2.4889, 2.6241, 2.6791, 2.3955, 2.3674, 2.9880,
                2.0377, 2.8852, 2.9133, 2.7962, 2.0987, 2.2619, 2.3354, 2.6797, 2.1366, 2.7212,
                0.1068, 0.6538, 0.4942, 0.7791, 0.7150, 0.9037, 0.8909, 0.3342, 0.6987, 0.1978,
                0.0305, 0.7441, 0.5000, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.5005, 0.4711,
                0.0596, 0.6820, 0.0424, 0.0714, 0.5216, 0.0967, 0.8181, 0.8175, 0.7224, 0.1499};
        DataPoint[] dpInput = new DataPoint[data.length];
        for(int i=0; i<dpInput.length; i++) {
            dpInput[i] = new DataPoint(data[i],i);
        }

        MaxMin correctResult = new MaxMin();
        correctResult.maxtab = new DataPoint[1];
        correctResult.mintab = new DataPoint[1];
        correctResult.maxtab[0] = new DataPoint(2.9880, 49);
        //correctResult.mintab[0] = new DataPoint(2.9880, 49); //Unknown result here


        MaxMin result = Library.localMaxMin(dpInput, 1); //TODO: Debugging yields a failed result right now.

        //TODO: Complete assertions here
    }

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

    @Test
    public void testWindow() throws Exception {

    }

    @Test
    public void testDetect_outlier_v2() throws Exception {

    }

    @Test
    public void testDetect_Rpeak() throws Exception {

    }

    @Test
    public void testRr_ave_update() throws Exception {

    }

    @Test
    public void testLomb() throws Exception {

    }

    @Test
    public void testHeartRateLFHF() throws Exception {

    }

    @Test
    public void testHeartRatePower() throws Exception {

    }

    @Test
    public void testPeakvalley_v2() throws Exception {

    }

    @Test
    public void testMac() throws Exception {

    }

    @Test
    public void testInterceptOutlierDetectorRIPLamia() throws Exception {

    }
}