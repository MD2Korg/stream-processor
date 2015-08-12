package md2k.mCerebrum.cStress.Library;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.SensorConfiguration;
import md2k.mCerebrum.cStress.Structs.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Rummana Bari <rbari@memphis.edu>
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
public class Core {

    /**
     * Compute the signal energy using FFTs
     * @param data
     * @param inc
     * @return
     */
    public static double computeEnergy(double[] data, int inc) {
        double result = 0;

        int NFFT = nextPower2(data.length);

        double[] buffer = new double[NFFT];
        System.arraycopy(data, 0, buffer, 0, data.length);

        FastFourierTransformer f = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftC = f.transform(buffer, TransformType.FORWARD);

        for (Complex aFftC : fftC) {
            result += (aFftC.abs() / inc) * (aFftC.abs() / inc);
        }
        return result;
    }

    /**
     * Determine the next power of 2 larger than length
     * @param length
     * @return
     */
    public static int nextPower2(int length) {
        if(length == 0)
            return 0;
        else
            return (int)Math.ceil(Math.log(length)/Math.log(2));
    }

    /**
     * Convert inputs to a magnitude vector
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[] magnitude(DataPoint[] x, DataPoint[] y, DataPoint[] z) {
        double[] result = new double[Math.min(Math.min(x.length, y.length), z.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.sqrt(Math.pow(x[i].value, 2) + Math.pow(y[i].value, 2) + Math.pow(z[i].value, 2));
        }
        return result;
    }

    /**
     * Mean crossing algorithm
     * @param x
     * @param mean
     * @return
     */
    public static double[] crossing(DataPoint[] x, double mean) {
        ArrayList<Double> crossings = new ArrayList<Double>();

        for (int i = 0; i < x.length - 1; i++) {
            if ((x[i].value > mean && x[i + 1].value <= mean) || x[i].value < mean && x[i + 1].value >= mean) {
                crossings.add((double) (i + 1));
            }
        }
        double[] result = new double[crossings.size()];
        for (int i = 0; i < crossings.size(); i++) {
            result[i] = crossings.get(i);
        }
        return result;
    }

    /**
     * Compute a discrete derivative
     * @param dp
     * @return
     */
    public static double[] diff(DataPoint[] dp) {
        double[] result;
        if (dp.length == 0) {
            result = new double[0];
        } else {
            result = new double[dp.length - 1];

            for (int i = 1; i < dp.length; i++) {
                result[i - 1] = dp[i].value - dp[i - 1].value;
            }
        }
        return result;
    }

    /**
     * Apply a filter and normalization to the sample
     * @param sample
     * @param filter
     * @param normalizePercentile
     * @return
     */
    public static double[] applyFilterNormalize(double[] sample, double[] filter, int normalizePercentile) {
        double[] result = conv(sample, filter);
        DescriptiveStatistics statsY2 = new DescriptiveStatistics();
        for (double d : result) {
            statsY2.addValue(d);
        }
        for (int i = 0; i < result.length; i++) {
            result[i] /= statsY2.getPercentile(normalizePercentile);
        }

        return result;
    }

    /**
     * Square the sample and normalization
     * @param sample
     * @param normalizePercentile
     * @return
     */
    public static double[] applySquareFilterNormalize(double[] sample, int normalizePercentile) {
        double[] result = new double[sample.length];
        DescriptiveStatistics statsY2 = new DescriptiveStatistics();
        for (double d : sample) {
            statsY2.addValue(d*d);
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = statsY2.getElement(i) / statsY2.getPercentile(normalizePercentile); //TODO: getPercentile returns different values than Matlab's implementation
        }

        return result;
    }

    /**
     * Standard implementation of the Blackman filter
     * @param window_l Window length of the Blackman filter
     * @return blackman filter
     */
    public static double[] blackman(int window_l) {
        double[] result = new double[window_l];
        int M = (int) Math.floor((window_l + 1) / 2);

        for (int i = 0; i < M; i++) {
            result[i] = 0.42 - 0.5 * Math.cos(2.0 * Math.PI * (double) i / (window_l - 1)) + 0.08 * Math.cos(4.0 * Math.PI * (double) i / (window_l - 1)); //These constants are specific to the Blackman filter
            result[window_l - i - 1] = result[i];
        }

        return result;
    }

