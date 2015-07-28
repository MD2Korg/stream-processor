package md2k.mCerebrum.cStress.Features;

import com.sun.tools.doclets.formats.html.resources.standard;
import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Lomb;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
public class ECGFeatures {
    private final DataPoint[] datapoints;
    private final double frequency;

    //ecg_statistical_features.m

//    public double variance;
//    public double heartrateFLHF;
//    public double heartratepower12;
//    public double heartratepower23;
//    public double heartratepower34;
//    public double rr_mean;
//    public double rr_median;
//    public double rr_quartilerange;
//    public double rr_80percentile;
//    public double rr_20percentile;
//    public double rr_count;

    private double[] rr_value = new double[0];
    private long[] rr_timestamp = new long[0];
    private long[] rr_index = new long[0];
    private int[] rr_outlier = new int[0];


    public DescriptiveStatistics RRStats;
    public double HeartRate;
    public double LombLowFrequencyEnergy;
    public double LombMediumFrequencyEnergy;
    public double LombHighFrequencyEnergy;
    public double LombLowHighFrequencyEnergyRatio;


    public boolean computeRR() {
        long[] Rpeak_index = detect_Rpeak(this.datapoints, this.frequency);

        long[] pkT = new long[Rpeak_index.length];
        for (int i = 0; i < Rpeak_index.length; i++) {
            pkT[i] = this.datapoints[(int) Rpeak_index[i]].timestamp;
        }

        if (pkT.length < 2) {
            System.out.println("Not enough peaks, aborting computation");
            return false;
        }

        rr_value = new double[pkT.length - 1];
        rr_timestamp = new long[pkT.length - 1];


        for (int i = 1; i < pkT.length; i++) {
            rr_value[i - 1] = (double) (pkT[i] - pkT[i - 1]) / 1000.0;
            rr_timestamp[i - 1] = pkT[i - 1];

        }

        rr_index = new long[pkT.length];
        System.arraycopy(pkT, 0, rr_index, 0, pkT.length);

        rr_outlier = detect_outlier_v2(rr_value, rr_timestamp);

        DescriptiveStatistics valueStats = new DescriptiveStatistics();
        for (int i = 0; i < rr_value.length; i++) {
            if (rr_outlier[i] == 0) {
                valueStats.addValue(rr_value[i]);
            }
        }

        double mu = valueStats.getMean();
        double sigma = valueStats.getStandardDeviation();

        for (int i = 0; i < rr_outlier.length; i++) {
            if (rr_outlier[i] == 0) {
                if (Math.abs(rr_value[i] - mu) > (3.0 * sigma)) {
                    rr_outlier[i] = 1;
                } else {
                    rr_outlier[i] = 0;
                }
            }
        }

        return true;
    }

