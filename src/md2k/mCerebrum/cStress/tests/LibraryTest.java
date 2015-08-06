package md2k.mCerebrum.cStress.tests;

import md2k.mCerebrum.cStress.Library;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Lomb;
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
        assertTrue(Library.nextPower2(20)==32);
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

        result = Library.crossing(input, -10);
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
                0.0305, 0.7441, 0.50, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.505, 0.4711,
                0.0596, 0.6820, 0.0424, 0.0714, 0.5216, 0.0967, 0.8181, 0.8175, 0.7224, 0.1499};

        double[] filter = new double[]{0,0,0,0,1,1,1,1,1,0,0,0,0};

        double[] correctResult = new double[]{
                0.0784, 0.0968, 0.1643, 0.1633, 0.1572, 0.1332, 0.1330, 0.103, 0.1096, 0.1654,
                0.1862, 0.1827, 0.2202, 0.2737, 0.2350, 0.2094, 0.2153, 0.1756, 0.1449, 0.1307,
                0.1701, 0.2063, 0.1913, 0.1532, 0.1559, 0.1332, 0.1102, 0.1331, 0.1306, 0.1278,
                0.1664, 0.1348, 0.1685, 0.220, 0.2381, 0.2203, 0.2370, 0.3593, 0.5378, 0.7024,
                0.8577, 1.0172, 1.0196, 0.9925, 1.031, 0.9930, 1.039, 1.0438, 0.9969, 1.0134,
                1.0548, 1.0891, 1.0180, 1.0359, 0.9919, 0.9733, 0.9205, 0.9703, 0.7980, 0.6635,
                0.4888, 0.3802, 0.2198, 0.2835, 0.3025, 0.2897, 0.2833, 0.2419, 0.1721, 0.1603,
                0.1736, 0.1561, 0.2126, 0.2590, 0.2489, 0.2776, 0.3036, 0.2774, 0.2433, 0.2131,
                0.2152, 0.1531, 0.1462, 0.1450, 0.2040, 0.1901, 0.2279, 0.2264, 0.2177, 0.1940,
                0.1404, 0.1061, 0.1101, 0.1131, 0.1240, 0.1859, 0.2380, 0.2083, 0.205, 0.1351};
            
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
                0.0305, 0.7441, 0.50, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.505, 0.4711,
                0.0596, 0.6820, 0.0424, 0.0714, 0.5216, 0.0967, 0.8181, 0.8175, 0.7224, 0.1499};

        double[] correctResult = new double[]{
                0.067, 0.0141, 0.0345, 0.083, 0.1110, 0.059, 0.079, 0.045, 0.081, 0.0296,
                0.0151, 0.1328, 0.0288, 0.053, 0.1275, 0.1495, 0.030, 0.019, 0.0104, 0.0260,
                0.0551, 0.0107, 0.0566, 0.0788, 0.077, 0.021, 0.0137, 0.0158, 0.0280, 0.0402,
                0.011, 0.0107, 0.0999, 0.01, 0.1344, 0.0831, 0.0372, 0.0521, 0.088, 0.9416,
                1.3673, 1.0101, 0.9899, 0.7756, 0.9647, 1.0723, 1.1178, 0.8937, 0.8729, 1.3904,
                0.6467, 1.2964, 1.3218, 1.2176, 0.6859, 0.7967, 0.8494, 1.1183, 0.7109, 1.1532,
                0.018, 0.0666, 0.0380, 0.0945, 0.0796, 0.1272, 0.1236, 0.0174, 0.0760, 0.061,
                0.01, 0.0862, 0.0389, 0.0359, 0.1275, 0.0579, 0.0594, 0.1150, 0.1010, 0.0518,
                0.052, 0.090, 0.1224, 0.01, 0.0374, 0.044, 0.1492, 0.0791, 0.0390, 0.0346,
                0.06, 0.0724, 0.03, 0.08, 0.0424, 0.015, 0.1042, 0.1041, 0.0813, 0.035};

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
        trueBlackman10 = new double[]{ 0, 0.0509, 0.2580, 0.630, 0.9511, 0.9511, 0.630, 0.2580, 0.0509, 0};

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
                0.0305, 0.7441, 0.50, 0.4799, 0.9047, 0.6099, 0.6177, 0.8594, 0.8055, 0.5767,
                0.1829, 0.2399, 0.8865, 0.0287, 0.4899, 0.1679, 0.9787, 0.7127, 0.505, 0.4711,
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
        double[] correctResult = {1.0, 2.0, 3.0, 3.60, 3.80, 3.60, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -3.0, -3.60, -3.80, -3.60, -3.0, -2.0, -0.60, 1.0, 2.0, 2.20, 1.60, 0, -1.60, -2.20, -2.6667, 0};
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
        double[] inputdata = new double[]{
                -0.917924476813228, -0.917924476813228, -0.753862625145271, -0.589800773477313, -0.753862625145271,
                -0.917924476813228, -0.917924476813228, -1.08198632848119, -1.08198632848119, -1.08198632848119,
                -0.917924476813228, -0.917924476813228, -0.753862625145271, -0.261677070141398, 0.230508484862474,
                -0.261677070141398, 0.722694039866347, 0.0664466331945169, 0.0664466331945169, -1.24604818014914,
                -0.589800773477313, -0.425738921809356, -0.753862625145271, -0.917924476813228, -0.917924476813228,
                -1.08198632848119, -1.08198632848119, -0.917924476813228, -1.08198632848119, -1.08198632848119,
                -0.753862625145271, -0.917924476813228, -0.753862625145271, -0.753862625145271, -0.753862625145271,
                -0.753862625145271, -0.917924476813228, -0.917924476813228, -1.08198632848119, -0.917924476813228,
                -0.917924476813228, -0.753862625145271, -0.753862625145271, -0.589800773477313, -0.917924476813228,
                -0.589800773477313, -0.753862625145271, -0.589800773477313, -0.589800773477313, -0.753862625145271,
                -0.753862625145271, -0.753862625145271, -0.753862625145271, -0.753862625145271, -0.753862625145271,
                -0.753862625145271, -0.753862625145271, -0.917924476813228, -1.08198632848119, -0.917924476813228,
                -0.261677070141398, -0.0976152184734407, -0.0976152184734407, 0.230508484862474, 0.230508484862474,
                -0.0976152184734407, -0.261677070141398, -0.589800773477313, -0.753862625145271, -0.917924476813228,
                -1.08198632848119, -0.753862625145271, -0.425738921809356, 0.394570336530432, 0.230508484862474,
                0.394570336530432, 1.05081774320226, 0.0664466331945169, -0.0976152184734407, -1.08198632848119,
                0.230508484862474, 4.33205477656141, -0.917924476813228, -0.917924476813228, -0.917924476813228,
                -0.917924476813228, -0.753862625145271, -0.753862625145271, -0.425738921809356, -0.0976152184734407,
                0.394570336530432, -0.0976152184734407, -0.425738921809356};

        DataPoint[] input = new DataPoint[inputdata.length];
        for(int i=0; i<input.length; i++) {
            input[i] = new DataPoint(inputdata[i],i+1);
        }

        Lomb result = Library.lomb(input);

        double[] correctResultP = new double[] {
                4.67921414136144, 4.9918173202647, 5.82786784021648, 5.63086219140335, 3.85843656513102,
                3.25177724990639, 2.74571579293217, 1.24010595520756, 0.126104480324749, 0.31854088965193,
                1.61384437636993, 2.32314673318736, 1.79114111736095, 0.81226822234912, 0.412459487127602,
                1.2357994941312, 2.38377588999756, 2.66772187257514, 1.57229344747761, 0.209501539515911,
                0.360120787556712, 2.45809323474862, 4.96214245158792, 5.5209523594057, 4.01914823613531,
                1.78549061486487, 0.547517383550135, 0.83356503645311, 1.55070110008783, 1.61818357297738,
                0.858548836516559, 0.152383833216177, 0.0359116563895136, 0.128192201699579, 0.0301443433090243,
                0.122614378340868, 0.991273688890313, 2.1752550164769, 2.55640544027612, 1.79371783214765,
                0.811483013815188, 0.879173994973389, 1.74313692641656, 2.31941846950287, 1.95082811049457,
                1.1901228989317, 0.785668669533728, 0.792934991911251, 0.964206575885427, 1.07510877449472,
                1.10559542184177, 0.942348203623467, 0.750512862844883, 0.809116854118117, 1.24402984968616,
                1.64913048666162, 1.49168802138273, 0.835115271493764, 0.243840468844885, 0.239109953831388,
                0.655424873061819, 0.920946316154778, 0.724736310035187, 0.337567411491088, 0.24377978778768,
                0.502578814607277, 0.796746131356234, 0.841102058028839, 0.74460209423957, 0.693262484147578,
                0.693274097258448, 0.740674417806035, 0.948356814463732, 1.39131139981509, 1.6556031735549,
                1.38495169346124, 0.775829923211945, 0.568523102936685, 1.04335785236737, 1.59144503258897,
                1.51838304128271, 0.929130383451798, 0.635354320914837, 0.988525381309064, 1.62498222250744,
                1.90332036343325, 1.73271727847712, 1.35984760778451, 1.04205442941426, 0.934515714784768,
                1.02096510871049, 1.21766546522226, 1.20228073473993, 0.852986889512837, 0.403786097131796,
                0.283095183705155, 0.586808784966944, 0.887128011975811, 0.77760396379931, 0.346839258649854,
                0.148317219843383, 0.460827513946637, 0.965270873457532, 1.09419071466221, 0.729270267558226,
                0.326546044321516, 0.302039953493521, 0.626885853423103, 0.88212652609271, 0.825450431231665,
                0.53380676099569, 0.277637081059907, 0.235886033261577, 0.383269250829952, 0.566844983767095,
                0.601255514597881, 0.460498960812623, 0.272825118910204, 0.270776828052624, 0.483953496437506,
                0.751873599846752, 0.859637159962459, 0.809939956181541, 0.72147482826175, 0.640762379014475,
                0.580115607599051, 0.527138081882833, 0.567045569147096, 0.649599929629271, 0.67535828560067,
                0.563051892142837, 0.426509955375219, 0.444202218631274, 0.62061386657057, 0.800872478371616,
                0.77618918582282, 0.591425250468233, 0.399748105098098, 0.396393780601866, 0.590448076991075,
                0.836207847946407, 0.944252994364163, 0.829988357666191, 0.693517159501687, 0.692322890432624,
                0.878586973475927, 0.984408903826885, 0.80342155622685, 0.469623895487054, 0.327574012792455,
                0.60372097063119, 0.991024903546646, 1.08812835189268, 0.733130956592615, 0.340437861822605,
                0.310671864145908, 0.608463471451749, 0.856421850178353, 0.74230311634726, 0.420804121965433,
                0.151673589289348, 0.181138173571597, 0.434363254069844, 0.687973625809582, 0.675747579786264,
                0.399611975331429, 0.138238122569294, 0.119232961729591, 0.318697971915615, 0.419682309304213,
                0.285849051349879, 0.0710438414749163, 0.134260843032924, 0.470081811162584, 0.783366102800891,
                0.680815755357774, 0.288768938291217, 0.0247809026203046, 0.0792887566553566, 0.337032773858422,
                0.41451115768105, 0.399459379483128, 0.385932427703259, 1.55802947559688, 0.38593242770324,
                0.399459379483129};

        double[] correctResultF = new double[] {
                0.00271739130434783, 0.00543478260869565, 0.00815217391304348, 0.0108695652173913, 0.0135869565217391,
                0.016304347826087, 0.0190217391304348, 0.0217391304347826, 0.0244565217391304, 0.0271739130434783,
                0.0298913043478261, 0.0326086956521739, 0.0353260869565217, 0.0380434782608696, 0.0407608695652174,
                0.0434782608695652, 0.046195652173913, 0.0489130434782609, 0.0516304347826087, 0.0543478260869565,
                0.0570652173913043, 0.0597826086956522, 0.0625, 0.0652173913043478, 0.0679347826086956,
                0.0706521739130435, 0.0733695652173913, 0.0760869565217391, 0.078804347826087, 0.0815217391304348,
                0.0842391304347826, 0.0869565217391304, 0.0896739130434783, 0.0923913043478261, 0.0951086956521739,
                0.0978260869565217, 0.10054347826087, 0.103260869565217, 0.105978260869565, 0.108695652173913,
                0.111413043478261, 0.114130434782609, 0.116847826086957, 0.119565217391304, 0.122282608695652,
                0.125, 0.127717391304348, 0.130434782608696, 0.133152173913043, 0.135869565217391, 0.138586956521739,
                0.141304347826087, 0.144021739130435, 0.146739130434783, 0.14945652173913, 0.152173913043478,
                0.154891304347826, 0.157608695652174, 0.160326086956522, 0.16304347826087, 0.165760869565217,
                0.168478260869565, 0.171195652173913, 0.173913043478261, 0.176630434782609, 0.179347826086957,
                0.182065217391304, 0.184782608695652, 0.1875, 0.190217391304348, 0.192934782608696, 0.195652173913043,
                0.198369565217391, 0.201086956521739, 0.203804347826087, 0.206521739130435, 0.209239130434783,
                0.21195652173913, 0.214673913043478, 0.217391304347826, 0.220108695652174, 0.222826086956522,
                0.22554347826087, 0.228260869565217, 0.230978260869565, 0.233695652173913, 0.236413043478261,
                0.239130434782609, 0.241847826086957, 0.244565217391304, 0.247282608695652, 0.25, 0.252717391304348,
                0.255434782608696, 0.258152173913043, 0.260869565217391, 0.263586956521739, 0.266304347826087,
                0.269021739130435, 0.271739130434783, 0.27445652173913, 0.277173913043478, 0.279891304347826,
                0.282608695652174, 0.285326086956522, 0.28804347826087, 0.290760869565217, 0.293478260869565,
                0.296195652173913, 0.298913043478261, 0.301630434782609, 0.304347826086957, 0.307065217391304,
                0.309782608695652, 0.3125, 0.315217391304348, 0.317934782608696, 0.320652173913043, 0.323369565217391,
                0.326086956521739, 0.328804347826087, 0.331521739130435, 0.334239130434783, 0.33695652173913,
                0.339673913043478, 0.342391304347826, 0.345108695652174, 0.347826086956522, 0.35054347826087,
                0.353260869565217, 0.355978260869565, 0.358695652173913, 0.361413043478261, 0.364130434782609,
                0.366847826086957, 0.369565217391304, 0.372282608695652, 0.375, 0.377717391304348, 0.380434782608696,
                0.383152173913043, 0.385869565217391, 0.388586956521739, 0.391304347826087, 0.394021739130435,
                0.396739130434783, 0.39945652173913, 0.402173913043478, 0.404891304347826, 0.407608695652174,
                0.410326086956522, 0.41304347826087, 0.415760869565217, 0.418478260869565, 0.421195652173913,
                0.423913043478261, 0.426630434782609, 0.429347826086957, 0.432065217391304, 0.434782608695652,
                0.4375, 0.440217391304348, 0.442934782608696, 0.445652173913043, 0.448369565217391, 0.451086956521739,
                0.453804347826087, 0.456521739130435, 0.459239130434783, 0.46195652173913, 0.464673913043478,
                0.467391304347826, 0.470108695652174, 0.472826086956522, 0.47554347826087, 0.478260869565217,
                0.480978260869565, 0.483695652173913, 0.486413043478261, 0.489130434782609, 0.491847826086957,
                0.494565217391304, 0.497282608695652, 0.5, 0.502717391304348, 0.505434782608696};


        assertArrayEquals(correctResultF,result.f,1e-4);

        assertArrayEquals(correctResultP,result.P,1e-4);


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