    /**
     * Finite Impulse Response Least-Squares filter
     * This filter is hard-coded from a Matlab output based on the frequency of the ECG sensor
     * @param fl Not used currently
     * @param f Not used currently
     * @param a Not used currently
     * @param w Not used currently
     * @return Implemented filter
     */
    public static double[] firls(double fl, double[] f, double[] a, double[] w) {
        //Hardcoded to the specifications of the Autosense ECG sensor.  Determined from the Matlab code

        double[] result = {
                -4.7274160329724543e-04, -4.3910892627884660e-04, 8.6902448327532778e-04, 1.2580375981368801e-03,
                -7.0870338859324122e-05, -3.1826848647111390e-04, 2.6535857915570944e-04, -8.1833969244558271e-04,
                -1.4124933569042541e-03, -1.9995228246965813e-05, 6.7313359750941992e-05, -6.0349375063082288e-04,
                8.6714439266190248e-04, 1.5065160936261314e-03, 1.0310266272463235e-04, 5.4715012534279738e-04,
                1.1904732453105013e-03, -7.6681365348830479e-04, -1.2037758114175129e-03, 3.1547725670266574e-05,
                -1.2630183530614810e-03, -1.7863067743220608e-03, 4.5997532952314775e-04, 3.2716936532409155e-04,
                -5.2644177116152754e-04, 1.7385569813462844e-03, 1.9889763081328412e-03, -1.0568777171727037e-04,
                1.0085738914257093e-03, 1.3166081526795959e-03, -1.6828106527494776e-03, -1.3954395354213642e-03,
                3.6314731240927499e-06, -2.3580287405187803e-03, -2.0329727875177984e-03, 1.0570699668543437e-03,
                -1.2914364939980619e-04, -3.8546902662939887e-04, 3.1026282496662044e-03, 2.0891652801290394e-03,
                -1.7040978539860152e-04, 2.2345403662638666e-03, 1.1662141454746484e-03, -2.7739626598314152e-03,
                -9.8117447601674384e-04, -4.4741116462223640e-04, -4.0970300710684790e-03, -1.8325183286848025e-03,
                1.3776445217444490e-03, -1.3211583879184985e-03, 3.7867414252479948e-04, 4.7380839079786659e-03,
                1.6152763281382203e-03, 5.0172009801508800e-04, 4.1276888849617836e-03, 3.2171309686397854e-04,
                -3.5444229001001413e-03, 5.9320034125439428e-05, -1.9474610880852858e-03, -6.1508063614239862e-03,
                -9.9665665297590886e-04, 7.1573500042050489e-04, -3.0500439336000633e-03, 2.2375273611317816e-03,
                6.0494339632141611e-03, 6.6666900112815589e-04, 2.6644168389563642e-03, 6.2464301815488002e-03,
                -1.3664646323453468e-03, -3.1859375309960399e-03, 1.3346226939768935e-03, -5.0751244870958813e-03,
                -7.8491706097835925e-03, 2.0337081693415804e-04, -1.8091340945618182e-03, -4.6875184234183237e-03,
                5.3652517463266009e-03, 6.1905855566496198e-03, -7.4368051532226921e-05, 7.0575372722313061e-03,
                7.8206867803311906e-03, -3.4972150936961369e-03, -7.6079548874512096e-04, 1.8835178013748015e-03,
                -1.0170003906003467e-02, -8.3679423125302028e-03, 7.4577361924604336e-04, -7.1106914021255080e-03,
                -5.2165564908720000e-03, 9.4292401382520472e-03, 4.2997116416556890e-03, 8.9934836096324411e-04,
                1.4397066723214469e-02, 7.9962549473134286e-03, -4.8304877652048297e-03, 4.7345958483973353e-03,
                4.9390510587356005e-05, -1.7446053084899240e-02, -7.0564384166495178e-03, -1.6381246987009848e-03,
                -1.6536831781652900e-02, -3.3074012030242165e-03, 1.3580158825803681e-02, -4.6367025497966462e-04,
                6.7704049102291549e-03, 2.6487077400862109e-02, 6.1540352580244520e-03, -2.4533091741271327e-03,
                1.5497475687230644e-02, -7.7410309481643867e-03, -2.8629194004745218e-02, -3.6522550021604841e-03,
                -1.3632609634575103e-02, -3.6030712287501945e-02, 3.8924925851573531e-03, 1.6707802872026386e-02,
                -1.1053287553442563e-02, 3.1002542423973951e-02, 5.7319009934381555e-02, 1.6637598373033171e-03,
                1.8714840588857272e-02, 5.1616797200589280e-02, -4.8886309433549893e-02, -7.3414476566383474e-02,
                4.0083352853056056e-03, -1.4044338414188356e-01, -2.4550216720451279e-01, 1.4662066861996509e-01,
                4.7941126114385940e-01, 1.4662066861996509e-01, -2.4550216720451279e-01, -1.4044338414188356e-01,
                4.0083352853056056e-03, -7.3414476566383474e-02, -4.8886309433549893e-02, 5.1616797200589280e-02,
                1.8714840588857272e-02, 1.6637598373033171e-03, 5.7319009934381555e-02, 3.1002542423973951e-02,
                -1.1053287553442563e-02, 1.6707802872026386e-02, 3.8924925851573531e-03, -3.6030712287501945e-02,
                -1.3632609634575103e-02, -3.6522550021604841e-03, -2.8629194004745218e-02, -7.7410309481643867e-03,
                1.5497475687230644e-02, -2.4533091741271327e-03, 6.1540352580244520e-03, 2.6487077400862109e-02,
                6.7704049102291549e-03, -4.6367025497966462e-04, 1.3580158825803681e-02, -3.3074012030242165e-03,
                -1.6536831781652900e-02, -1.6381246987009848e-03, -7.0564384166495178e-03, -1.7446053084899240e-02,
                4.9390510587356005e-05, 4.7345958483973353e-03, -4.8304877652048297e-03, 7.9962549473134286e-03,
                1.4397066723214469e-02, 8.9934836096324411e-04, 4.2997116416556890e-03, 9.4292401382520472e-03,
                -5.2165564908720000e-03, -7.1106914021255080e-03, 7.4577361924604336e-04, -8.3679423125302028e-03,
                -1.0170003906003467e-02, 1.8835178013748015e-03, -7.6079548874512096e-04, -3.4972150936961369e-03,
                7.8206867803311906e-03, 7.0575372722313061e-03, -7.4368051532226921e-05, 6.1905855566496198e-03,
                5.3652517463266009e-03, -4.6875184234183237e-03, -1.8091340945618182e-03, 2.0337081693415804e-04,
                -7.8491706097835925e-03, -5.0751244870958813e-03, 1.3346226939768935e-03, -3.1859375309960399e-03,
                -1.3664646323453468e-03, 6.2464301815488002e-03, 2.6644168389563642e-03, 6.6666900112815589e-04,
                6.0494339632141611e-03, 2.2375273611317816e-03, -3.0500439336000633e-03, 7.1573500042050489e-04,
                -9.9665665297590886e-04, -6.1508063614239862e-03, -1.9474610880852858e-03, 5.9320034125439428e-05,
                -3.5444229001001413e-03, 3.2171309686397854e-04, 4.1276888849617836e-03, 5.0172009801508800e-04,
                1.6152763281382203e-03, 4.7380839079786659e-03, 3.7867414252479948e-04, -1.3211583879184985e-03,
                1.3776445217444490e-03, -1.8325183286848025e-03, -4.0970300710684790e-03, -4.4741116462223640e-04,
                -9.8117447601674384e-04, -2.7739626598314152e-03, 1.1662141454746484e-03, 2.2345403662638666e-03,
                -1.7040978539860152e-04, 2.0891652801290394e-03, 3.1026282496662044e-03, -3.8546902662939887e-04,
                -1.2914364939980619e-04, 1.0570699668543437e-03, -2.0329727875177984e-03, -2.3580287405187803e-03,
                3.6314731240927499e-06, -1.3954395354213642e-03, -1.6828106527494776e-03, 1.3166081526795959e-03,
                1.0085738914257093e-03, -1.0568777171727037e-04, 1.9889763081328412e-03, 1.7385569813462844e-03,
                -5.2644177116152754e-04, 3.2716936532409155e-04, 4.5997532952314775e-04, -1.7863067743220608e-03,
                -1.2630183530614810e-03, 3.1547725670266574e-05, -1.2037758114175129e-03, -7.6681365348830479e-04,
                1.1904732453105013e-03, 5.4715012534279738e-04, 1.0310266272463235e-04, 1.5065160936261314e-03,
                8.6714439266190248e-04, -6.0349375063082288e-04, 6.7313359750941992e-05, -1.9995228246965813e-05,
                -1.4124933569042541e-03, -8.1833969244558271e-04, 2.6535857915570944e-04, -3.1826848647111390e-04,
                -7.0870338859324122e-05, 1.2580375981368801e-03, 8.6902448327532778e-04, -4.3910892627884660e-04,
                -4.7274160329724543e-04};

        return result;
    }

