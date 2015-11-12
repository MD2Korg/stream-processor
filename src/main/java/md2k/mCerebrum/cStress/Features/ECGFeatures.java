package md2k.mCerebrum.cStress.Features;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Library.Core;
import md2k.mCerebrum.cStress.Library.DataStream;
import md2k.mCerebrum.cStress.Library.DataStreams;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.Lomb;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.Iterator;

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
public class ECGFeatures {

    public ECGFeatures(DataStreams datastreams) {

        //Compute RR Intervals
        String result = computeRR(datastreams);
        if (result.matches("org.md2k.success")) {

            double activity = datastreams.get("org.md2k.cstress.data.accel.activity").data.get(0).value;
            //Decide if we should add the RR intervals from this minute to the running stats
            if (activity == 0.0) {

                for (int i = 0; i < datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.size(); i++) {
                    if (datastreams.get("org.md2k.cstress.data.ecg.outlier").data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                        datastreams.get("org.md2k.cstress.data.ecg.rr").add(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i));
                        DataPoint hr = new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).timestamp, 60.0 / datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).value);
                        datastreams.get("org.md2k.cstress.data.ecg.rr.heartrate").add(hr);
                    }
                }

                DataPoint[] rrDatapoints = new DataPoint[(int) datastreams.get("org.md2k.cstress.data.ecg.rr").data.size()];
                for (int i = 0; i < rrDatapoints.length; i++) {
                    rrDatapoints[i] = new DataPoint(i, datastreams.get("org.md2k.cstress.data.ecg.rr").data.get(i).value);
                }

                if(rrDatapoints.length > 0) {
                    Lomb HRLomb = Core.lomb(rrDatapoints);

                    datastreams.get("org.md2k.cstress.data.ecg.rr.LowHighFrequencyEnergyRatio").add(new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRateLFHF(HRLomb.P, HRLomb.f, 0.09, 0.15)));
                    datastreams.get("org.md2k.cstress.data.ecg.rr.LombLowFrequencyEnergy").add(new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.1, 0.2)));
                    datastreams.get("org.md2k.cstress.data.ecg.rr.LombMediumFrequencyEnergy").add(new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.2, 0.3)));
                    datastreams.get("org.md2k.cstress.data.ecg.rr.LombHighFrequencyEnergy").add(new DataPoint(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(0).timestamp, Core.heartRatePower(HRLomb.P, HRLomb.f, 0.3, 0.4)));
                }
            }
        }
    }

    /**
     * R-peak detector
     *
     * @param datapoints Raw ECG datapoints
     * @param frequency  ECG sampling frequency
     * @return Indexes of R-peaks
     */
    public String detect_Rpeak(DataStreams datastreams) {

        DataStream ECG = datastreams.get("org.md2k.cstress.data.ecg");
        double frequency = (Double) ECG.metadata.get("frequency");

        //Ohio State Algorithm
        int window_l = (int) Math.ceil(frequency / 5.0);

        //Specific to Autosense hardware @ 64Hz and 12-bit values //TODO: Fix this
        double thr1 = AUTOSENSE.THR1_INIT;
        double f = 2.0 / frequency;
        double[] F = {0.0, 4.5 * f, 5.0 * f, 20.0 * f, 20.5 * f, 1};
        double[] A = {0, 0, 1, 1, 0, 0};
        double[] w = {500.0 / 0.02, 1.0 / 0.02, 500 / 0.02};
        double fl = AUTOSENSE.FL_INIT;


        Core.applyFilterNormalize(datastreams.get("org.md2k.cstress.data.ecg"),
                datastreams.get("org.md2k.cstress.data.ecg.y2"),
                datastreams.get("org.md2k.cstress.data.ecg.y2-normalized"),
                Core.firls(fl, F, A, w),
                90);

        Core.applyFilterNormalize(datastreams.get("org.md2k.cstress.data.ecg.y2-normalized"),
                datastreams.get("org.md2k.cstress.data.ecg.y3"),
                datastreams.get("org.md2k.cstress.data.ecg.y3-normalized"),
                new double[]{-1.0 / 8.0, -2.0 / 8.0, 0.0 / 8.0, 2.0 / 8.0, -1.0 / 8.0},
                90);

        Core.applySquareFilterNormalize(datastreams.get("org.md2k.cstress.data.ecg.y3-normalized"),
                datastreams.get("org.md2k.cstress.data.ecg.y4"),
                datastreams.get("org.md2k.cstress.data.ecg.y4-normalized"),
                90);

        Core.applyFilterNormalize(datastreams.get("org.md2k.cstress.data.ecg.y4-normalized"),
                datastreams.get("org.md2k.cstress.data.ecg.y5"),
                datastreams.get("org.md2k.cstress.data.ecg.y5-normalized"),
                Core.blackman(window_l),
                90);

        DataStream y5 = datastreams.get("org.md2k.cstress.data.ecg.y5-normalized");
        for (int i = 2; i < y5.data.size() - 2; i++) {
            if (y5.data.get(i - 2).value < y5.data.get(i - 1).value &&
                    y5.data.get(i - 1).value < y5.data.get(i).value &&
                    y5.data.get(i).value >= y5.data.get(i + 1).value &&
                    y5.data.get(i + 1).value > y5.data.get(i + 2).value) { //TODO: Why is this hard-coded to five samples to examine?
                datastreams.get("org.md2k.cstress.data.ecg.peaks").add(new DataPoint(y5.data.get(i)));
            }
        }


        // If CURRENTPEAK > THR_SIG, that location is identified as a ìQRS complex
        // candidateî and the signal level (SIG_LEV) is updated:
        // SIG _ LEV = 0.125 ◊CURRENTPEAK + 0.875◊ SIG _ LEV
        // If THR_NOISE < CURRENTPEAK < THR_SIG, then that location is identified as a
        // ìnoise peakî and the noise level (NOISE_LEV) is updated:
        // NOISE _ LEV = 0.125◊CURRENTPEAK + 0.875◊ NOISE _ LEV
        // Based on new estimates of the signal and noise levels (SIG_LEV and NOISE_LEV,
        // respectively) at that point in the ECG, the thresholds are adjusted as follows:
        // THR _ SIG = NOISE _ LEV + 0.25 ◊ (SIG _ LEV ? NOISE _ LEV )
        // THR _ NOISE = 0.5◊ (THR _ SIG)


        double thr2 = 0.5 * thr1;
        double sig_lev = AUTOSENSE.SIG_LEV_FACTOR * thr1;
        double noise_lev = AUTOSENSE.NOISE_LEV_FACTOR * sig_lev;


        DataStream peaks = datastreams.get("org.md2k.cstress.data.ecg.peaks");
        DataPoint rr_ave;

        if (datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").stats.getN() == 0) {
            datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").setPreservedLastInsert(true);
            double rr_avg = 0.0;
            for (int i = 1; i < peaks.data.size(); i++) {
                rr_avg += peaks.data.get(i).value - peaks.data.get(i - 1).value;
            }
            rr_avg /= (peaks.data.size() - 1);
            rr_ave = new DataPoint(ECG.data.get(0).timestamp, rr_avg);
            datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").add(rr_ave);
        }
        rr_ave = datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").data.get(datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave").data.size() - 1);


        int c1 = 0;
        ArrayList<Integer> c2 = new ArrayList<Integer>();

        ArrayList<DataPoint> Rpeak_temp1 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp1").data;


        for (int i = 0; i < peaks.data.size(); i++) {
            if (Rpeak_temp1.size() == 0) {
                if (peaks.data.get(i).value > thr1 && peaks.data.get(i).value < (3.0 * sig_lev)) {
                    if (Rpeak_temp1.size() <= c1) {
                        Rpeak_temp1.add(new DataPoint(0, 0.0));
                    }
                    Rpeak_temp1.set(c1, peaks.data.get(i));
                    sig_lev = Core.ewma(peaks.data.get(i).value, sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                    if (c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i);
                    c1 += 1;
                } else if (peaks.data.get(i).value < thr1 && peaks.data.get(i).value > thr2) {
                    noise_lev = Core.ewma(peaks.data.get(i).value, noise_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                }

                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev); //TODO: Candidate for datastream
                thr2 = 0.5 * thr1; //TODO: Candidate for datastream

                rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
            } else {
                if (((peaks.data.get(i).timestamp - peaks.data.get(c2.get(c1 - 1)).timestamp) > 1.66 * rr_ave.value) && (i - c2.get(c1 - 1)) > 1) {
                    ArrayList<Double> searchback_array_inrange = new ArrayList<Double>();
                    ArrayList<Integer> searchback_array_inrange_index = new ArrayList<Integer>();

                    for (int j = c2.get(c1 - 1) + 1; j < i - 1; j++) {
                        if (peaks.data.get(i).value < 3.0 * sig_lev && peaks.data.get(i).value > thr2) {
                            searchback_array_inrange.add(peaks.data.get(i).value);
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
                        if (Rpeak_temp1.size() >= c1) {
                            Rpeak_temp1.add(new DataPoint(0, 0.0));
                        }
                        Rpeak_temp1.set(c1, peaks.data.get(c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index)));
                        sig_lev = Core.ewma(Rpeak_temp1.get(c1 - 1).value, sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                        if (c1 >= c2.size()) {
                            c2.add(0);
                        }
                        c2.set(c1, c2.get(c1 - 1) + searchback_array_inrange_index.get(searchback_max_index));
                        i = c2.get(c1 - 1) + 1;
                        c1 += 1;
                        thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                        thr2 = 0.5 * thr1;
                        rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
                        continue;
                    }
                } else if (peaks.data.get(i).value >= thr1 && peaks.data.get(i).value < (3.0 * sig_lev)) {
                    if (Rpeak_temp1.size() >= c1) {
                        Rpeak_temp1.add(new DataPoint(0, 0.0));
                    }
                    Rpeak_temp1.set(c1, peaks.data.get(i));
                    sig_lev = Core.ewma(peaks.data.get(i).value, sig_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                    if (c2.size() <= c1) {
                        c2.add(0);
                    }
                    c2.set(c1, i);
                    c1 += 1;
                } else if (peaks.data.get(i).value < thr1 && peaks.data.get(i).value > thr2) {
                    noise_lev = Core.ewma(peaks.data.get(i).value, noise_lev, AUTOSENSE.EWMA_ALPHA); //TODO: Candidate for datastream
                }
                thr1 = noise_lev + 0.25 * (sig_lev - noise_lev);
                thr2 = 0.5 * thr1;
                rr_ave = rr_ave_update(Rpeak_temp1, datastreams.get("org.md2k.cstress.data.ecg.peaks.rr_ave"));
            }
        }


        ArrayList<DataPoint> Rpeak_temp2 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp2").data;
        Rpeak_temp2.addAll(Rpeak_temp1); //Make a copy of the data vector

        boolean difference = false;

        while (!difference) {
            int length_Rpeak_temp2 = Rpeak_temp2.size();
            ArrayList<DataPoint> diffRpeak = new ArrayList<DataPoint>();
            for (int j = 1; j < Rpeak_temp2.size(); j++) {
                diffRpeak.add(new DataPoint(Rpeak_temp2.get(j).timestamp - Rpeak_temp2.get(j - 1).timestamp, Rpeak_temp2.get(j).value - Rpeak_temp2.get(j - 1).value));
            }

            ArrayList<DataPoint> comp1 = new ArrayList<DataPoint>();
            ArrayList<DataPoint> comp2 = new ArrayList<DataPoint>();
            ArrayList<Integer> eli_index = new ArrayList<Integer>();

            for (int j = 0; j < diffRpeak.size(); j++) {
                if (diffRpeak.get(j).timestamp < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER * frequency)) {
                    comp1.add(Rpeak_temp2.get(j));
                    comp2.add(Rpeak_temp2.get(j + 1));
                    if (comp1.get(comp1.size() - 1).value < comp2.get(comp2.size() - 1).value) {
                        eli_index.add(0);
                    } else {
                        eli_index.add(1);
                    }
                } else {
                    eli_index.add(-999999);
                }
            }

            for (int j = 0; j < diffRpeak.size(); j++) {
                if (diffRpeak.get(j).timestamp < (AUTOSENSE.RPEAK_INTERPEAK_MULTIPLIER * frequency)) {
                    Rpeak_temp2.set(j + eli_index.get(j), new DataPoint(0, -999999));
                }
            }

            for (Iterator<DataPoint> it = Rpeak_temp2.iterator(); it.hasNext(); ) {
                if (it.next().value == -999999) {
                    it.remove();
                }
            }

            difference = (length_Rpeak_temp2 == Rpeak_temp2.size());

        }


        ArrayList<DataPoint> Rpeak_temp3 = datastreams.get("org.md2k.cstress.data.ecg.peaks.temp3").data;
        if (Rpeak_temp2.size() > 0) {
            Rpeak_temp3.add(Rpeak_temp2.get(0));


            for (int k = 1; k < Rpeak_temp2.size() - 1; k++) {
                double maxValue = -1e9;


                double peaktime = Rpeak_temp2.get(k).timestamp;
                int windowStart = 0;
                int windowStop = peaks.data.size();
                for (int i = 0; i < peaks.data.size(); i++) {
                    if (peaks.data.get(i).timestamp < (peaktime - (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR))) {
                        windowStart = i;
                    }
                    if (peaks.data.get(i).timestamp > (peaktime + (int) Math.ceil(frequency / AUTOSENSE.RPEAK_BIN_FACTOR))) {
                        windowStop = i;
                        break;
                    }
                }


                int index = 0;
                DataPoint maxDP = new DataPoint(0, 0.0);
                try {
                    for (int j = windowStart + 1; j < windowStop; j++) {
                        if (peaks.data.get(j).value > maxValue) {
                            maxValue = peaks.data.get(j).value;
                            maxDP = new DataPoint(peaks.data.get(j));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Rpeak_temp3.add(maxDP);
                }
            }
        }

        datastreams.get("org.md2k.cstress.data.ecg.peaks.rpeaks").data.addAll(Rpeak_temp3);
        return "org.md2k.cstress.data.ecg.peaks.rpeaks";
    }

    /**
     * @param rpeak_temp1
     * @param rr_ave
     * @return
     */
    public DataPoint rr_ave_update(ArrayList<DataPoint> rpeak_temp1, DataStream rr_ave) { //TODO: Consider replacing this algorithm with something like and EWMA
        ArrayList<Long> peak_interval = new ArrayList<Long>();
        DataPoint result = new DataPoint(0, 0.0);
        if (rpeak_temp1.size() != 0) {
            for (int i = 1; i < rpeak_temp1.size(); i++) {
                peak_interval.add(rpeak_temp1.get(i).timestamp - rpeak_temp1.get(i - 1).timestamp);
            }

            if (peak_interval.size() >= AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE) {
                for (int i = peak_interval.size() - AUTOSENSE.PEAK_INTERVAL_MINIMUM_SIZE; i < peak_interval.size(); i++) {
                    result.value += peak_interval.get(i);
                }
                result.value /= 8.0;
                result.timestamp = rpeak_temp1.get(rpeak_temp1.size() - 1).timestamp;
                rr_ave.add(result);
            }
        }
        return result;
    }



    /**
     * Compute RR intervals
     */
    private String computeRR(DataStreams datastreams) {

        String rpeaks = detect_Rpeak(datastreams);

        if(datastreams.get(rpeaks).data.size() < 2 ) {
            System.err.println("Not enough peaks, aborting computation");
            return "org.md2k.error";
        }

        DataStream rpeaks_temp = datastreams.get("org.md2k.cstress.data.ecg.peaks.rpeaks");
        for (int i = 0; i < rpeaks_temp.data.size()-1; i++) {
            datastreams.get("org.md2k.cstress.data.ecg.rr_value").add(new DataPoint (rpeaks_temp.data.get(i).timestamp, (rpeaks_temp.data.get(i+1).timestamp - rpeaks_temp.data.get(i).timestamp) / 1000.0));
        }

        String rr_outlier = Core.detect_outlier_v2(datastreams);
        for(int i=0; i< datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.size(); i++) {
            if(datastreams.get(rr_outlier).data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").add(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i));
            }
        }

        double mu = datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").stats.getMean();
        double sigma = datastreams.get("org.md2k.cstress.data.ecg.rr_value.filtered").stats.getStandardDeviation();
        for (int i = 0; i < datastreams.get(rr_outlier).data.size(); i++) {
            if (datastreams.get(rr_outlier).data.get(i).value == AUTOSENSE.QUALITY_GOOD) {
                if (Math.abs(datastreams.get("org.md2k.cstress.data.ecg.rr_value").data.get(i).value - mu) > (3.0 * sigma)) {
                    datastreams.get(rr_outlier).data.get(i).value = AUTOSENSE.QUALITY_NOISE;
                } else {
                    datastreams.get(rr_outlier).data.get(i).value = AUTOSENSE.QUALITY_GOOD;
                }
            }
        }

        return "org.md2k.success";
    }
}
