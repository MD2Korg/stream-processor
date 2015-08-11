package md2k.mCerebrum.cStress.Statistics;


import java.util.HashMap;


/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * - Karen Hovsepian <karoaper@gmail.com>
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


public class BinnedStatistics {

    private long count;

    private int med;
    private double mad;
    private double low;
    private double high;

    private int numBins;
    private int minValue;
    private int maxValue;
    
    private HashMap<Integer,Integer> bins;
    //TODO: Needs a persistence and initialization layer

    /**
     * Class to keep track of running statistics.
     */
    public BinnedStatistics(int minValue, int maxValue) {
        this.count = 0;
        this.mad = 0;
        this.med = 0;
        this.low = 0;
        this.high = 0;
        this.numBins = maxValue - minValue + 1;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.bins = new HashMap<Integer, Integer>();

    }


    public void reset() {
        count = 0;
        mad = 0;
        med = 0;
        low = 0;
        high = 0;

        this.bins = new HashMap<Integer, Integer>();
    }

    public void add(int x) {
        if(!bins.containsKey(x)) {
            bins.put(x,0);
        }
        bins.put(x,bins.get(x)+1);
        count++;
    }


    private void computeMed() {
        int sum = 0;
        for (int i = minValue; i <= maxValue; i++) {
            if(bins.containsKey(i)) {
                sum += bins.get(i);
                if (sum > count / 2) {
                    this.med = i;
                    break;
                } else if (sum == count / 2) {
                    if (count % 2 == 0)
                        this.med = (2 * i + 1) / 2;
                    else
                        this.med = i;

                    break;
                }
            }
        }
    }

    private void computeMad() {
        HashMap<Integer,Integer> madbins = new HashMap<Integer, Integer>();

        for (int i = minValue; i <= maxValue; i++)
            if(bins.containsKey(i)) {
                int index = Math.abs(i - med);
                if(!madbins.containsKey(index)) {
                    madbins.put(index,0);
                }
                madbins.put(index, madbins.get(i));
            }

        int sum = 0;
        for (int i = 0; i < numBins - 1; i++) {
            if(madbins.containsKey(i)) {
                sum += madbins.get(i);
                if (sum > count / 2) {
                    mad = i;
                    break;
                } else if (sum == count / 2) {
                    if (count % 2 == 0)
                        mad = (2 * i + 1) / 2.0;
                    else
                        mad = i;

                    break;
                }
            }
        }
        this.low = med - 3.0 * mad;
        this.high = med + 3.0 * mad;
    }


    public double getMean() {
        double sum = 0;
        for (int i = 0; i < numBins; i++) {
            if(bins.containsKey(i)) {
                sum += bins.get(i) * i;
            }
        }
        return sum / count;
    }

    public double getStdev() {
        double sum = 0;
        double mean = getMean();
        for (int i = 0; i < numBins; i++) {
            if(bins.containsKey(i)) {
                sum += bins.get(i) * ((i - mean) * (i - mean));
            }
        }
        return Math.sqrt(sum / (count - 1));
    }


    public double getWinsorizedMean() {
        computeMed();
        computeMad();

        double sum = 0;
        for (int i = 0; i < numBins; i++) {
            if(bins.containsKey(i)) {
                sum += bins.get(i) * ((i > high) ? high : ((i < low) ? low : i ));
            }
        }
        return sum / count;
    }

    public double getWinsorizedStdev() {
        computeMed();
        computeMad();

        double winsorizedMean = getWinsorizedMean();

        double sum = 0;
        for (int i = 0; i < numBins; i++) {
            double temp = ((i > high) ? high : ((i < low) ? low : i));
            if(bins.containsKey(i)) {
                sum += bins.get(i) * (temp - winsorizedMean) * (temp - winsorizedMean);
            }
        }
        return Math.sqrt(sum / (count - 1));
    }

}
