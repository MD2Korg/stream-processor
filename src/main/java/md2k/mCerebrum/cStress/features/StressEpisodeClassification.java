package md2k.mCerebrum.cStress.features;

import md2k.mCerebrum.cStress.StreamConstants;
import md2k.mCerebrum.cStress.library.datastream.DataPointStream;
import md2k.mCerebrum.cStress.library.datastream.DataStreams;
import md2k.mCerebrum.cStress.library.signalprocessing.Smoothing;
import md2k.mCerebrum.cStress.library.structs.DataPoint;

import java.util.List;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
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

public class StressEpisodeClassification {

    public static final double macdParamFast = 7;
    public static final double macdParamSlow = 19;
    public static final double macdParamSignal = 2;

    public static final double thresholdYes = 0.44;
    public static final double thresholdNo = 0.29;

    public static final int stressProbabilitySmoothingWindow = 3; // 3 minute
    public static final int BUFFER_SIZE = 100;
    public static final int BUFFER_SIZE_SMALL = 10;

    public StressEpisodeClassification(DataStreams datastreams, long windowSize) {

        DataPointStream dsStressProbability = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY);

        DataPointStream dsStressProbabilityImputed = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_IMPUTED);
        dsStressProbabilityImputed.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsStressProbabilitySmoothed = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_SMOOTHED);
        dsStressProbabilitySmoothed.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream dsStressProbabilityAvailable = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY_AVAILABLE);
        dsStressProbabilityAvailable.setHistoricalBufferSize(BUFFER_SIZE);

        DataPointStream dsEmaFast = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_FAST);
        dsEmaFast.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsEmaSlow = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_SLOW);
        dsEmaSlow.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsEmaSignal = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_EMA_SIGNAL);
        dsEmaSignal.setHistoricalBufferSize(BUFFER_SIZE_SMALL);

        DataPointStream dsStressEpisodeClassification = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_CLASSIFICATION);
        dsStressEpisodeClassification.setHistoricalBufferSize(BUFFER_SIZE_SMALL);



        if (dsStressProbability.data.size() > 0) {
            DataPoint stressProbability = new DataPoint(dsStressProbability.getLatestTimestamp(), dsStressProbability.getLatestValue());
            dsStressProbabilityImputed.add(stressProbability);

            dsStressProbabilityAvailable.add(new DataPoint(dsStressProbability.getLatestTimestamp(), 1.0));
        } else {
            //Handle missing datapoint. Imputation by carry forwarding the last known value.
            long currentTimestamp = -1;
            List<DataPoint> imputedHistoryLast = dsStressProbabilityImputed.getHistoricalNValues(1);
            if (imputedHistoryLast.size() > 0) {
                if (imputedHistoryLast.get(0).timestamp == -1) {
                    currentTimestamp = -1;
                } else {
                    currentTimestamp = imputedHistoryLast.get(0).timestamp + windowSize; // 60000 should be replaced by windowSize
                }
            }
            if (currentTimestamp == -1) {
                return;
            }
            DataPoint stressProbability = new DataPoint(currentTimestamp, getLastHistoricalValue(dsStressProbability, -1));
            if (stressProbability.timestamp == -1 || stressProbability.value == -1) {
                return;
            }
            dsStressProbabilityImputed.add(stressProbability);

            dsStressProbabilityAvailable.add(new DataPoint(currentTimestamp, 0.0));
        }

        smoothViaHistoricalValues(dsStressProbabilitySmoothed, dsStressProbabilityImputed, stressProbabilitySmoothingWindow);

        DataPoint stressProbability = new DataPoint(dsStressProbabilitySmoothed.getLatestTimestamp(), dsStressProbabilitySmoothed.getLatestValue());

        double emaFastPrev;
        List<DataPoint> emaFastHistory = dsEmaFast.getHistoricalNValues(1);
        if (emaFastHistory.size() == 0) {
            emaFastPrev = stressProbability.value;
        } else {
            emaFastPrev = emaFastHistory.get(0).value;
        }
        double emaFast = Smoothing.ewma(stressProbability.value, emaFastPrev, 2.0 / (macdParamFast + 1));
        dsEmaFast.add(new DataPoint(stressProbability.timestamp, emaFast));

        double emaSlowPrev;
        List<DataPoint> emaSlowHistory = dsEmaSlow.getHistoricalNValues(1);
        if (emaSlowHistory.size() == 0) {
            emaSlowPrev = stressProbability.value;
        } else {
            emaSlowPrev = emaSlowHistory.get(0).value;
        }
        double emaSlow = Smoothing.ewma(stressProbability.value, emaSlowPrev, 2.0 / (macdParamSlow + 1));
        dsEmaSlow.add(new DataPoint(stressProbability.timestamp, emaSlow));

        double macdPrev = emaFastPrev - emaSlowPrev;
        double macd = emaFast - emaSlow;

        double emaSignalPrev;
        List<DataPoint> emaSignalHistory = dsEmaSignal.getHistoricalNValues(1);
        if (emaSignalHistory.size() == 0) {
            emaSignalPrev = stressProbability.value;
        } else {
            emaSignalPrev = emaSignalHistory.get(0).value;
        }
        double emaSignal = Smoothing.ewma(macd, emaSignalPrev, 2.0 / (macdParamSignal + 1));
        dsEmaSignal.add(new DataPoint(stressProbability.timestamp, emaSignal));

        //double histogramPrev = dsHistogram.getLatestValue();

        double histogramPrev = macdPrev - emaSignalPrev;
        double histogram = macd - emaSignal;
        if (histogramPrev < 0 && histogram > 0) {
            //Episode is ended; Started Increasing again
            long episodeStartTimestamp = getEpisodeStartTimestamp(datastreams);
            List<DataPoint> listAvailable = dsStressProbabilityAvailable.getHistoricalValues(episodeStartTimestamp);
            double sumAvailable = 0;
            for (DataPoint available : listAvailable) {
                sumAvailable += available.value;
            }
            double proportionAvailable = sumAvailable / listAvailable.size();
            if (proportionAvailable < .5) {
                //More than 50% data is lost.
                dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.Unknown.value));
            } else {
                List<DataPoint> listStressProbability = dsStressProbabilitySmoothed.getHistoricalValues(episodeStartTimestamp);
                double sumStressProbability = 0;
                for (DataPoint stressProbabilityDP : listStressProbability) {
                    sumStressProbability += stressProbabilityDP.value;
                }
                double stressDensity = sumStressProbability / listStressProbability.size();
                if (stressDensity >= thresholdYes) {
                    dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.YesStress.value));
                } else if (stressDensity <= thresholdNo) {
                    dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.NotStress.value));
                } else {
                    dsStressEpisodeClassification.add(new DataPoint(stressProbability.timestamp, StressEpisodeClass.Unsure.value));
                }
            }
        } else if (histogramPrev > 0 && histogram < 0) {
            //Episode in the middle; Started Decreasing
        }
    }

    public long getEpisodeStartTimestamp(DataStreams datastreams) {
        DataPointStream dsStressEpisodeClassification = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_STRESS_EPISODE_CLASSIFICATION);
        List<DataPoint> historicalNValues = dsStressEpisodeClassification.getHistoricalNValues(3);
        for (int i = 0; i < historicalNValues.size(); i++) {
            DataPoint dataPoint = historicalNValues.get(i);
            if (dataPoint.value == StressEpisodeClass.NotStress.value ||
                    dataPoint.value == StressEpisodeClass.Unsure.value ||
                    dataPoint.value == StressEpisodeClass.YesStress.value ||
                    dataPoint.value == StressEpisodeClass.Unknown.value) {
                return dataPoint.timestamp;
            }
        }
        return -1;
    }

    public double getLastHistoricalValue(DataPointStream dataPointStream, double defaultValue) {
        List<DataPoint> listHistory = dataPointStream.getHistoricalNValues(1);
        if (listHistory.size() == 0) {
            return defaultValue;
        }
        return listHistory.get(0).value;
    }

    public void smoothViaHistoricalValues(DataPointStream output, DataPointStream input, int n) {
        List<DataPoint> listHistoryDP = input.getHistoricalNValues(n);
        if (listHistoryDP.size() == 0) {
            return;
        }
        double sumValue = 0;
        for (DataPoint historyDP : listHistoryDP) {
            sumValue += historyDP.value;
        }
        output.add(new DataPoint(input.getLatestTimestamp(), sumValue / listHistoryDP.size()));
    }

    public static enum StressEpisodeClass {
        NotStress(0), Unsure(1), YesStress(2), Unknown(3), NotClassified(4);
        private double value;

        private StressEpisodeClass(double value) {
            this.value = value;
        }
    }

    /*
    public long getCurrentTimestamp() {
        Date date = new Date();
        return date.getTime();
    }
    */
}
