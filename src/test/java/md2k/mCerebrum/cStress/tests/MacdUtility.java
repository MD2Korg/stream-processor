package md2k.mCerebrum.cStress.tests;

import java.util.ArrayList;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Hillol Sarker <hsarker@memphis.edu>
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
public class MacdUtility {

    public static double mean(double[] samples) throws Exception {
        return mean(samples, 0, samples.length);
    }

    public static double mean(double[] samples, int fromIndex, int toIndex) throws Exception {
        if (samples.length == 0) {
            throw new Exception("Empty array to calculate mean");
        }
        if (fromIndex < 0 || fromIndex >= samples.length || toIndex < 0 || toIndex >= samples.length) {
            throw new IndexOutOfBoundsException();
        }
        double meanValue = 0;
        for (int i = fromIndex; i <= toIndex; i++) {
            meanValue += samples[i];
        }
        return meanValue / (toIndex - fromIndex + 1);
    }

    static double[] getExponentialMovingAverage(double[] samples, int timePeriod) throws Exception {
        if (samples.length < timePeriod) {
            return new double[0];
        }
        double[] emaSamples = new double[samples.length - timePeriod + 1];
        emaSamples[0] = mean(samples, 0, timePeriod - 1);
        double alpha = 2.0 / (timePeriod + 1);

        for (int i = 1; i < (samples.length - timePeriod + 1); i++) {
            emaSamples[i] = samples[i + timePeriod - 1] * alpha + emaSamples[i - 1] * (1 - alpha);
        }

        return emaSamples;
    }

    static double[] getMacd(double[] emaFast, double[] emaSlow) {
        double[] macd = new double[emaSlow.length];
        int offset = emaFast.length - emaSlow.length;
        for (int i = 0; i < emaSlow.length; i++) {
            macd[i] = emaFast[offset + i] - emaSlow[i];
        }
        return macd;
    }

    public static ArrayList<Episode> getEpisodes(double[] samples, int a, int b, int c) throws Exception {
        double[] emaFast = getExponentialMovingAverage(samples, a); // samples.length-a+1
        double[] emaSlow = getExponentialMovingAverage(samples, b); // samples.length-b+1

        double[] macd = getMacd(emaFast, emaSlow); //emaSlow.length = samples.length-b+1
        double[] signal = getExponentialMovingAverage(macd, c); // macd.length-c+1 = emaSlow.length-c+1 = samples.length-b-c+2
        double[] histogram = getMacd(macd, signal); //signal.length = samples.length-b-c+2

        ArrayList<Episode> episodeArrayList = new ArrayList<Episode>();
        //Episode episode = null;
        Episode episode = new Episode(0, -1, -1); // Initial episode starts from beginning
        int offset = b + c - 2; // may be it should be -1
        for (int i = 1; i < histogram.length; i++) {
            if (histogram[i - 1] < 0 && histogram[i] > 0) {
                if (episode != null) {
                    episode.end = i + offset - 1;
                    episodeArrayList.add(episode);
                }
                episode = new Episode();
                episode.start = i + offset;
            } else if (histogram[i - 1] > 0 && histogram[i] < 0) {
                if (episode != null) {
                    episode.middle = i + offset;
                }
            }
        }
        if (episode.start != -1) {
            // End of episode
            episode.end = samples.length - 1;
            episodeArrayList.add(episode);
        }
        return episodeArrayList;
    }

    public static ArrayList<Episode> getEpisodes(double[] samples) throws Exception {
        return getEpisodes(samples, 7, 19, 2);
    }

    public static class Episode {
        public int start;
        public int middle;
        public int end;

        public Episode() {
            this.start = -1;
            this.middle = -1;
            this.end = -1;
        }

        public Episode(int start, int middle, int end) {
            this.start = start;
            this.middle = middle;
            this.end = end;
        }
    }
}