    /**
     * Standard convolution implementation for producing the "same" size filter
     * @param signal Input signal
     * @param kernel Kernel to apply to the signal
     * @return Convoluted signal
     */
    public static double[] conv(double[] signal, double[] kernel) {
        double[] result = new double[Math.max(Math.max(signal.length + kernel.length, signal.length), kernel.length)];

        double[] tempsignal = new double[signal.length + kernel.length];
        System.arraycopy(signal, 0, tempsignal, kernel.length/2, signal.length); //Zero pad the end of signal

        for (int i = 0; i < signal.length; i++) {
            result[i] = 0;
            for (int j = 0; j < kernel.length; j++) {
                result[i] += tempsignal[i + j] * kernel[j];
            }
        }

        double[] shortresult = new double[signal.length];
        System.arraycopy(result, 0, shortresult, 0, signal.length); //Remove excess array size
        return shortresult;
    }


    /**
     * Reimplementation of Matlab's smooth function
     * @param rip
     * @param n
     * @return
     */
    public static DataPoint[] smooth(DataPoint[] rip, int n) {
        DataPoint[] result = new DataPoint[rip.length];

        int windowSize = 1;
        double sum;
        for(int i=0; i<rip.length; i++) {
            sum = 0.0;
            int startingPoint;
            if( (rip.length-i+1) < n ) {
                startingPoint = rip.length-windowSize;
            } else {
                startingPoint = (int) Math.max(Math.floor(i - n / 2), 0);
            }
            for(int j=startingPoint; j<startingPoint+windowSize; j++) {
                sum += rip[j].value;
            }
            sum /= (double) windowSize;

            result[i] = new DataPoint(sum, rip[i].timestamp);

            if(windowSize < n && (rip.length-i) > n) { //Increase windowSize until n
                windowSize += 2;
            } else if ( (rip.length-i+1) < n) {
                windowSize -= 2;
            }
        }

        return result;
    }

    /**
     * Basic time bounded windowing.
     * @param data
     * @param size
     * @return
     */
    public static ArrayList<DataPoint[]> window(DataPoint[] data, int size) {
        ArrayList<DataPoint[]> result = new ArrayList<DataPoint[]>();

        long startTime = data[0].timestamp;
        ArrayList<DataPoint> tempArray = new ArrayList<DataPoint>();
        DataPoint[] temp;
        for(DataPoint dp: data) {
            if(dp.timestamp < startTime+size) {
                tempArray.add(dp);
            } else {
                temp = new DataPoint[tempArray.size()];
                for(int i=0; i<temp.length; i++) {
                    temp[i] = tempArray.get(i);
                }
                result.add(temp);
                tempArray = new ArrayList<DataPoint>();
                startTime += size;
            }
        }
        temp = new DataPoint[tempArray.size()];
        for(int i=0; i<temp.length; i++) {
            temp[i] = tempArray.get(i);
        }
        result.add(temp);

        return result;
    }