    public static int[] detect_outlier_v2(double[] sample, long[] timestamp) {
        ArrayList<Integer> outlier = new ArrayList<>();

        try {
            if (timestamp.length != 0) {
                ArrayList<Double> valid_rrInterval = new ArrayList<>();
                ArrayList<Long> valid_timestamp = new ArrayList<>();
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
                double MED = 4.5 * 0.5 * (diff_rrInterval.getPercentile(75) - diff_rrInterval.getPercentile(25));
                double MAD = (valid_rrInterval_stats.getPercentile(50) - 2.8 * 0.5 * (diff_rrInterval.getPercentile(75) - diff_rrInterval.getPercentile(25))) / 3.0;
                double CBD = (MED + MAD) / 2.0;
                if (CBD < 0.2) {
                    CBD = 0.2;
                }

                for (double aSample : sample) {
                    outlier.add(AUTOSENSE.G_QUALITY_BAD);
                }
                outlier.set(0, AUTOSENSE.G_QUALITY_GOOD);
                double standard_rrInterval = valid_rrInterval.get(0); //TODO: What to do on error case?
                boolean prev_beat_bad = false;

                for (int i = 1; i < valid_rrInterval.size() - 1; i++) {
                    double ref = valid_rrInterval.get(i);
                    if (ref > 0.3 && ref < 2.0) {
                        double beat_diff_prevGood = Math.abs(standard_rrInterval - valid_rrInterval.get(i));
                        double beat_diff_pre = Math.abs(valid_rrInterval.get(i - 1) - valid_rrInterval.get(i));
                        double beat_diff_post = Math.abs(valid_rrInterval.get(i) - valid_rrInterval.get(i + 1));

                        if (prev_beat_bad && beat_diff_prevGood < CBD) {
                            for (int j = 0; j < timestamp.length; j++) {
                                if (timestamp[j] == valid_timestamp.get(i)) {
                                    outlier.set(j, AUTOSENSE.G_QUALITY_GOOD);
                                }
                            }
                            prev_beat_bad = false;
                            standard_rrInterval = valid_rrInterval.get(i);
                        } else if (prev_beat_bad && beat_diff_prevGood > CBD && beat_diff_pre <= CBD && beat_diff_post <= CBD) {
                            for (int j = 0; j < timestamp.length; j++) {
                                if (timestamp[j] == valid_timestamp.get(i)) {
                                    outlier.set(j, AUTOSENSE.G_QUALITY_GOOD);
                                }
                            }
                            prev_beat_bad = false;
                            standard_rrInterval = valid_rrInterval.get(i);
                        } else if (prev_beat_bad && beat_diff_prevGood > CBD && (beat_diff_pre > CBD || beat_diff_post > CBD)) {
                            prev_beat_bad = true;
                        } else if (!prev_beat_bad && beat_diff_pre <= CBD) {
                            for (int j = 0; j < timestamp.length; j++) {
                                if (timestamp[j] == valid_timestamp.get(i)) {
                                    outlier.set(j, AUTOSENSE.G_QUALITY_GOOD);
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
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        int[] result = new int[outlier.size()];
        for (int i = 0; i < outlier.size(); i++) {
            result[i] = outlier.get(i);
        }
        return result;
    }

    public static long[] detect_Rpeak(DataPoint[] datapoints, double frequency) {

        double[] sample = new double[datapoints.length];
        double[] timestamps = new double[datapoints.length];
        for (int i = 0; i < sample.length; i++) {
            sample[i] = datapoints[i].value;
            timestamps[i] = datapoints[i].timestamp;
        }

        int window_l = (int) Math.ceil(frequency / 5.0);

        double thr1 = 0.5;
        double f = 2.0 / frequency;
        double delp = 0.02;
        double dels1 = 0.02;
        double dels2 = 0.02;
        double[] F = {0.0, 4.5 * f, 5.0 * f, 20.0 * f, 20.5 * f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0 / dels1, 1.0 / delp, 500 / dels2};
        double fl = 256;
        double[] b = firls(fl, F, A, w);

        double[] y2 = conv(sample, b);
        DescriptiveStatistics statsY2 = new DescriptiveStatistics();
        for (double d : y2) {
            statsY2.addValue(d);
        }
        for (int i = 0; i < y2.length; i++) {
            y2[i] /= statsY2.getPercentile(90);
        }

        double[] h_D = {-1.0 / 8.0, -2.0 / 8.0, 0.0 / 8.0, 2.0 / 8.0, -1.0 / 8.0};
        double[] y3 = conv(y2, h_D);
        DescriptiveStatistics statsY3 = new DescriptiveStatistics();
        for (double d : y3) {
            statsY3.addValue(d);
        }
        for (int i = 0; i < y3.length; i++) {
            y3[i] /= statsY3.getPercentile(90);
        }

        double[] y4 = new double[y3.length];
        DescriptiveStatistics statsY4 = new DescriptiveStatistics();
        for (int i = 0; i < y3.length; i++) {
            y4[i] = y3[i] * y3[i];
            statsY4.addValue(y4[i]);
        }
        for (int i = 0; i < y4.length; i++) {
            y4[i] /= statsY4.getPercentile(90);
        }

        double[] h_I = blackman(window_l);
        double[] y5 = conv(y4, h_I);
        DescriptiveStatistics statsY5 = new DescriptiveStatistics();
        for (double d : y5) {
            statsY5.addValue(d);
        }
        for (int i = 0; i < y5.length; i++) {
            y5[i] /= statsY5.getPercentile(90);
        }

        ArrayList<Integer> pkt = new ArrayList<>();
        ArrayList<Double> valuepks = new ArrayList<>();
        for (int i = 2; i < y5.length - 2; i++) {
            if (y5[i - 2] < y5[i - 1] && y5[i - 1] < y5[i] && y5[i] < y5[i + 1] && y5[i + 1] < y5[i + 2]) {
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
        double sig_lev = 4.0 * thr1;
        double noise_lev = 0.1 * sig_lev;

        int c1 = 1;
        ArrayList<Integer> c2 = new ArrayList<>();
        int i = 1;
        ArrayList<Integer> Rpeak_temp1 = new ArrayList<>();


        while (i < pkt.size()) {
            if (Rpeak_temp1.size() == 0) {
                if (y5[pkt.get(i)] >= thr1 && y5[pkt.get(i)] < (3.0 * sig_lev)) {
                    Rpeak_temp1.add(pkt.get(i));
                    sig_lev = 0.125 * y5[pkt.get(i)] + 0.875 * sig_lev;
                    c2.add(i);
                    c1 += 1;
                } else if (y5[pkt.get(i)] < thr1 && y5[pkt.get(i)] > thr2) {
                    noise_lev = 0.125 * y5[pkt.get(i)] + 0.875 * noise_lev;
                }

                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                i += 1;

                rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
            } else {
                if (((pkt.get(i) - pkt.get(c2.get(c1 - 2))) > 1.66 * rr_ave) && (i - c2.get(c1 - 2)) > 1) { //TODO: This code block is broken.  Decipher how the pkt, c1, and c2 variables interact.  Indexing problems right now.
                    ArrayList<Double> searchback_array_inrange = new ArrayList<>();
                    ArrayList<Integer> searchback_array_inrange_index = new ArrayList<>();

                    for (int j = c2.get(c1 - 2) + 1; j < i - 1; j++) {
                        if (valuepks.get(i) < 3.0 * sig_lev && valuepks.get(i) > thr2) {
                            searchback_array_inrange.add(valuepks.get(i));
                            searchback_array_inrange_index.add(j - c2.get(c1 - 1));
                        }
                    }

                    if (searchback_array_inrange.size() > 0) {
                        double searchback_max = -1e9;
                        int searchback_max_index = -999999;
                        for (int j = 0; j < searchback_array_inrange.size(); j++) {
                            if (searchback_array_inrange.get(j) > searchback_max) {
                                searchback_max = searchback_array_inrange.get(i);
                                searchback_max_index = j;
                            }
                        }
                        Rpeak_temp1.add(pkt.get(c2.get(c1 - 2)) + searchback_array_inrange_index.get(searchback_max_index));
                        sig_lev = 0.125 * y5[Rpeak_temp1.get(c1 - 1)] + 0.875 * sig_lev;
                        c2.set(c1, c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index));
                        i = c2.get(c1 - 1) + 1;
                        c1 += 1;
                        thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                        thr2 = 0.5 * thr1;
                        rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
                        continue;
                    }
                } else if (y5[pkt.get(i)] >= thr1 && y5[pkt.get(i)] < 3.0 * sig_lev) {
                    Rpeak_temp1.add(pkt.get(i));
                    sig_lev = 0.125 * y5[pkt.get(i)] + 0.875 * sig_lev;
                    c2.add(i);
                    c1 += 1;
                } else if (y5[pkt.get(i)] < thr1 && y5[pkt.get(i)] > thr2) {
                    noise_lev = 0.125 * y5[pkt.get(i)] + 0.875 * noise_lev;
                }
                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                i += 1;
                rr_ave = rr_ave_update(Rpeak_temp1, rr_ave);
            }
        }

        boolean difference = false;

        ArrayList<Integer> Rpeak_temp2 = new ArrayList<>();
        for (Integer j : Rpeak_temp1) {
            Rpeak_temp2.add(j);
        }


        while (!difference) {
            int length_Rpeak_temp2 = Rpeak_temp2.size();
            ArrayList<Integer> comp_index1 = new ArrayList<>();
            ArrayList<Integer> comp_index2 = new ArrayList<>();
            ArrayList<Double> comp1 = new ArrayList<>();
            ArrayList<Double> comp2 = new ArrayList<>();

            for (int j = 1; j < Rpeak_temp2.size(); j++) {
                if ((Rpeak_temp2.get(j) - Rpeak_temp2.get(j - 1)) < (0.5 * frequency))
                    comp_index1.add(Rpeak_temp2.get(j - 1));
                comp_index2.add(Rpeak_temp2.get(j));
            }
            for (int j = 0; j < comp_index1.size(); j++) {
                comp1.add(sample[comp_index1.get(j)]);
                comp2.add(sample[comp_index2.get(j)]);
            }
            ArrayList<Integer> eli_index = new ArrayList<>();
            for (int j = 0; j < comp1.size(); j++) {
                if (comp1.get(j) < comp2.get(j)) {
                    eli_index.add(0);
                } else {
                    eli_index.add(1);
                }
            }

            ArrayList<Integer> temp = new ArrayList<>();
            try {
                for (int j = 1; j < Rpeak_temp2.size(); j++) {
                    if ((Rpeak_temp2.get(j) - Rpeak_temp2.get(j - 1)) < (0.5 * frequency)) {
                        Rpeak_temp2.set(j + eli_index.get(j - 1) - 1, -9999999); //TODO: Broken indexing.  What is the desired behavior here and in the Matlab code?  This is a hack to workaround the Matlab implementation.
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (Integer aRpeak_temp2 : Rpeak_temp2) {
                if (aRpeak_temp2 != -9999999) {
                    temp.add(aRpeak_temp2);
                }
            }
            Rpeak_temp2 = temp;

            difference = length_Rpeak_temp2 == Rpeak_temp2.size();

        }

        ArrayList<Integer> Rpeak_temp3 = new ArrayList<>();
        Rpeak_temp3.add(Rpeak_temp2.get(0));

        for (int k = 1; k < Rpeak_temp2.size() - 1; k++) {
            double maxValue = -1e9;
            int index = 0;
            for (int j = Rpeak_temp2.get(k) - (int) Math.ceil(frequency / 10.0); j < Rpeak_temp2.get(k) + (int) Math.ceil(frequency / 10.0); j++) {
                if (sample[j] > maxValue) {
                    maxValue = sample[j];
                    index = j;
                }
            }
            Rpeak_temp3.add(index - 1);
        }


        long[] result = new long[Rpeak_temp3.size()];
        for (int k = 0; k < Rpeak_temp3.size(); k++) {
            result[k] = (long) Rpeak_temp3.get(k);
        }

        return result;
    }

    public static double rr_ave_update(ArrayList<Integer> rpeak_temp1, double rr_ave) {
        ArrayList<Integer> peak_interval = new ArrayList<>();

        peak_interval.add(rpeak_temp1.get(0));
        for (int i = 1; i < rpeak_temp1.size(); i++) {
            peak_interval.add(rpeak_temp1.get(i) - rpeak_temp1.get(i - 1));
        }

        if (peak_interval.size() < 8) {
            return rr_ave;
        } else {
            double result = 0.0;
            for (int i = peak_interval.size() - 8; i < peak_interval.size(); i++) {
                result += peak_interval.get(i);
            }
            result /= 8.0;
            return result;
        }
    }

    public static double[] blackman(int window_l) {
        double[] result = new double[window_l];
        int M = (int) Math.floor((window_l + 1) / 2);
        for (int i = 0; i < M; i++) {
            result[i] = 0.42 - 0.5 * Math.cos(2.0 * Math.PI * (double) i / (window_l - 1)) + 0.08 * Math.cos(4.0 * Math.PI * (double) i / (window_l - 1));
            result[window_l - i - 1] = result[i];
        }
        return result;
    }

    public static double[] firls(double fl, double[] f, double[] a, double[] w) {
        //Hardcoded to the specifications of the Autosense ECG sensor right now

        double[] result = {-4.7274160329724543e-04, -4.3910892627884660e-04, 8.6902448327532778e-04, 1.2580375981368801e-03, -7.0870338859324122e-05, -3.1826848647111390e-04, 2.6535857915570944e-04, -8.1833969244558271e-04, -1.4124933569042541e-03, -1.9995228246965813e-05, 6.7313359750941992e-05, -6.0349375063082288e-04, 8.6714439266190248e-04, 1.5065160936261314e-03, 1.0310266272463235e-04, 5.4715012534279738e-04, 1.1904732453105013e-03, -7.6681365348830479e-04, -1.2037758114175129e-03, 3.1547725670266574e-05, -1.2630183530614810e-03, -1.7863067743220608e-03, 4.5997532952314775e-04, 3.2716936532409155e-04, -5.2644177116152754e-04, 1.7385569813462844e-03, 1.9889763081328412e-03, -1.0568777171727037e-04, 1.0085738914257093e-03, 1.3166081526795959e-03, -1.6828106527494776e-03, -1.3954395354213642e-03, 3.6314731240927499e-06, -2.3580287405187803e-03, -2.0329727875177984e-03, 1.0570699668543437e-03, -1.2914364939980619e-04, -3.8546902662939887e-04, 3.1026282496662044e-03, 2.0891652801290394e-03, -1.7040978539860152e-04, 2.2345403662638666e-03, 1.1662141454746484e-03, -2.7739626598314152e-03, -9.8117447601674384e-04, -4.4741116462223640e-04, -4.0970300710684790e-03, -1.8325183286848025e-03, 1.3776445217444490e-03, -1.3211583879184985e-03, 3.7867414252479948e-04, 4.7380839079786659e-03, 1.6152763281382203e-03, 5.0172009801508800e-04, 4.1276888849617836e-03, 3.2171309686397854e-04, -3.5444229001001413e-03, 5.9320034125439428e-05, -1.9474610880852858e-03, -6.1508063614239862e-03, -9.9665665297590886e-04, 7.1573500042050489e-04, -3.0500439336000633e-03, 2.2375273611317816e-03, 6.0494339632141611e-03, 6.6666900112815589e-04, 2.6644168389563642e-03, 6.2464301815488002e-03, -1.3664646323453468e-03, -3.1859375309960399e-03, 1.3346226939768935e-03, -5.0751244870958813e-03, -7.8491706097835925e-03, 2.0337081693415804e-04, -1.8091340945618182e-03, -4.6875184234183237e-03, 5.3652517463266009e-03, 6.1905855566496198e-03, -7.4368051532226921e-05, 7.0575372722313061e-03, 7.8206867803311906e-03, -3.4972150936961369e-03, -7.6079548874512096e-04, 1.8835178013748015e-03, -1.0170003906003467e-02, -8.3679423125302028e-03, 7.4577361924604336e-04, -7.1106914021255080e-03, -5.2165564908720000e-03, 9.4292401382520472e-03, 4.2997116416556890e-03, 8.9934836096324411e-04, 1.4397066723214469e-02, 7.9962549473134286e-03, -4.8304877652048297e-03, 4.7345958483973353e-03, 4.9390510587356005e-05, -1.7446053084899240e-02, -7.0564384166495178e-03, -1.6381246987009848e-03, -1.6536831781652900e-02, -3.3074012030242165e-03, 1.3580158825803681e-02, -4.6367025497966462e-04, 6.7704049102291549e-03, 2.6487077400862109e-02, 6.1540352580244520e-03, -2.4533091741271327e-03, 1.5497475687230644e-02, -7.7410309481643867e-03, -2.8629194004745218e-02, -3.6522550021604841e-03, -1.3632609634575103e-02, -3.6030712287501945e-02, 3.8924925851573531e-03, 1.6707802872026386e-02, -1.1053287553442563e-02, 3.1002542423973951e-02, 5.7319009934381555e-02, 1.6637598373033171e-03, 1.8714840588857272e-02, 5.1616797200589280e-02, -4.8886309433549893e-02, -7.3414476566383474e-02, 4.0083352853056056e-03, -1.4044338414188356e-01, -2.4550216720451279e-01, 1.4662066861996509e-01, 4.7941126114385940e-01, 1.4662066861996509e-01, -2.4550216720451279e-01, -1.4044338414188356e-01, 4.0083352853056056e-03, -7.3414476566383474e-02, -4.8886309433549893e-02, 5.1616797200589280e-02, 1.8714840588857272e-02, 1.6637598373033171e-03, 5.7319009934381555e-02, 3.1002542423973951e-02, -1.1053287553442563e-02, 1.6707802872026386e-02, 3.8924925851573531e-03, -3.6030712287501945e-02, -1.3632609634575103e-02, -3.6522550021604841e-03, -2.8629194004745218e-02, -7.7410309481643867e-03, 1.5497475687230644e-02, -2.4533091741271327e-03, 6.1540352580244520e-03, 2.6487077400862109e-02, 6.7704049102291549e-03, -4.6367025497966462e-04, 1.3580158825803681e-02, -3.3074012030242165e-03, -1.6536831781652900e-02, -1.6381246987009848e-03, -7.0564384166495178e-03, -1.7446053084899240e-02, 4.9390510587356005e-05, 4.7345958483973353e-03, -4.8304877652048297e-03, 7.9962549473134286e-03, 1.4397066723214469e-02, 8.9934836096324411e-04, 4.2997116416556890e-03, 9.4292401382520472e-03, -5.2165564908720000e-03, -7.1106914021255080e-03, 7.4577361924604336e-04, -8.3679423125302028e-03, -1.0170003906003467e-02, 1.8835178013748015e-03, -7.6079548874512096e-04, -3.4972150936961369e-03, 7.8206867803311906e-03, 7.0575372722313061e-03, -7.4368051532226921e-05, 6.1905855566496198e-03, 5.3652517463266009e-03, -4.6875184234183237e-03, -1.8091340945618182e-03, 2.0337081693415804e-04, -7.8491706097835925e-03, -5.0751244870958813e-03, 1.3346226939768935e-03, -3.1859375309960399e-03, -1.3664646323453468e-03, 6.2464301815488002e-03, 2.6644168389563642e-03, 6.6666900112815589e-04, 6.0494339632141611e-03, 2.2375273611317816e-03, -3.0500439336000633e-03, 7.1573500042050489e-04, -9.9665665297590886e-04, -6.1508063614239862e-03, -1.9474610880852858e-03, 5.9320034125439428e-05, -3.5444229001001413e-03, 3.2171309686397854e-04, 4.1276888849617836e-03, 5.0172009801508800e-04, 1.6152763281382203e-03, 4.7380839079786659e-03, 3.7867414252479948e-04, -1.3211583879184985e-03, 1.3776445217444490e-03, -1.8325183286848025e-03, -4.0970300710684790e-03, -4.4741116462223640e-04, -9.8117447601674384e-04, -2.7739626598314152e-03, 1.1662141454746484e-03, 2.2345403662638666e-03, -1.7040978539860152e-04, 2.0891652801290394e-03, 3.1026282496662044e-03, -3.8546902662939887e-04, -1.2914364939980619e-04, 1.0570699668543437e-03, -2.0329727875177984e-03, -2.3580287405187803e-03, 3.6314731240927499e-06, -1.3954395354213642e-03, -1.6828106527494776e-03, 1.3166081526795959e-03, 1.0085738914257093e-03, -1.0568777171727037e-04, 1.9889763081328412e-03, 1.7385569813462844e-03, -5.2644177116152754e-04, 3.2716936532409155e-04, 4.5997532952314775e-04, -1.7863067743220608e-03, -1.2630183530614810e-03, 3.1547725670266574e-05, -1.2037758114175129e-03, -7.6681365348830479e-04, 1.1904732453105013e-03, 5.4715012534279738e-04, 1.0310266272463235e-04, 1.5065160936261314e-03, 8.6714439266190248e-04, -6.0349375063082288e-04, 6.7313359750941992e-05, -1.9995228246965813e-05, -1.4124933569042541e-03, -8.1833969244558271e-04, 2.6535857915570944e-04, -3.1826848647111390e-04, -7.0870338859324122e-05, 1.2580375981368801e-03, 8.6902448327532778e-04, -4.3910892627884660e-04, -4.7274160329724543e-04};

        return result;
    }

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
        System.arraycopy(result, 0, shortresult, 0, signal.length);
        return shortresult;
    }



    public ECGFeatures(DataPoint[] dp, double freq) {

        this.datapoints = dp;
        this.frequency = freq;

        if (computeRR()) {

            RRStats = new DescriptiveStatistics();

            for (int i = 0; i < rr_value.length; i++) {
                if (rr_outlier[i] == AUTOSENSE.G_QUALITY_GOOD) {
                    RRStats.addValue(rr_value[i]);
                }
            }

            HeartRate = ((double) RRStats.getN()) / (dp[dp.length - 1].timestamp - dp[0].timestamp);

            DataPoint[] rrDatapoints = new DataPoint[(int) RRStats.getN()];
            for (int i = 0; i < rrDatapoints.length; i++) {
                rrDatapoints[i] = new DataPoint(RRStats.getElement(i), i);
            }


            Lomb HRLomb = lomb(rrDatapoints); //TODO: I don't think this is supposed to be RR intervals.  Please confirm/deny

            LombLowHighFrequencyEnergyRatio = heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15);
            LombLowFrequencyEnergy = heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2);
            LombMediumFrequencyEnergy = heartRatePower(HRLomb.P, HRLomb.f, 0.2, 0.3);
            LombHighFrequencyEnergy = heartRatePower(HRLomb.P, HRLomb.f, 0.3, 0.4);

        }
    }

    public static double heartRateLFHF(double[] P, double[] f, double lowRate, double highRate) {
        double result1 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] < lowRate) {
                result1 += P[i];
            }
        }
        double result2 = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowRate && f[i] <= highRate) { //Should this be >= lowRate instead of what is in the code?
                result2 += P[i];
            }
        }
        return result1 / result2;
    }

    public static double heartRatePower(double[] P, double[] f, double lowFrequency, double highFrequency) {
        double result = 0;
        for (int i = 0; i < P.length; i++) {
            if (f[i] >= lowFrequency && f[i] <= highFrequency) {
                result += P[i];
            }
        }

        return result;
    }

    public Lomb lomb(DataPoint[] dp) {
        //HeartRateLomb.m


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
                cwttau[j] = cwt[j] * cwtau - swt[j] * swtau;

                swttau2 += swttau[j] * swttau[j];
                cwttau2 += cwttau[j] * cwttau[j];
            }


            part1 = 0;
            part2 = 0;
            for (int j = 0; j < cwttau.length; j++) {
                part1 += (dp[j].value * cwttau[j]) * (dp[j].value * cwttau[j]);
                part2 += (dp[j].value * swttau[j]) * (dp[j].value * swttau[j]);
            }

            P[i] = ((part1 / cwttau2) + (part2 / swttau2)) / (2 * vx);

        }

        Lomb result = new Lomb();
        result.P = P;
        result.f = f;

        return result;
    }


}
