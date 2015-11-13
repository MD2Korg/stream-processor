package md2k.mCerebrum.cStress.Library;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Structs.*;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.*;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Ju Gao <gao.363@buckeyemail.osu.edu>
 * - Rummana Bari <rbari@memphis.edu>
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
     *
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
     *
     * @param length
     * @return
     */
    public static int nextPower2(int length) {
        if (length == 0)
            return 0;
        else
            return (int) Math.ceil(Math.log(length) / Math.log(2));
    }

    /**
     * Convert inputs to a magnitude vector
     *
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
     *
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
     *
     * @param dp
     * @return
     */
    public static ArrayList<DataPoint> diff(ArrayList<DataPoint> dp) {
        ArrayList<DataPoint> result = new ArrayList<DataPoint>();
        if (dp.size() != 0) {
            for (int i = 0; i < dp.size() - 1; i++) {
                result.add(new DataPoint(dp.get(i).timestamp, dp.get(i + 1).value - dp.get(i).value));
            }
        }
        return result;
    }


    public static void applyFilterNormalize(DataStream input, DataStream output, DataStream outputNormalized, double[] filter, int normalizePercentile) {
        double[] sample = new double[input.data.size()];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = input.data.get(i).value;
        }
        double[] result = conv(sample, filter);

        //Add value to datastream for computing percentiles
        for (int i = 0; i < result.length; i++) {
            output.add(new DataPoint(input.data.get(i).timestamp, result[i]));
        }
        //Normalized based on percentiles
        for (int i = 0; i < sample.length; i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp, output.data.get(i).value / output.getPercentile(normalizePercentile)));
        }
    }

    public static void applySquareFilterNormalize(DataStream input, DataStream output, DataStream outputNormalized, int normalizePercentile) {
        //Add value to datastream for computing percentiles
        for (int i = 0; i < input.data.size(); i++) {
            output.add(new DataPoint(input.data.get(i).timestamp, input.data.get(i).value * input.data.get(i).value));
        }
        //Normalized based on percentiles
        for (int i = 0; i < output.data.size(); i++) {
            outputNormalized.add(new DataPoint(output.data.get(i).timestamp, output.data.get(i).value / output.getPercentile(normalizePercentile)));
        }
    }


    /**
     * Standard implementation of the Blackman filter
     *
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
     *
     * @param fl Not used currently
     * @param f  Not used currently
     * @param a  Not used currently
     * @param w  Not used currently
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
     *
     * @param signal Input signal
     * @param kernel Kernel to apply to the signal
     * @return Convoluted signal
     */
    public static double[] conv(double[] signal, double[] kernel) {
        double[] result = new double[Math.max(Math.max(signal.length + kernel.length, signal.length), kernel.length)];

        double[] tempsignal = new double[signal.length + kernel.length];
        System.arraycopy(signal, 0, tempsignal, kernel.length / 2, signal.length); //Zero pad the end of signal

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
     *
     * @param rip
     * @param n
     * @return
     */
    public static void smooth(DataStream output, DataStream input, int n) {
        int windowSize = 1;
        double sum;
        for (int i = 0; i < input.data.size(); i++) {
            sum = 0.0;
            int startingPoint;
            if ((input.data.size() - i + 1) < n) {
                startingPoint = input.data.size() - windowSize;
            } else {
                startingPoint = (int) Math.max(Math.floor(i - n / 2), 0);
            }
            for (int j = startingPoint; j < startingPoint + windowSize; j++) {
                sum += input.data.get(j).value;
            }
            sum /= (double) windowSize;

            output.add(new DataPoint(input.data.get(i).timestamp, sum));

            if (windowSize < n && (input.data.size() - i) > n) { //Increase windowSize until n
                windowSize += 2;
            } else if ((input.data.size() - i + 1) < n) {
                windowSize -= 2;
            }
        }
    }


    public static long nextEpochTimestamp(long timestamp) {
        long previousMinute = timestamp / (60 * 1000);
        Date date = new Date((previousMinute + 1) * (60 * 1000));
        return date.getTime();
    }

    /**
     * Basic time bounded windowing.
     *
     * @param data
     * @param size
     * @return
     */
    public static ArrayList<DataPoint[]> window(ArrayList<DataPoint> data, int size) {
        ArrayList<DataPoint[]> result = new ArrayList<DataPoint[]>();

        if(data.size() > 0) {
            long startTime = nextEpochTimestamp(data.get(0).timestamp) - 60 * 1000; //Get next minute window and subtract a minute to arrive at the appropriate startTime
            ArrayList<DataPoint> tempArray = new ArrayList<DataPoint>();
            DataPoint[] temp;
            for (DataPoint dp : data) {
                if (dp.timestamp < startTime + size) {
                    tempArray.add(dp);
                } else {
                    temp = new DataPoint[tempArray.size()];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = tempArray.get(i);
                    }
                    result.add(temp);
                    tempArray = new ArrayList<DataPoint>();
                    startTime += size;
                }
            }
            temp = new DataPoint[tempArray.size()];
            for (int i = 0; i < temp.length; i++) {
                temp[i] = tempArray.get(i);
            }
            result.add(temp);
        }
        return result;
    }


    /**
     * Basic EWMA function
     *
     * @param x
     * @param y
     * @param alpha
     * @return
     */
    public static double ewma(double x, double y, double alpha) {
        return alpha * x + (1 - alpha) * y;
    }

    /**
     * Lomb–Scargle periodogram implementation
     * Reference: HeartRateLomb.m
     *
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
            part1 = part1 * part1; //Square result
            part2 = part2 * part2; //Square result

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }

    /**
     * Heartrate Low Frequency - High Frequency ratio
     *
     * @param P
     * @param f
     * @param lowRate  Low frequency cutoff
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
     *
     * @param P
     * @param f
     * @param lowFrequency  Low frequency cutoff
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

    public static void normalize(DataStream output, DataStream input) {
        for (DataPoint dp: input.data) {
            output.add(new DataPoint(dp.timestamp,(dp.value - input.getMean()) / input.getStandardDeviation()));
        }
    }


    //TODO: Can this be removed?
//    /**
//     * Moving Average Curve
//     * @param sample
//     * @param windowLength
//     * @return
//     */
//    public static ArrayList<DataPoint> mac(ArrayList<DataPoint> sample, int windowLength) {
//
//        ArrayList<DataPoint> result = new ArrayList<DataPoint>();
//
//        for(int i=windowLength;i < sample.size()-windowLength; i++) {
//            result.add( new DataPoint(0, 0.0) );
//            for(int j=-windowLength; j<windowLength; j++) {
//                result.get(i-windowLength).value += sample.get(i+j).value;
//            }
//            result.get(i-windowLength).value /= (2.0*windowLength); //Compute mean
//            result.get(i-windowLength).timestamp = sample.get(i+windowLength).timestamp;
//
//        }
//        return result;
//    }

}