    /**
     * Outlier detection for RR-interval data
     * Reference: detect_outlier_v2.m
     * @param sample RR-interval values
     * @param timestamp RR-interval timestamps
     * @return Outlier array
     */
    public static int[] detect_outlier_v2(double[] sample, long[] timestamp) {
        ArrayList<Integer> outlier = new ArrayList<Integer>();

        if (timestamp.length != 0) {
            ArrayList<Double> valid_rrInterval = new ArrayList<Double>();
            ArrayList<Long> valid_timestamp = new ArrayList<Long>();
            DescriptiveStatistics valid_rrInterval_stats = new DescriptiveStatistics();
            for (int i = 0; i < sample.length; i++) {
                if (sample[i] > 0.3 && sample[i] < 2.0) {
                    valid_rrInterval.add(sample[i]);
                    valid_rrInterval_stats.addValue(sample[i]);
                    valid_timestamp.add(timestamp[i]);
                }
            }
            DescriptiveStatistics diff_rrInterval = new DescriptiveStatistics();
            for (int i = 1; i < valid_rrInterval.size(); i++) {
                diff_rrInterval.addValue(Math.abs(valid_rrInterval.get(i) - valid_rrInterval.get(i - 1)));
            }
            double MED = AUTOSENSE.MED_CONSTANT * 0.5 * (diff_rrInterval.getPercentile(75) - diff_rrInterval.getPercentile(25));
            double MAD = (valid_rrInterval_stats.getPercentile(50) - AUTOSENSE.MAD_CONSTANT * 0.5 * (diff_rrInterval.getPercentile(75) - diff_rrInterval.getPercentile(25))) / 3.0;
            double CBD = (MED + MAD) / 2.0;
            if (CBD < AUTOSENSE.CBD_THRESHOLD) {
                CBD = AUTOSENSE.CBD_THRESHOLD;
            }

            for (double aSample : sample) {
                outlier.add(AUTOSENSE.QUALITY_BAD);
            }
            outlier.set(0, AUTOSENSE.QUALITY_GOOD);
            double standard_rrInterval = valid_rrInterval.get(0);
            boolean prev_beat_bad = false;

            for (int i = 1; i < valid_rrInterval.size() - 1; i++) {
                double ref = valid_rrInterval.get(i);
                if (ref > AUTOSENSE.REF_MINIMUM && ref < AUTOSENSE.REF_MAXIMUM) {
                    double beat_diff_prevGood = Math.abs(standard_rrInterval - valid_rrInterval.get(i));
                    double beat_diff_pre = Math.abs(valid_rrInterval.get(i - 1) - valid_rrInterval.get(i));
                    double beat_diff_post = Math.abs(valid_rrInterval.get(i) - valid_rrInterval.get(i + 1));

                    if ((prev_beat_bad && beat_diff_prevGood < CBD) || (prev_beat_bad && beat_diff_prevGood > CBD && beat_diff_pre <= CBD && beat_diff_post <= CBD)) {
                        for (int j = 0; j < timestamp.length; j++) {
                            if (timestamp[j] == valid_timestamp.get(i)) {
                                outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                            }
                        }
                        prev_beat_bad = false;
                        standard_rrInterval = valid_rrInterval.get(i);
                    } else if (prev_beat_bad && beat_diff_prevGood > CBD && (beat_diff_pre > CBD || beat_diff_post > CBD)) {
                        prev_beat_bad = true;
                    } else if (!prev_beat_bad && beat_diff_pre <= CBD) {
                        for (int j = 0; j < timestamp.length; j++) {
                            if (timestamp[j] == valid_timestamp.get(i)) {
                                outlier.set(j, AUTOSENSE.QUALITY_GOOD);
                            }
                        }
                        prev_beat_bad = false;
                        standard_rrInterval = valid_rrInterval.get(i);
                    } else if (!prev_beat_bad && beat_diff_pre > CBD) {
                        prev_beat_bad = true;
                    }

                }
            }

        }

        int[] result = new int[outlier.size()];
        for (int i = 0; i < outlier.size(); i++) {
            result[i] = outlier.get(i);
        }
        return result;
    }


    /**
     * Basic EWMA function
     * @param x
     * @param y
     * @param alpha
     * @return
     */
    public static double ewma(double x, double y, double alpha) {
        return alpha * x + (1-alpha) * y;
    }

    /**
     * R-peak detector
     * @param datapoints Raw ECG datapoints
     * @param frequency ECG sampling frequency
     * @return Indexes of R-peaks
     */
    public static long[] detect_Rpeak(DataPoint[] datapoints, double frequency) {
        double[] sample = new double[datapoints.length];
        double[] timestamps = new double[datapoints.length];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = datapoints[i].value;
            timestamps[i] = datapoints[i].timestamp;
        }

        int window_l = (int) Math.ceil(frequency / 5.0);

        double thr1 = AUTOSENSE.THR1_INIT;
        double f = 2.0 / frequency;
        double[] F = {0.0, 4.5 * f, 5.0 * f, 20.0 * f, 20.5 * f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0 / 0.02, 1.0 / 0.02, 500 / 0.02};
        double fl = AUTOSENSE.FL_INIT;

        double[] y2 = applyFilterNormalize(sample, firls(fl, F, A, w), 90);
        double[] y3 = applyFilterNormalize(y2, new double[]{-1.0 / 8.0, -2.0 / 8.0, 0.0 / 8.0, 2.0 / 8.0, -1.0 / 8.0}, 90);
        double[] y4 = applySquareFilterNormalize(y3, 90);
        double[] y5 = applyFilterNormalize(y4, blackman(window_l), 90);

        ArrayList<Integer> pkt = new ArrayList<Integer>();
        ArrayList<Double> valuepks = new ArrayList<Double>();
        for (int i = 2; i < y5.length - 2; i++) {
            if (y5[i - 2] < y5[i - 1] && y5[i - 1] < y5[i] && y5[i] >= y5[i + 1] && y5[i + 1] > y5[i + 2]) {
                pkt.add(i);
                valuepks.add(y5[i]);
            }
        }

        double rr_ave = 0.0;
        for (int i = 1; i < pkt.size(); i++) {
            rr_ave += pkt.get(i) - pkt.get(i - 1);
        }
        rr_ave /= (pkt.size() - 1);

        double thr2 = 0.5 * thr1;
        double sig_lev = AUTOSENSE.SIG_LEV_FACTOR * thr1;
        double noise_lev = AUTOSENSE.NOISE_LEV_FACTOR * sig_lev;

        int c1 = 0;
        ArrayList<Integer> c2 = new ArrayList<Integer>();
        int i = 0;
        ArrayList<Integer> Rpeak_temp1 = new ArrayList<Integer>();


        while (i < pkt.size()) {
            if (Rpeak_temp1.size() == 0) {
                if (y5[pkt.get(i)] >= thr1 && y5[pkt.get(i)] < (3.0 * sig_lev)) {
                    if(Rpeak_temp1.size() <= c1) {
                        Rpeak_temp1.add(0);
                    }
                    Rpeak_temp1.set(c1,pkt.get(i));
                    sig_lev = ewma(y5[pkt.get(i)],sig_lev, AUTOSENSE.EWMA_ALPHA);
                    if(c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1,i);
                    c1 += 1;
                } else if (y5[pkt.get(i)] < thr1 && y5[pkt.get(i)] > thr2) {
                    noise_lev = ewma(y5[pkt.get(i)],noise_lev, AUTOSENSE.EWMA_ALPHA);
                }

                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                i += 1;

                rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
            } else {
                if (((pkt.get(i) - pkt.get(c2.get(c1 - 1))) > 1.66 * rr_ave) && (i - c2.get(c1 - 1)) > 1) {
                    ArrayList<Double> searchback_array_inrange = new ArrayList<Double>();
                    ArrayList<Integer> searchback_array_inrange_index = new ArrayList<Integer>();

                    for (int j = c2.get(c1 - 1) + 1; j < i - 1; j++) {
                        if (valuepks.get(i) < 3.0 * sig_lev && valuepks.get(i) > thr2) {
                            searchback_array_inrange.add(valuepks.get(i));
                            searchback_array_inrange_index.add(j - c2.get(c1 - 1));
                        }
                    }

                    if (searchback_array_inrange.size() > 0) {
                        double searchback_max = searchback_array_inrange.get(0);
                        int searchback_max_index = 0;
                        for (int j = 0; j < searchback_array_inrange.size(); j++) {
                            if (searchback_array_inrange.get(j) > searchback_max) {
                                searchback_max = searchback_array_inrange.get(i);
                                searchback_max_index = j;
                            }
                        }
                        if(Rpeak_temp1.size() >= c1) {
                            Rpeak_temp1.add(0);
                        }
                        Rpeak_temp1.set(c1,pkt.get(c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index)));
                        sig_lev = ewma(y5[Rpeak_temp1.get(c1 - 1)], sig_lev, AUTOSENSE.EWMA_ALPHA);
                        if(c1 >= c2.size()) {
                            c2.add(0);
                        }
                        c2.set(c1, c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index));
                        i = c2.get(c1 - 1) + 1;
                        c1 += 1;
                        thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                        thr2 = 0.5 * thr1;
                        rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
                        continue;
                    }
                } else if (y5[pkt.get(i)] >= thr1 && y5[pkt.get(i)] < 3.0 * sig_lev) {
                    if(Rpeak_temp1.size() >= c1) {
                        Rpeak_temp1.add(0);
                    }
                    Rpeak_temp1.set(c1,pkt.get(i));
                    sig_lev = ewma(y5[pkt.get(i)],sig_lev, AUTOSENSE.EWMA_ALPHA);
                    if(c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i);
                    c1 += 1;
                } else if (y5[pkt.get(i)] < thr1 && y5[pkt.get(i)] > thr2) {
                    noise_lev = ewma(y5[pkt.get(i)],noise_lev, AUTOSENSE.EWMA_ALPHA);
                }
                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                i++;
                rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
            }
        }

        boolean difference = false;

        ArrayList<Integer> Rpeak_temp2 = new ArrayList<Integer>();
        for (Integer j : Rpeak_temp1) {
            Rpeak_temp2.add(j);
        }


        while (!difference) {
            int length_Rpeak_temp2 = Rpeak_temp2.size();
            ArrayList<Integer> diffRpeak = new ArrayList<Integer>();
            for(int j=1; j<Rpeak_temp2.size(); j++) {
                diffRpeak.add(Rpeak_temp2.get(j)-Rpeak_temp2.get(j-1));
            }

            ArrayList<Double> comp1 = new ArrayList<Double>();
            ArrayList<Double> comp2 = new ArrayList<Double>();
            ArrayList<Integer> eli_index = new ArrayList<Integer>();

            for(int j=0; j<diffRpeak.size(); j++) {
                if (diffRpeak.get(j) < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER *frequency)) {
                    comp1.add(sample[Rpeak_temp2.get(j)]);
                    comp2.add(sample[Rpeak_temp2.get(j + 1)]);
                    if (comp1.get(comp1.size()-1) < comp2.get(comp2.size()-1)) {
                        eli_index.add(0);
                    } else {
                        eli_index.add(1);
                    }
                } else {
                    eli_index.add(-999999);
                }
            }

            for(int j=0; j<diffRpeak.size(); j++) {
                if (diffRpeak.get(j) < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER*frequency)) {
                    Rpeak_temp2.set(j+eli_index.get(j), -999999);
                }
            }

            for (Iterator<Integer> it=Rpeak_temp2.iterator(); it.hasNext(); ) {
    			if (it.next() == -999999) {
    				it.remove();
    			}
    		}

            difference = length_Rpeak_temp2 == Rpeak_temp2.size();

        }

        ArrayList<Integer> Rpeak_temp3 = new ArrayList<Integer>();
        Rpeak_temp3.add(Rpeak_temp2.get(0));

        for (int k = 1; k < Rpeak_temp2.size(); k++) {
            double maxValue = -1e9;
            int index = 0;
            try {
                for (int j = Rpeak_temp2.get(k) - (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR); j < Rpeak_temp2.get(k) + (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR); j++) {
                    if (sample[j] > maxValue) {
                        maxValue = sample[j];
                        index = j;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Rpeak_temp3.add(index - 1);
            }
        }


        long[] result = new long[Rpeak_temp3.size()];
        for (int k = 0; k < Rpeak_temp3.size(); k++) {
            result[k] = (long) Rpeak_temp3.get(k);
        }

        return result;
    }

    /**
     *
     * @param rpeak_temp1
     * @param rr_ave
     * @return
     */
    public static double rr_ave_update(ArrayList<Integer> rpeak_temp1, double rr_ave) {
        ArrayList<Integer> peak_interval = new ArrayList<Integer>();

        if (rpeak_temp1.size() == 0) {
            return rr_ave;
        } else {
            peak_interval.add(rpeak_temp1.get(0));
            for (int i = 1; i < rpeak_temp1.size(); i++) {
                peak_interval.add(rpeak_temp1.get(i) - rpeak_temp1.get(i - 1));
            }

            if (peak_interval.size() < AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE) {
                return rr_ave;
            } else {
                double result = 0.0;
                for (int i = peak_interval.size() - AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE; i < peak_interval.size(); i++) {
                    result += peak_interval.get(i);
                }
                result /= 8.0;
                return result;
            }
        }
    }

    /**
     * Lomb–Scargle periodogram implementation
     * Reference: HeartRateLomb.m
     * @param dp DataPoint array
     * @return Lomb structure with P and f defined
     */
    public static Lomb lomb(DataPoint[] dp) {
        double T = dp[dp.length - 1].timestamp - dp[0].timestamp;
        int nf = (int) Math.round(0.5 * 4.0 * 1.0 * dp.length);
        double[] f = new double[nf];

        for (int i = 0; i < nf; i++) {
            f[i] = (i + 1) / (T * 4);
        }

        nf = f.length;

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (DataPoint aData : dp) {
            stats.addValue(aData.value);
        }

        double mx = stats.getMean();
        double vx = stats.getVariance();

        for (DataPoint aDp : dp) {
            aDp.value -= mx;
        }

        double[] P = new double[nf];
        double[] wt;
        double[] swt;
        double[] cwt;
        double Ss2wt;
        double Sc2wt;
        double wtau;
        double swtau;
        double cwtau;
        double[] swttau;
        double[] cwttau;
        double swttau2;
        double cwttau2;
        wt = new double[dp.length];
        swt = new double[dp.length];
        cwt = new double[dp.length];
        swttau = new double[swt.length];
        cwttau = new double[swt.length];
        double part1;
        double part2;

        for (int i = 0; i < nf; i++) {
            Ss2wt = 0;
            Sc2wt = 0;

            for (int j = 0; j < wt.length; j++) {
                wt[j] = 2.0 * Math.PI * f[i] * dp[j].timestamp;
                swt[j] = Math.sin(wt[j]);
                cwt[j] = Math.cos(wt[j]);

                Ss2wt += cwt[j] * swt[j];
                Sc2wt += (cwt[j] - swt[j]) * (cwt[j] + swt[j]);

            }
            Ss2wt *= 2;

            wtau = 0.5 * Math.atan2(Ss2wt, Sc2wt);
            swtau = Math.sin(wtau);
            cwtau = Math.cos(wtau);


            swttau2 = 0;
            cwttau2 = 0;

            for (int j = 0; j < swt.length; j++) {
                swttau[j] = swt[j] * cwtau - cwt[j] * swtau;
                cwttau[j] = cwt[j] * cwtau + swt[j] * swtau;

                swttau2 += swttau[j] * swttau[j];
                cwttau2 += cwttau[j] * cwttau[j];
            }


            part1 = 0;
            part2 = 0;
            for (int j = 0; j < cwttau.length; j++) {
                part1 += (dp[j].value * cwttau[j]);
                part2 += (dp[j].value * swttau[j]);
            }
            part1 = part1*part1; //Square result
            part2 = part2*part2; //Square result

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }

    /**
     * Heartrate Low Frequency - High Frequency ratio
     * @param P
     * @param f
     * @param lowRate Low frequency cutoff
     * @param highRate High frequency cutoff
     * @return LF/HF ratio
     */
    public static double heartRateLFHF(double[] P, double[] f, double lowRate, double highRate) {
        double result1 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] < lowRate) {
                result1 += P[i];
            }
        }
        double result2 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowRate && f[i] <= highRate) {
                result2 += P[i];
            }
        }
        return result1 / result2;
    }

    /**
     * Heartrate Power
     * @param P
     * @param f
     * @param lowFrequency Low frequency cutoff
     * @param highFrequency High frequency cutoff
     * @return
     */
    public static double heartRatePower(double[] P, double[] f, double lowFrequency, double highFrequency) {
        double result = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowFrequency && f[i] <= highFrequency) {
                result += P[i];
            }
        }

        return result;
    }

    public static PeakValley peakvalley_v2(DataPoint[] rip, SensorConfiguration sc) {

        DataPoint[] sample = smooth(rip, AUTOSENSE.PEAK_VALLEY_SMOOTHING_SIZE);

        int windowLength = (int) Math.round(AUTOSENSE.WINDOW_LENGTH_SECS * sc.getFrequency("RIP"));

        DataPoint[] MAC = mac(sample, windowLength);

        ArrayList<Integer> upInterceptIndex = new ArrayList<Integer>();
        ArrayList<Integer> downInterceptIndex = new ArrayList<Integer>();

        for (int i = 1; i < MAC.length; i++) {
            if (sample[i - 1].value <= MAC[i - 1].value && sample[i].value > MAC[i].value) {
                upInterceptIndex.add(i - 1);
            } else if (sample[i - 1].value >= MAC[i - 1].value && sample[i].value < MAC[i].value) {
                downInterceptIndex.add(i - 1);
            }
        }

        Intercepts UIDI = InterceptOutlierDetectorRIPLamia(upInterceptIndex, downInterceptIndex, sample, (int) (AUTOSENSE.SAMPLE_LENGTH_SECS*1000), sc);

        int[] UI = UIDI.UI;
        int[] DI = UIDI.DI;

        ArrayList<Integer> peakIndex = new ArrayList<Integer>();
        ArrayList<Integer> valleyIndex = new ArrayList<Integer>();

        for (int i = 0; i < DI.length-1; i++) {

            int peakindex = 0;
            double peakValue = -1e9;
            for (int j = UI[i]; j < DI[i + 1]; j++) {
                if (sample[j].value > peakValue) {
                    peakindex = j;
                    peakValue = sample[j].value;
                }
            }
            peakIndex.add(peakindex);

            DataPoint[] temp = new DataPoint[UI[i] - DI[i]];
            System.arraycopy(sample, DI[i], temp, 0, UI[i] - DI[i]);

            double vlValueMinimum = 1e9;
            int vlValueIndex = -999999;
            for(int j=0; j<temp.length; j++) {
                if(vlValueMinimum > temp[j].value) {
                    vlValueMinimum = temp[j].value;
                    vlValueIndex = j;
                }
            }
            if (vlValueIndex == -999999) {
                continue;
            }
            valleyIndex.add(DI[i] + vlValueIndex - 1);

        }

        double[] inspirationAmplitude = new double[valleyIndex.size()];
        double[] expirationAmplitude;

        double meanInspirationAmplitude = 0.0;
        double meanExpirationAmplitude;

        for (int i = 0; i < valleyIndex.size() - 1; i++) {
            inspirationAmplitude[i] = sample[peakIndex.get(i)].value - sample[valleyIndex.get(i)].value;

            meanInspirationAmplitude += inspirationAmplitude[i];
        }
        meanInspirationAmplitude /= (valleyIndex.size() - 1);

        ArrayList<Integer> finalPeakIndex = new ArrayList<Integer>();
        ArrayList<Integer> finalValleyIndex = new ArrayList<Integer>();

        for (int i = 0; i < inspirationAmplitude.length; i++) {
            if (inspirationAmplitude[i] > AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanInspirationAmplitude) {
                finalPeakIndex.add(peakIndex.get(i));
                finalValleyIndex.add(valleyIndex.get(i));
            }
        }

        PeakValley result = new PeakValley();

        if (finalValleyIndex.size() > 0) {
            expirationAmplitude = new double[finalValleyIndex.size() - 1];
            meanExpirationAmplitude = 0.0;
            for (int i = 0; i < finalValleyIndex.size() - 1; i++) {
                expirationAmplitude[i] = Math.abs(sample[finalValleyIndex.get(i + 1)].value - sample[finalPeakIndex.get(i)].value);
                meanExpirationAmplitude += expirationAmplitude[i];
            }
            meanExpirationAmplitude /= (finalValleyIndex.size() - 1);


            ArrayList<Integer> resultPeakIndex = new ArrayList<Integer>();
            ArrayList<Integer> resultValleyIndex = new ArrayList<Integer>();

            resultValleyIndex.add(finalValleyIndex.get(0));

            for (int i = 0; i < expirationAmplitude.length; i++) {
                if (expirationAmplitude[i] > AUTOSENSE.INSPIRATION_EXPIRATION_AMPLITUDE_THRESHOLD_FACTOR * meanExpirationAmplitude) {
                    resultValleyIndex.add(finalValleyIndex.get(i + 1));
                    resultPeakIndex.add(finalPeakIndex.get(i));
                }
            }
            resultPeakIndex.add(finalPeakIndex.get(finalPeakIndex.size() - 1));

            result.valleyIndex = resultValleyIndex;
            result.peakIndex = resultPeakIndex;
        }
        return result;
    }

    /**
     * Moving Average Curve
     * @param sample
     * @param windowLength
     * @return
     */
    public static DataPoint[] mac(DataPoint[] sample, int windowLength) {

        DataPoint[] result = new DataPoint[sample.length-2*windowLength];

        for(int i=windowLength;i < sample.length-windowLength; i++) {
            result[i-windowLength] = new DataPoint(0.0,0);
            for(int j=-windowLength; j<windowLength; j++) {
                result[i-windowLength].value += sample[i+j].value;
            }
            result[i-windowLength].value /= (2.0*windowLength); //Compute mean
            result[i-windowLength].timestamp = sample[i+windowLength].timestamp;

        }
        return result;
    }

    /**
     * Intercept Outlier Detector
     * Reference: Intercept_outlier_detector_RIP_lamia.m
     * @param upInterceptIndex
     * @param downInterceptIndex
     * @param sample
     * @param windowLength
     * @return
     */
    public static Intercepts InterceptOutlierDetectorRIPLamia(ArrayList<Integer> upInterceptIndex, ArrayList<Integer> downInterceptIndex, DataPoint[] sample, int windowLength, SensorConfiguration sc) {
        Intercepts result = new Intercepts();

        int minimumLength = Math.min(upInterceptIndex.size(), downInterceptIndex.size());

        ArrayList<Integer> D = new ArrayList<Integer>();
        ArrayList<Integer> U = new ArrayList<Integer>();
        for(int i=0; i<minimumLength; i++) {
            U.add(upInterceptIndex.get(i));
            D.add(downInterceptIndex.get(i));
        }

        ArrayList<Integer> UI = new ArrayList<Integer>();
        ArrayList<Integer> DI = new ArrayList<Integer>();

        int i = 0;
        int j = 0;
        while(i < U.size()-2) {
            if (j > (D.size()-1)) {
                break;
            }

            while(j < D.size()-1) {
               if(U.get(0) < D.get(0)) {
                   if (i == U.size() || j == D.size()) {
                       break;
                   }

                   if (U.get(i) < D.get(j) && D.get(j) < U.get(i+1)) {
                       UI.add(U.get(i));
                       ArrayList<Integer> ind = new ArrayList<Integer>();
                       for (Integer aD : D) {
                           if ((aD > D.get(j)) && (aD < U.get(i+1))) {
                               ind.add(aD);
                           }
                       }
                       if (ind.size() == 0) {
                           DI.add(D.get(j));
                           j++;
                       } else {
                           DI.add(ind.get(ind.size()-1));
                           j = ind.get(ind.size()-1)+1;
                       }
                       i++;
                   } else if (U.get(i) < D.get(j) && D.get(j) > U.get(i+1)) {
                       DI.add(D.get(i));
                       ArrayList<Integer> ind = new ArrayList<Integer>();
                       for (Integer aU : U) {
                           if ((aU > U.get(i)) && (aU < D.get(j))) {
                               ind.add(aU);
                           }
                       }
                       if (ind.size() == 0) {
                           UI.add(U.get(i));
                           i++;
                       } else {
                           UI.add(ind.get(ind.size()-1));
                           i = ind.get(ind.size()-1)+1;
                       }
                       j++;
                   }
               } else if (D.get(0) < U.get(0)) {
                   if (i == D.size() || j == U.size()) {
                       break;
                   }

                   if (D.get(i) < U.get(j) && U.get(j) < D.get(i+1)) {
                       DI.add(D.get(i));
                       ArrayList<Integer> ind = new ArrayList<Integer>();
                       for (Integer aU : U) {
                           if ((aU > U.get(j)) && (aU < D.get(i+1))) {
                               ind.add(aU);
                           }
                       }
                       if (ind.size() == 0) {
                           UI.add(U.get(j));
                           j++;
                       } else {
                           UI.add(ind.get(ind.size()-1));
                           j = ind.get(ind.size()-1)+1;
                       }
                       i++;
                   } else if (D.get(i) < U.get(j) && U.get(j) > D.get(i+1)) {
                       UI.add(U.get(i));
                       ArrayList<Integer> ind = new ArrayList<Integer>();
                       for (Integer aD : D) {
                           if ((aD > D.get(i)) && (aD < U.get(j))) {
                               ind.add(aD);
                           }
                       }
                       if (ind.size() == 0) {
                           DI.add(D.get(i));
                           i++;
                       } else {
                           DI.add(ind.get(ind.size()-1));
                           i = ind.get(ind.size()-1)+1;
                       }
                       j++;
                   }
               }
            }
        }

        if (UI.size() ==0 && DI.size() == 0) {
            return result;
        }
        if (UI.get(0) < DI.get(0)) {
            UI.remove(0);
        }

        minimumLength = Math.min(UI.size(),DI.size());
        while(UI.size() > minimumLength) {
            UI.remove(UI.size()-1);
        }
        while(DI.size() > minimumLength) {
            DI.remove(DI.size()-1);
        }


        ArrayList<Integer> DownIntercept = new ArrayList<Integer>();
        ArrayList<Integer> UpIntercept = new ArrayList<Integer>();
        double fr;
        for(int ii=0; ii<DI.size()-1; ii++) {
            fr = (double)windowLength / (sample[DI.get(ii+1)].timestamp - sample[DI.get(ii)].timestamp);
            if (fr >= AUTOSENSE.RESPIRATION_MINIMUM && fr <= AUTOSENSE.RESPIRATION_MAXIMUM) {
                DownIntercept.add(DI.get(ii));
                UpIntercept.add(UI.get(ii));
            }
        }


        ArrayList<Integer> DownIntercept2 = new ArrayList<Integer>();
        ArrayList<Integer> UpIntercept2 = new ArrayList<Integer>();
        double equivalentSamplePoints = AUTOSENSE.WINDOW_LENGTH_SECS/ AUTOSENSE.WINDOW_DIVIDER_FACTOR * sc.getFrequency("RIP");
        double upToDownDistance;
        for(int ii=0; ii<DownIntercept.size()-1; ii++) {
            upToDownDistance = DownIntercept.get(ii+1)-UpIntercept.get(ii)+1;
            if(upToDownDistance > equivalentSamplePoints) {
                UpIntercept2.add(UpIntercept.get(ii));
                DownIntercept2.add(DownIntercept.get(ii));
            }
        }

        if(DownIntercept2.size() > 0) {
            DownIntercept2.remove(DownIntercept2.size() - 1);
        }


        result.UI = new int[UpIntercept2.size()];
        result.DI = new int[UpIntercept2.size()-1];
        for(int ii=0; ii<UpIntercept2.size(); ii++) {
            result.UI[ii] = UpIntercept2.get(ii);
            if(ii < UpIntercept2.size()-1) {
                result.DI[ii] = DownIntercept2.get(ii);
            }
        }

        return result;
    }
}
