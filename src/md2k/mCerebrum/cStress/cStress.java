package md2k.mCerebrum.cStress;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.Autosense.AUTOSENSE_PACKET;
import md2k.mCerebrum.cStress.Features.AccelerometerFeatures;
import md2k.mCerebrum.cStress.Features.ECGFeatures;
import md2k.mCerebrum.cStress.Features.RIPFeatures;
import md2k.mCerebrum.cStress.Structs.DataPoint;

import java.util.ArrayList;


/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
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
public class cStress {

    long windowStartTime = -1;

    long windowSize;


    ArrayList<AUTOSENSE_PACKET> ECG;
    ArrayList<AUTOSENSE_PACKET> RIP;
    ArrayList<AUTOSENSE_PACKET> ACCELX;
    ArrayList<AUTOSENSE_PACKET> ACCELY;
    ArrayList<AUTOSENSE_PACKET> ACCELZ;

    SensorConfiguration sensorConfig;

    //Feature Computation Classes
    AccelerometerFeatures accelFeatures;
    ECGFeatures ecgFeatures;
    RIPFeatures ripFeatures;


    public cStress(long windowSize) {
        this.windowSize = windowSize;
        config();
        resetBuffers();

    }


    private void config() {
        //Set cStress configurations here

        sensorConfig = new SensorConfiguration();
        sensorConfig.add("RIP", 64.0 / 3.0, AUTOSENSE.CHEST_RIP);
        sensorConfig.add("ECG", 64.0, AUTOSENSE.CHEST_ECG);
        sensorConfig.add("ACCELX", 64.0 / 6.0, AUTOSENSE.CHEST_ACCEL_X);
        sensorConfig.add("ACCELY", 64.0 / 6.0, AUTOSENSE.CHEST_ACCEL_Y);
        sensorConfig.add("ACCELZ", 64.0 / 6.0, AUTOSENSE.CHEST_ACCEL_Z);

    }

//    private Object read_process_memphis_formatteddata(long winlen) {
//
//        Object D = process_data();
//
//        D = detect_RR(this.ECG);
//
//        Object peakvalley = detect_peakvalley_v2(this.RIP);
//
//        Object themodel = get_model(0, 10);
//        Object W1 = segmentinwindow(themodel); //Segment based on something other than time
//
//
//        Object F2 = window2feature_accelerometer_main(this.ACCELX, this.ACCELY, this.ACCELZ);
//
////        if isempty(normalization_params)
////        if strcmp(normsource,'accel')
////        normalization_times = filter_inds_by_activity(F2);
//        Object normalization_times = filter_inds_by_activity(F2);
//
////        else
////        rr_times = D.sensor(SENSOR.R_ECGID).rr_timestamp(D.sensor(SENSOR.R_ECGID).rr_outlier==0);
////        rr_ints = D.sensor(SENSOR.R_ECGID).rr_value(D.sensor(SENSOR.R_ECGID).rr_outlier==0);
////        params = get_baseline_params();
//        double rr_ints = 0;
//        Object rr_times = null;
//        Object params = get_baseline_params();
////        [~,hard_labels,~,~,~,~] = hierarchical_mixture_segmentation_model({60./rr_ints},{rr_times},params,[],[]);
//        Object foo = hierarchical_mixture_segmentation_model(60/rr_ints,rr_times,params);
////        baseline_like_region_inds = find(hard_labels == min(hard_labels));
////        breaks = find(diff(baseline_like_region_inds)>1);
////        normalization_times = [rr_times(baseline_like_region_inds(1)),0];
////        for j=1:length(breaks)
////        normalization_times(end,2) = rr_times(baseline_like_region_inds(breaks(j)));
////        normalization_times(end+1,1) = rr_times(baseline_like_region_inds(breaks(j)+1));
////        end
////        normalization_times(end,2) = rr_times(baseline_like_region_inds(end));
////        end
////                D = normalize_rrintervals(D,normalization_times);
//        D = normalize_rrintervals(D,normalization_times);
////        else
////        D = normalize_rrintervals_fromparams(D,normalization_params{2});
//        Object normalization_params = null;
//        D = normalize_rrintervals_fromparams(D,normalization_params);
////        end
//
//        themodel = get_model(1,0);
//        Object WRIP = segmentbycycle(themodel, this.RIP);
//
//        Object FRIP = window2feature_rip_main(WRIP,themodel, this.RIP);
//
//        //W1 = segmentinwindow(themodel,D,W1);
//        Object F1 = null;
//         F1=window2feature_ecg_main(W1,F1);
//        F1=window2feature_accelerometer_main(W1,F1);
//
////        if isempty(normalization_params)
////                [winsorized_mean,winsorized_stdev] = get_winsorized_meanvar(FRIP.sensor(FEATURE.RIPID).feature,normalization_times);%(:,1:end-2));
//                        Object winsorized = get_winsorized_meanvar(FRIP,normalization_times);
////        FRIP.sensor(FEATURE.RIPID).feature(:,1:end-2) = (FRIP.sensor(FEATURE.RIPID).feature(:,1:end-2)-repmat(winsorized_mean,nr,1))./repmat(winsorized_stdev,nr,1);
//        F1 = compute_statistics_from_wins(F1, FRIP, W1, WRIP);
////        inds = [];
////        for j=1:size(normalization_times,1)
////        inds = [inds; find(F1.sensor(4).statistical_feature(:,end-1) >= normalization_times(j,1) & F1.sensor(4).statistical_feature(:,end-1) <= normalization_times(j,2) | normalization_times(j,1) >= F1.sensor(4).statistical_feature(:,end-1) & normalization_times(j,1) <= F1.sensor(4).statistical_feature(:,end))];
////        end
////        F1.sensor(4).statistical_feature(:,1)=(F1.sensor(4).statistical_feature(:,1)-nanmean(F1.sensor(4).statistical_feature(inds,1)))/sqrt(nanvar(F1.sensor(4).statistical_feature(inds,1)));
////        F1.sensor(4).statistical_feature(:,2)=(F1.sensor(4).statistical_feature(:,2)-nanmean(F1.sensor(4).statistical_feature(inds,2)))/sqrt(nanvar(F1.sensor(4).statistical_feature(inds,2)));
////        else
////        FRIP.sensor(FEATURE.RIPID).feature(:,1:end-2) = (FRIP.sensor(FEATURE.RIPID).feature(:,1:end-2)-repmat(normalization_params{3}{1},nr,1))./repmat(normalization_params{3}{2},nr,1);
////        F1 = compute_statistics_from_wins(F1,FRIP,W1,WRIP);
////        F1.sensor(4).statistical_feature(:,1)=(F1.sensor(4).statistical_feature(:,1)-normalization_params{4}{1}(1))/normalization_params{4}{1}(2);
////        F1.sensor(4).statistical_feature(:,2)=(F1.sensor(4).statistical_feature(:,2)-normalization_params{4}{2}(1))/normalization_params{4}{2}(2);
////        end
//
//        return D;
//    }
//
//    private Object window2feature_accelerometer_main(Object w1, Object f1) {
//        return null;
//    }
//
//    private Object window2feature_ecg_main(Object w1, Object f1) {
//        return null;
//    }
//
//    private Object segmentinwindow(Object themodel) {
//        return null;
//    }
//
//    private Object get_winsorized_meanvar(Object frip, Object normalization_times) {
////        global NORMALIZATION
////
////        inds = [];
////        for i=1:size(normalization_times,1)
////        inds = [inds; find(x(:,end-1) >= normalization_times(i,1) & x(:,end) <= normalization_times(i,2))];
////        end
////                x1 = x(inds,1:end-2);
////
////        nc = size(x1,2);
////        for j=1:nc
////
////
////                med = nanmedian(x1(:,j));
////        mad = nanmedian(abs(x1(:,j)-med));
////
////        low = med-NORMALIZATION.BETA*mad;
////        high = med+NORMALIZATION.BETA*mad;
////        if NORMALIZATION.TRIM
////        x1(x1(:,j)>high,j) = NaN;
////        x1(x1(:,j)<low,j) = NaN;
////        else
////        x1(x1(:,j)>high,j) = high;
////        x1(x1(:,j)<low,j) = low;
////        end
////                end
////
////        winsorized_mean = nanmean(x1);
////        winsorized_stdev = sqrt(nanvar(x1));
//        return null;
//    }
//
//    private Object compute_statistics_from_wins(Object f1, Object frip, Object w1, Object wrip) {
////        global FEATURE
////        global SENSOR
////        F.sensor(FEATURE.RIPSTATID).statistical_feature = nan(length(W.starttimestamp),FEATURE.RIPSTAT.FEATURENO);
////        F.sensor(FEATURE.RIPSTATID).featurename = FEATURE.RIPSTAT.NAME;
////        FR = FEATURE.RIPSTAT;
////        for i=1:length(W.starttimestamp)
////        inds = find(FCYC.sensor(FEATURE.RIPID).feature(:,end-1)>=W.starttimestamp(i) & FCYC.sensor(FEATURE.RIPID).feature(:,end-1)<W.endtimestamp(i));
////        if isempty(inds)
////        continue
////                end
////        features = nan(1,FR.FEATURENO-2);
////
////        features(FR.BREATHRATE) = double(length(inds));
////        temp = 0;
////        for k=inds'
////        temp = temp + (WCYC.sensor(SENSOR.R_RIPID).window(k).timestamp(WCYC.sensor(SENSOR.R_RIPID).window(k).peakindex)-WCYC.sensor(SENSOR.R_RIPID).window(k).timestamp(1))/1000*(WCYC.sensor(SENSOR.R_RIPID).window(k).sample(WCYC.sensor(SENSOR.R_RIPID).window(k).peakindex)-WCYC.sensor(SENSOR.R_RIPID).window(k).sample(1))/2;
////        end
////
////        features(FR.MINUTEVENT) = double(temp*length(inds));
////
////
////
////        exprV1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.EXPRDURATION);
////        exprV = exprV1(~isnan(exprV1));
////        if length(exprV)>=length(exprV1)*(1-FEATURE.MAXMISSING)
////        features(FR.EXPRDURQUARTDEV) = 0.5*(prctile(exprV,75)-prctile(exprV,25));
////        features(FR.EXPRDURMEAN) = mean(exprV);
////        features(FR.EXPRDURMEDIAN) = median(exprV);
////        features(FR.EXPRDUR80PERCENT) = double(prctile(exprV,80));
////        end
////
////                inspV1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.INSPDURATION);
////        inspV = inspV1(~isnan(inspV1));
////        if length(inspV)>=length(inspV1)*(1-FEATURE.MAXMISSING)
////        features(FR.INSPDURQUARTDEV) = 0.5*(prctile(inspV,75)-prctile(inspV,25));
////        features(FR.INSPDURMEAN) = mean(inspV);
////        features(FR.INSPDURMEDIAN) = median(inspV);
////        features(FR.INSPDUR80PERCENT) = double(prctile(inspV,80));
////        end
////
////                respV1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.RESPDURATION);
////        respV = respV1(~isnan(respV1));
////        if length(respV)>=length(respV1)*(1-FEATURE.MAXMISSING)
////        features(FR.RESPDURQUARTDEV) = 0.5*(prctile(respV,75)-prctile(respV,25));
////        features(FR.RESPDURMEAN) = mean(respV);
////        features(FR.RESPDURMEDIAN) = median(respV);
////        features(FR.RESPDUR80PERCENT) = double(prctile(respV,80));
////        end
////
////                ieRatio1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.IERATIO);
////        ieRatio = ieRatio1(~isnan(ieRatio1));
////        if length(ieRatio)>=length(ieRatio1)*(1-FEATURE.MAXMISSING)
////        features(FR.IERATIOQUARTDEV)= 0.5*(prctile(ieRatio,75)-prctile(ieRatio,25));
////        features(FR.IERATIOMEAN)= mean(ieRatio);
////        features(FR.IERATIOMEDIAN)= median(ieRatio);
////        features(FR.IERATIO80PERCENT)= prctile(ieRatio,80);
////        end
////
////                rsaV1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.RSA);
////        rsaV = rsaV1(~isnan(rsaV1));
////        if length(rsaV)>=length(rsaV1)*(1-FEATURE.MAXMISSING)
////        features(FR.RSAQUARTDEV)= 0.5*(prctile(rsaV,75)-prctile(rsaV,25));
////        features(FR.RSAMEAN)= mean(rsaV);
////        features(FR.RSAMEDIAN)= median(rsaV);
////        features(FR.RSA80PERCENT)= prctile(rsaV,80);
////        end
////
////                strchV1 = FCYC.sensor(FEATURE.RIPID).feature(inds,FEATURE.RIP.STRETCH);
////        strchV = strchV1(~isnan(strchV1));
////        if length(strchV)>=length(strchV1)*(1-FEATURE.MAXMISSING)
////        features(FR.STRETCHQUARTDEV)= 0.5*(prctile(strchV,75)-prctile(strchV,25));
////        features(FR.STRETCHMEAN)= mean(strchV);
////        features(FR.STRETCHMEDIAN)= median(strchV);
////        features(FR.STRETCH80PERCENT)= prctile(strchV,80);
////        end
////        F.sensor(FEATURE.RIPSTATID).statistical_feature(i,:) = [features,W.starttimestamp(i),W.endtimestamp(i)];
////        end
//
//        return null;
//    }
//
//    private Object window2feature_rip_main(Object wrip, Object themodel, ArrayList<AutosenseSample> rip) {
////        function [F,indivfeatures]=window2feature_rip_main(W, F,MODEL,D)
////        global SENSOR FEATURE
////                numofwindow = length(W.sensor(SENSOR.R_RIPID).window);
////        if strcmp(MODEL.WINDOWTYPE,'cycle')
////        F.sensor(FEATURE.RIPID).feature = nan(numofwindow,FEATURE.RIP.FEATURENO);
////        else
////        F.sensor(FEATURE.RIPSTATID).statistical_feature = nan(numofwindow,FEATURE.RIPSTAT.FEATURENO);
////        end
////        if nargout == 2
////        indivfeatures = [];
////        end
////        for i=1:numofwindow
////        if strcmp(MODEL.WINDOWTYPE,'cycle')
////        F.sensor(FEATURE.RIPID).window(i) = W.sensor(SENSOR.R_RIPID).window(i);
////        if isempty(W.sensor(SENSOR.R_RIPID).window(i).peakindex)
////        continue;
////        end
////        if nargin == 4
////        F.sensor(FEATURE.RIPID).feature(i,:) = ripfeature_extraction(W.sensor(SENSOR.R_RIPID).window(i),D.sensor(SENSOR.R_ECGID));
//        Object ripfeature = ripfeature_extraction(new Object());
////        else
////        F.sensor(FEATURE.RIPID).feature(i,:) = ripfeature_extraction(W.sensor(SENSOR.R_RIPID).window(i));
////        end
////        F.sensor(FEATURE.RIPID).feature(i,FEATURE.RIP.STARTTIMESTAMP)=W.sensor(SENSOR.R_RIPID).window(i).timestamp(1);
////        F.sensor(FEATURE.RIPID).feature(i,FEATURE.RIP.ENDTIMESTAMP)=W.sensor(SENSOR.R_RIPID).window(i).timestamp(end);
////        else
////        if isempty(W.sensor(SENSOR.R_RIPID).window(i).peakvalley_timestamp)
////        continue;
////        end
////        F.sensor(FEATURE.RIPSTATID).window(i) = W.sensor(SENSOR.R_RIPID).window(i);
////        if nargin == 4
////                [features,rsaV] = ripfeature_extraction_by_window(W.sensor(SENSOR.R_RIPID).window(i),D.sensor(SENSOR.R_ECGID).rr_value(D.sensor(SENSOR.R_ECGID).rr_outlier==0),D.sensor(SENSOR.R_ECGID).rr_timestamp(D.sensor(SENSOR.R_ECGID).rr_outlier==0));
//            ripfeature = ripfeature_extraction_by_window(new Object(), new Object());
////        else
////        [features,rsaV] = ripfeature_extraction_by_window(W.sensor(SENSOR.R_RIPID).window(i),W.sensor(SENSOR.R_ECGID).window(i).rr_sample(W.sensor(SENSOR.R_ECGID).window(i).rr_outlier==0),W.sensor(SENSOR.R_ECGID).window(i).rr_timestamp(W.sensor(SENSOR.R_ECGID).window(i).rr_outlier==0));
////        end
////        indivfeatures = [indivfeatures;rsaV];
////        F.sensor(FEATURE.RIPSTATID).statistical_feature(i,:) = features;
////        F.sensor(FEATURE.RIPSTATID).statistical_feature(i,FEATURE.RIPSTAT.STARTTIMESTAMP)=W.sensor(SENSOR.R_RIPID).window(i).timestamp(1);
////        F.sensor(FEATURE.RIPSTATID).statistical_feature(i,FEATURE.RIPSTAT.ENDTIMESTAMP)=W.sensor(SENSOR.R_RIPID).window(i).timestamp(end);
////        end
////
////                end
////        if strcmp(MODEL.WINDOWTYPE,'cycle')
////        if 0
////        F.sensor(FEATURE.RIPID).feature = ripfeature_FD(F.sensor(FEATURE.RIPID).feature);
////        F.sensor(FEATURE.RIPID).feature = ripfeature_MXMDSTRETCHRATIO(F.sensor(FEATURE.RIPID).feature);
////        F.sensor(FEATURE.RIPID).feature = ripfeature_5cycle(F.sensor(FEATURE.RIPID).feature);
////        end
////        F.sensor(FEATURE.RIPID).featurename = FEATURE.RIP.NAME;
////        else
////        F.sensor(FEATURE.RIPSTATID).featurename = FEATURE.RIPSTAT.NAME;
////        end
//
//        return null;
//    }
//
//    private Object ripfeature_extraction_by_window(Object o, Object o1) {
////        function [features,rsaVs]=ripfeature_extraction_by_window(seg,rr_sample,rr_timestamp)
////        global FEATURE
////        FR = FEATURE.RIPSTAT;
////        features = zeros(1,FR.FEATURENO);
////
////
////        %seg.peakvalley_timestamp
////                %seg.peakvalley_sample
////                %pause
////        inspV = double(InspirationDurationCalculateWin(seg.peakvalley_timestamp));
////        exprV = double(ExpirationDurationCalculateWin(seg.peakvalley_timestamp));
////        respV = double(RespirationDurationCalculateWin(seg.peakvalley_timestamp));
////
////        %inspV = inspVs(inspVs(:,1)>0,1);
////        %exprV = exprVs(exprVs(:,1)>0,1);
////        %respV = respVs(respVs(:,1)>0,1);
////
////
////        features(FR.BREATHRATE) = double(length(inspV));
////        features(FR.MINUTEVENT) = double(BreathMinVent(seg.peakvalley_sample,seg.peakvalley_timestamp));
////
////        features(FR.EXPRDURQUARTDEV) = 0.5*(prctile(exprV,75)-prctile(exprV,25));
////        features(FR.EXPRDURMEAN) = mean(exprV);
////        features(FR.EXPRDURMEDIAN) = median(exprV);
////        features(FR.EXPRDUR80PERCENT) = double(prctile(exprV,80));
////
////        features(FR.INSPDURQUARTDEV) = 0.5*(prctile(inspV,75)-prctile(inspV,25));
////        features(FR.INSPDURMEAN) = mean(inspV);
////        features(FR.INSPDURMEDIAN) = median(inspV);
////        features(FR.INSPDUR80PERCENT) = double(prctile(inspV,80));
////
////        features(FR.RESPDURQUARTDEV) = 0.5*(prctile(respV,75)-prctile(respV,25));
////        features(FR.RESPDURMEAN) = mean(respV);
////        features(FR.RESPDURMEDIAN) = median(respV);
////        features(FR.RESPDUR80PERCENT) = double(prctile(respV,80));
////
////        n = min(length(inspV),length(exprV));
////        ieRatio = inspV(1:n)./exprV(1:n);
////        %if sum(ieRatio<0)>0
////                %    ieRatio
////                %    pause
////                %end
////        features(FR.IERATIOQUARTDEV)= 0.5*(prctile(ieRatio,75)-prctile(ieRatio,25));
////        features(FR.IERATIOMEAN)= mean(ieRatio);
////        features(FR.IERATIOMEDIAN)= median(ieRatio);
////        features(FR.IERATIO80PERCENT)= prctile(ieRatio,80);
////
////        rsaV=RSACalculate(seg.peakvalley_timestamp,seg.peakvalley_sample,rr_sample,rr_timestamp);
////
////        if isempty(rsaV)
////        rsaV = nan;
////        else
////
////        features(FR.RSAQUARTDEV)= 0.5*(prctile(rsaV,75)-prctile(rsaV,25));
////        features(FR.RSAMEAN)= mean(rsaV);
////        features(FR.RSAMEDIAN)= median(rsaV);
////        features(FR.RSA80PERCENT)= prctile(rsaV,80);
////
////        strchV = double(BreathStretchCalculate(seg.sample,seg.timestamp,seg.peakvalley_sample,seg.peakvalley_timestamp));
////        features(FR.STRETCHQUARTDEV)= 0.5*(prctile(strchV,75)-prctile(strchV,25));
////        features(FR.STRETCHMEAN)= mean(strchV);
////        features(FR.STRETCHMEDIAN)= median(strchV);
////        features(FR.STRETCH80PERCENT)= prctile(strchV,80);
////
////        end
//        return null;
//    }
//
//    private Object ripfeature_extraction(Object o) {
////        function features=ripfeature_extraction(seg,ecg)
////        global FEATURE
////        FR = FEATURE.RIP;
////        features = nan(1,FR.FEATURENO);
////
////        strchR=[];
////        if length(seg.sample)==0
////        mn=0;
////        pause
////        else
////        mn=min(seg.sample);
////        end
////                mx=seg.sample(seg.peakindex);
////
////        strchV = mx - mn;
////        strchMX = mx - FR.MEDIAN;
////        strchMN = FR.MEDIAN - mn;
////        [inspV, ~]=InspirationDurationCalculate(seg.sample,seg.timestamp,seg.peakindex);
////        [exprV, ~]=ExpirationDurationCalculate(seg.sample,seg.timestamp,seg.peakindex);
////        [respV, ~]=RespirationDurationCalculate(seg.sample,seg.timestamp);
////        if nargin == 2
////        rsa = double(RSACalculateCycle(seg.timestamp,ecg.rr_value(ecg.rr_outlier==0),ecg.rr_timestamp(ecg.rr_outlier==0)));
////        if ~isempty(rsa) && rsa ~= 0
////        features(FR.RSA)= rsa;
////        end
////                end
////
////        features(FR.STRETCH) = double(strchV);
////
////        ieRatio=double(inspV)./double(exprV);
////        if isinf(ieRatio) && 0
////        features(FR.IERATIO) = nan;
////        else
////        features(FR.IERATIO) = double(ieRatio);
////        end
////
////        features(FR.INSPDURATION) = double(inspV);
////        features(FR.EXPRDURATION) = double(exprV);
////        features(FR.RESPDURATION) = double(respV);
////        features(FR.STRETCHUP)= double(strchMX);
////        features(FR.STRETCHDOWN)= double(strchMN);
////        end
//
//        return null;
//    }
//
//    private Object segmentbycycle(Object themodel, ArrayList<AutosenseSample> rip) {
////        function W=segmentbycycle(MODEL,D,W)
////        global SENSOR WINDOW
////
////                ind=0;
////        for i=1:2:size(D.sensor(SENSOR.R_RIPID).peakvalley_sample,2)-2
////        starttimestamp=D.sensor(SENSOR.R_RIPID).peakvalley_timestamp(i);
////        endtimestamp=D.sensor(SENSOR.R_RIPID).peakvalley_timestamp(i+2);
////        index=find(D.sensor(SENSOR.R_RIPID).timestamp>=starttimestamp & D.sensor(SENSOR.R_RIPID).timestamp<=endtimestamp);
////        if isempty(index)
////        continue;
////        end;
////        ind=ind+1;
////        W.sensor(SENSOR.R_RIPID).window(ind).sample =D.sensor(SENSOR.R_RIPID).sample(index);
////        W.sensor(SENSOR.R_RIPID).window(ind).timestamp =D.sensor(SENSOR.R_RIPID).timestamp(index);
////
////        a=find(W.sensor(SENSOR.R_RIPID).window(ind).timestamp==D.sensor(SENSOR.R_RIPID).peakvalley_timestamp(i+1));
////        W.sensor(SENSOR.R_RIPID).window(ind).peakindex = a;
////        W.sensor(SENSOR.R_RIPID).window(ind).valleyindex = 1;
////
////        W.starttimestamp=W.sensor(SENSOR.R_RIPID).window(ind).timestamp(1);
////        W.endtimestamp=W.sensor(SENSOR.R_RIPID).window(ind).timestamp(end);
////        end
////                end
//
//        return null;
//    }
//
//    private Object normalize_rrintervals_fromparams(Object d, Object normalization_params) {
////        function D=normalize_rrintervals_fromparams(D,normalization_params,type)
////        global SENSOR
////        global NORMALIZATION
////
////        if nargin < 3
////        type = 1;
////        end
////                temp = D.sensor(SENSOR.R_ECGID);
////        x = temp.rr_value(temp.rr_outlier == 0);
////
////        med = nanmedian(x);
////        mad = nanmedian(abs(x-med));
////
////
////        low = med-NORMALIZATION.BETA*mad;
////        high = med+NORMALIZATION.BETA*mad;
////        if NORMALIZATION.TRIM
////                x = x(x<=high & x >= low);
////        else
////        x(x>high) = high;
////        x(x<low) = low;
////        end
////
////        if type == 2
////        D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier == 0)=(x-normalization_params(1))/normalization_params(2);
////        else
////        D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier == 0)=(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier == 0)-normalization_params(1))/normalization_params(2);
////        end
////                end
//
//        return null;
//    }
//
//    private Object normalize_rrintervals(Object d, Object normalization_times) {
////        function D=normalize_rrintervals(D,normalization_times,type)
////        global SENSOR
////        global NORMALIZATION
////
////        if nargin < 3
////        type = 1;
////        end
////                temp = D.sensor(SENSOR.R_ECGID);
////        x_whole = temp.rr_value(temp.rr_outlier ==0);
////
////        if nargin >= 2
////        inds = [];
////        for i=1:size(normalization_times,1)
////        inds = [inds, find(temp.rr_timestamp >= normalization_times(i,1) & temp.rr_timestamp <= normalization_times(i,2))];
////        end
////
////                x = temp.rr_value(inds);
////        x = x(temp.rr_outlier(inds) ==0);
////        else
////        x = x_whole;
////        end
////
////                med = nanmedian(x);
////        mad = nanmedian(abs(x-med));
////
////        med_whole = nanmedian(x_whole);
////        mad_whole = nanmedian(abs(x_whole-med_whole));
////
////        low = med-NORMALIZATION.BETA*mad;
////        high = med+NORMALIZATION.BETA*mad;
////        if NORMALIZATION.TRIM
////                x = x(x<=high & x >= low);
////        else
////        x(x>high) = high;
////        x(x<low) = low;
////        end
////
////                low_whole = med_whole-NORMALIZATION.BETA*mad_whole;
////        high_whole = med_whole+NORMALIZATION.BETA*mad_whole;
////        if NORMALIZATION.TRIM
////                x_whole = x_whole(x_whole<=high_whole & x_whole >= low_whole);
////        else
////        x_whole(x_whole>high) = high_whole;
////        x_whole(x_whole<low) = low_whole;
////        end
////
////
////        if type == 1
////                %    D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)=(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)-nanmean(x))/sqrt(nanvar(x));
////        D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)=(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)-nanmean(x))/sqrt(nanvar(x_whole));
////        elseif type == 2
////        D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)=(x-nanmean(x))/sqrt(nanvar(x));
////        else
////        D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)=(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)-nanmean(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)))/sqrt(nanvar(D.sensor(SENSOR.R_ECGID).rr_value(temp.rr_outlier ==0)));
////        end
////                end
//
//        return null;
//    }
//
//    private Object hierarchical_mixture_segmentation_model(double v, Object rr_times, Object params) {
////        function [soft_labels,hard_labels,likelihoods,mixture_params,logL,inputsegments] = hierarchical_mixture_segmentation_model(data,time,params,model_params,inputsegments)
////
////
////
////        K1 = params.K1;
////        K2 = params.K2;
////        tol = params.tol;
////        maxiter = params.maxiter;
////        doplot = params.doplot;
////        %nu = params.priors.nu;
////        %phi = params.priors.phi;
////        gammaval = params.priors.gammaval;
////        %mu0 = params.priors.mu0;
////        z0 = params.priors.z0;
////        lambda0 = params.priors.lambda0;
////        %sigma0 = params.priors.sigma0;
////        alpha0 = params.priors.alpha0;
////        c = params.c;
////
////        binsize = 15;
////
////        max_num_elements = 200000000;
////
////        combineddata = [];
////        combinedtime = [];
////        for i=1:length(data)
////        combineddata = [combineddata;data{i}'];
////        combinedtime = [combinedtime;time{i}'];
////        end
////                data = combineddata;
////        time = combinedtime;
////
////
////        [newdata] = bin_data(data,time,max_num_elements,binsize,params.segmentparams.max_segment_size);
////        data = newdata(:,1);
////        time = newdata(:,2);
////
////
////        if ~isempty(model_params)
////        have_initial_params = 1;
////        lambdas = model_params{3};
////        alphas = model_params{6};
////        mus = model_params{1};
////        sigs = model_params{2};
////        musT = model_params{4};
////        sigsT = model_params{5};
////
////        K1 = length(musT);
////        N = length(data);
////        else
////        have_initial_params = 0;
////
////        if isempty(inputsegments)
////                [model_params,inputsegments,time,data] = get_initial_values(data,time,params);
////        else
////        [model_params,inputsegments,time,data] = get_initial_values(data,time,params,inputsegments);
////        end
////                lambdas = model_params{1};
////        alphas = model_params{2};
////        P = model_params{3};
////        Q = model_params{4};
////        K1 = size(P,2);
////        N = length(data);
////        end
////
////
////                [data,timedata,timescales,datascales] = scale_data(data,time);
////
////        iter = 1;
////        minT = min(time);
////        if doplot
////                minV = min(data);
////        maxV = max(data);
////        avgV = mean(data);
////        f1 = figure;
////        f2 = figure;
////        subplot(3,1,1)
////        h1 = scatter(time-minT,data,'.','DisplayName','RRints');
////        subplot(3,1,2)
////        h2 = scatter(time-minT,data,'.','DisplayName','RRints');
////        hold on
////        subplot(3,1,3)
////        h3 = scatter(time-minT,data,'.','DisplayName','RRints');
////        hold on
////
////        colorsK2 = ['r','b','g','m'];
////        colorsK1 = distinguishable_colors(K2);
////
////        end
////
////
////
////        while iter <= maxiter
////        if iter > 1 || have_initial_params
////        Q = gaussian(data,mus,sigs,N,K2);
////        P = gaussian(timedata,musT,sigsT,N,K1);
////        end
////
////
////                A = P.*lambdas(ones(1,N),:);
////        C = A.*(Q*alphas');
////        L = sum(C,2);
////
////        logL(iter) = sum(log(L));
////
////        if doplot && iter > 2
////        figure(f1);
////        plot(iter,logL(iter)-logL(iter-1),'.');
////        hold on
////        plot([1,iter],[0,0],'--');
////        lims = ylim;
////        ylim([-5,lims(2)]);
////        end
////
////        if (iter > 2 && logL(iter)-logL(iter-1) < abs(logL(iter))*tol)
////            break;
////        end
////
////                D = bsxfun(@rdivide,C,L+eps);
////
////
////        sumA = sum(A,2);
////        [~,postlabel] = max(bsxfun(@rdivide,A,sumA)');
////                [~,postlabel3] = max(alphas(postlabel,:)');
////        if doplot && (mod(iter,50) == 0 || iter == 1 || iter == maxiter)
////
////        figure(f2);
////
////        subplot(3,1,1);
////        [~,postlabel] = max(D');
////                %change the colors in a scatter plot
////        set(h1,'CData',postlabel);
////
////
////
////        subplot(3,1,3)
////
////        set(h3,'CData',postlabel3);
////        end
////
////                sumD = sum(D);
////        musT = (timedata'*D)./sumD;
////                %    sigsT{iter+1} =  (diag(((bsxfun(@minus,timedata,musT{iter+1})).^2)'*D)'+2*gammaval)./(sumD+3+2*z0);
////        sigsT =  diag(((bsxfun(@minus,timedata,musT)).^2)'*D)'./sumD;
////        lambdas = (sumD+lambda0-1)/(N+K1*lambda0-K1);
////
////        %    G = A * alphas{iter};
////        %    E = Q .* G;
////        %    F = E ./ L(:,ones(1,K2));
////        F = bsxfun(@rdivide,(Q .* (A * alphas)),L+eps);
////        if doplot && (mod(iter,50) == 0 || iter == 1 || iter == maxiter)
////
////        figure(f2);
////        if 0
////        subplot(3,1,2);
////
////        H = bsxfun(@minus,alphas,reshape(Q',[1,K2,N]));
////        sumH = sum(H,2);
////        H = bsxfun(@rdivide,H,sumH);
////        [~,postlabel] = max(H(1,:,:));
////
////        set(h2,'CData',postlabel);
////
////        [~,postlabel] = max(H(2,:,:));
////        end
////                [~,postlabel] = max(F');
////        subplot(3,1,2);
////        set(h2,'CData',postlabel);
////
////        end
////
////                sumF = sum(F);
////        %   H = bsxfun(@minus,alphas{iter},reshape(Q',[1,K2,N]))
////                %   J = bsxfun(@times,reshape(A',[K1,1,N]),bsxfun(@minus,alphas{iter},reshape(Q',[1,K2,N])))
////        M = bsxfun(@rdivide,bsxfun(@times,reshape(A',[K1,1,N]),bsxfun(@times,alphas,reshape(Q',[1,K2,N]))),reshape(L,[1,1,N]));
////
////        if K2 < 3
////        signs = [-1,1];
////        if iter > 1
////        mus = (data'*F+signs.*sigs*c)./sumF;
////        else
////        mus = (data'*F)./sumF;
////        end
////        else
////        mus = (data'*F)./sumF;
////        end
////
////                %    mus{iter}(2) = (temp(2)-sigs{iter}(2)*C);
////        %    mus{iter} = mus{iter}./sumF;
////        %    sigs{iter+1} = (diag(((bsxfun(@minus,data,mus{iter+1})).^2)'*F)'+2*phi)./(sumF+3+2*sigma0);
////        sigs = diag(((bsxfun(@minus,data,mus)).^2)'*F)'./sumF;
////        %    alphas{iter+1} = bsxfun(@rdivide,sum(M,3)+alpha0-1,sumD'+K2*alpha0-K2);
////        alphas = bsxfun(@rdivide,sum(M,3),sumD');
////
////        iter = iter + 1;
////        end
////                sumA = sum(A,2);
////        [~,postlabel] = max(bsxfun(@rdivide,A,sumA)');
////                [~,postlabel3] = max(alphas(postlabel,:)');
////
////        if doplot
////        if iter < maxiter
////        figure(f2);
////        F = bsxfun(@rdivide,(Q .* (A * alphas)),L+eps);
////        subplot(3,1,1);
////        [~,postlabel] = max(D');
////                %change the colors in a scatter plot
////        set(h1,'CData',postlabel);
////
////        [~,postlabel] = max(F');
////        subplot(3,1,2);
////        set(h2,'CData',postlabel);
////
////        subplot(3,1,3)
////        set(h3,'CData',postlabel3);
////        end
////                end
////
////        %mixture_params = {mus{end},sigs{end},lambdas{end},musT{end},sigsT{end},alphas{end}};
////
////        mixture_params = {mus,sigs,lambdas,musT,sigsT,alphas};
////
////
////        likelihoods = L;
////        soft_labels = {D,F};
////        hard_labels = postlabel3;
////        end
////
////
////        function newdata = bin_data(data,time,max_num_elements,bin_size,max_segment_size)
////        N = length(data);
////
////        numsegments = length([time(1):max_segment_size:time(end)]);
////
////        %    totalnumelems = N*numsegments*3;
////        %    numelems_per_bin = ceil(totalnumelems/max_num_elements);
////        if ceil(N*numsegments*3/max_num_elements) == 1
////        newdata = [data,time];
////        return
////        else
////        if ceil(N*numsegments/max_num_elements) == 1
////        newN = max_num_elements/(numsegments*3);
////        else
////        newN = max_num_elements/numsegments;
////        end
////                end
////
////        newdata = [];
////        bin_size = ceil((time(end)-time(1))/newN);
////        for t = time(1):bin_size:time(end)-bin_size
////        inds = find(time>=t & time < t+bin_size);
////        if ~isempty(inds)
////        newdata(end+1,1:2) = [mean(data(inds)),t];
////        end
////                end
////        end
////
////
////
////        function pdf = gaussian(x,mus,sigs,N,K)
////        pdf = 1./sqrt(2*pi*sigs(ones(1,N),:)).*exp(-(x(:,ones(1,K))-mus(ones(1,N),:)).^2./(2*sigs(ones(1,N),:)));
////        end
////
////        function pdf = gaussian1(x,mus,sigs,N,K1,K2)
////        pdf = 1./sqrt(2*pi*repmat(sigs,[1,1,N])).*exp(-(reshape(x(:,ones(1,K1*K2))',[K1,K2,N])-repmat(mus,[1,1,N])).^2./(2*repmat(sigs,[1,1,N])));
////        end
////
////
////        function [model_params,inputsegments,time,data] = get_initial_values(data,time,params,inputsegments)
////
////        K2 = params.K2;
////
////        use_prior_seg = params.use_prior_seg;
////        use_prior_kmeans = params.use_prior_kmeans;
////        params.segmentparams.init_segment_size = 5*1000;
////        params.segmentparams.num_clusters = params.K1;
////        %combineddata = [];
////        %combinedtime = [];
////        segments = [];
////        inputsegments = [];
////
////        if 0
////        for i=1:length(data)
////        combineddata = [combineddata;data{i}'];
////        combinedtime = [combinedtime;time{i}'];
////
////        if use_prior_seg
////        if nargin < 4
////                [segment,tc,wc] = segment_rr_ints(data{i},time{i},params.segmentparams);
////        inputsegments{i} = segment;
////        segments = [segments segment];
////        else
////        segments = [segments inputsegments{i}];
////        end
////        else
////        for t=time{i}(1):params.segmentparams.max_segment_size:time{i}(end)
////                inds = find(time{i}>=t & time{i} < t+params.segmentparams.max_segment_size);
////        if ~isempty(inds)
////        segments(end+1).lt = t;
////        segments(end).rt = t+params.segmentparams.max_segment_size;
////        end
////                end
////        end
////                end
////        else
////        if use_prior_seg
////        if nargin < 4
////                [segments,tc,wc] = segment_rr_ints(data,time,params.segmentparams);
////        else
////        segments = inputsegments;
////        end
////        else
////        for t=time(1):params.segmentparams.max_segment_size:time(end)
////        inds = find(time>=t & time < t+params.segmentparams.max_segment_size);
////        if ~isempty(inds)
////        segments(end+1).lt = t;
////        segments(end).rt = t+params.segmentparams.max_segment_size;
////        end
////                end
////        end
////                end
////
////        if use_prior_seg
////        assignin('base','inputsegments',inputsegments);
////        end
////
////                K1 = length(segments);
////        %data = combineddata;
////        %time = combinedtime;
////
////        P = zeros(length(data),K1);
////        for i=1:length(segments)
////        P(time >= segments(i).lt & time < segments(i).rt,i) = 1;
////        end
////
////                inds = find(sum(P,2)>0);
////        time = time(inds);
////        data = data(inds);
////        P = P(inds,:);
////        N = length(data);
////
////        if use_prior_kmeans
////        if K2 == 2
////        label = kmeans(data,[],'Start',[min(data);mean(data);max(data)]);
////        elseif K2 == 3
////        label = kmeans(data,[],'Start',[min(data);mean(data);max(data)]);
////        else
////        range = max(data)-min(data);
////        label = kmeans(data,[],'Start',[min(data):range/K2:max(data)]);
////        end
////                Q = full(sparse(1:N,label,1,N,K2,N));
////        else
////        Q=zeros(N,K2);
////        range = max(data)-min(data);
////        bins = min(data):range/K2:max(data);
////        %    bins=ceil(bins);
////        for i=1:length(bins)-1
////        Q(data>=bins(i) & data <= bins(i+1),i) = 1;
////        end
////                end
////
////        lambdas = ones(1,K1);
////        alphas = zeros(K1,K2);
////
////
////        for i=1:K1
////                numinclust = sum(P(:,i)==1);
////        lambdas(i) = numinclust/N;
////
////        for j=1:K2
////        alphas(i,j) = sum(P(:,i)==1 & Q(:,j) == 1)/numinclust;
////        end
////                end
////
////        model_params = {lambdas,alphas,P,Q};
////
////        end
////
////
////        function [data,time,timescales,datascales] = scale_data(data,time)
////        timescales = [max(time),min(time)];
////        datascales = [max(data),min(data)];
////        time = (time-min(time))/(max(time)-min(time));
////        data = (data-min(data))/(max(data)-min(data));
////        end
//
//        return null;
//    }
//
//    private Object get_baseline_params() {
////        params.tol = 10^(-10);
////        params.doplot = 0;
////        params.maxiter = 400;
////        params.K1 = 20;
////        params.K2 = 3;
////        params.use_prior_seg = 0;
////        params.use_prior_kmeans = 0;
////        params.segmentparams.max_segment_size = 10*60*1000;
////        params.c = 0;
////
////        params.priors.z0 = 3;
////        params.priors.lambda0 = 0.5;
////        params.priors.alpha0 = 1;
////        params.priors.z0 = 10;
////        params.priors.gammaval = 30;
////
//        return null;
//    }
//
//    private Object filter_inds_by_activity(Object f2) {
////        function normal_times = filter_inds_by_activity(F)
////
////        global NORMALIZATION
////        global FEATURE
////
////
////        FR = FEATURE.CHESTACCEL;
////
////        accelfeature = F.sensor(FEATURE.CHESTACCELID).feature(F.sensor(FEATURE.CHESTACCELID).feature(:,end)~=0,FR.STDEVMAGNITUDE);
////
////        cutoff1 = prctile(accelfeature,1);
////        cutoff2 = prctile(accelfeature,99);
////
////        %inds = find(accelfeature > cutoff1 & accelfeature < cutoff2);
////
////        accelfeature = (accelfeature-cutoff2)/(max(accelfeature)-cutoff1);
////
////
////        %startimes = F.sensor(5).feature(inds,FR.STARTTIMESTAMP);
////        %endtimes = F.sensor(5).feature(inds,FR.ENDTIMESTAMP);
////        %accelfeature = accelfeature(inds);
////
////        startimes = F.sensor(FEATURE.CHESTACCELID).feature(F.sensor(FEATURE.CHESTACCELID).feature(:,end)~=0,FR.STARTTIMESTAMP);
////        endtimes = F.sensor(FEATURE.CHESTACCELID).feature(F.sensor(FEATURE.CHESTACCELID).feature(:,end)~=0,FR.ENDTIMESTAMP);
////        activityornot = accelfeature > NORMALIZATION.ACTIVITYFILTER_CUTOFF;
////
////        normal_times1 = [];
////        for time=startimes(1):60000:endtimes(end)
////        oneminute_inds = startimes>=time & endtimes < time+60000;
////        if isempty(oneminute_inds)
////        continue
////                end
////        min_active = sum(activityornot(oneminute_inds));
////        if min_active < length(oneminute_inds)/2
////        normal_times1(end+1,1:2) = [time,time+60000];
////        end
////                end
////
////
////        normal_times = [];
////
////        %normal_times
////        for i=1:size(normal_times1,1)
////        if ~isempty(normal_times) & normal_times(end,2) == normal_times1(i,1)
////        normal_times(end,2) = normal_times1(i,2);
////        else
////        normal_times(end+1,1:2) = normal_times1(i,:);
////        end
////                end
////
////        end
////
//
//
//        return null;
//    }
//
//    private Object window2feature_accelerometer_main(ArrayList<AutosenseSample> accelx, ArrayList<AutosenseSample> accely, ArrayList<AutosenseSample> accelz) {
//
//        Object features = accelerometerfeature_extraction(accelx, accely, accelz, this.sensorConfig.getFrequency("ACCELX"));
//
//        return null;
//    }
//
//    private Object accelerometerfeature_extraction(ArrayList<AutosenseSample> accelx, ArrayList<AutosenseSample> accely, ArrayList<AutosenseSample> accelz, double frequency) {
////        function features=accelerometerfeature_extraction(G,segX,segY,segZ)
////
////        FR = G.FEATURE.R_ACL;
////
////        feature=[];
////        %check if anyone of the segment is very short or zero length
////        if isempty(segX.sample)||isempty(segY.sample)||isempty(segZ.sample)
////        return;
////        end;
////
////        minX=min(segX.sample);maxX=max(segX.sample);rangeX=maxX-minX;
////
////        minY=min(segY.sample);maxY=max(segY.sample);rangeY=maxY-minY;
////
////        minZ=min(segZ.sample);maxZ=max(segZ.sample);rangeZ=maxZ-minZ;
////
////        meanX=mean(segX.sample);meanY=mean(segY.sample);meanZ=mean(segZ.sample);
////
////        stdevX=std(segX.sample);stdevY=std(segY.sample);stdevZ=std(segZ.sample);
////
////        varX=var(segX.sample);varY=var(segY.sample);varZ=var(segZ.sample);
////
////        medX=median(segX.sample);medY=median(segY.sample);medZ=median(segZ.sample);
////
////        fdX=mean(diff(segX.sample));fdY=mean(diff(segY.sample));fdZ=mean(diff(segZ.sample));
////
////        ratioXY=[];
////        diffXY=[];
////        if length(segX.sample)<length(segY.sample)
////        for i=1:length(segX.sample)
////        diffXY=[diffXY segX.sample(i)-segY.sample(i)];
////        if segY.sample(i)~= 0
////        ratioXY=[ratioXY segX.sample(i)/segY.sample(i)];
////        end;
////        end;
////        else
////        for i=1:length(segY.sample)
////        diffXY=[diffXY segX.sample(i)-segY.sample(i)];
////        if segY.sample(i)~= 0
////        ratioXY=[ratioXY segX.sample(i)/segY.sample(i)];
////        end;
////        end;
////        end;
////        avgDiffXY=mean(diffXY);
////        avgRatioXY=mean(ratioXY);
////
////        ratioYZ=[];
////        diffYZ=[];
////        if length(segY.sample)<length(segZ.sample)
////        for i=1:length(segY.sample)
////        diffYZ=[diffYZ segY.sample(i)-segZ.sample(i)];
////        if segZ.sample(i)~= 0
////        ratioYZ=[ratioYZ segY.sample(i)/segZ.sample(i)];
////        end;
////        end;
////        else
////        for i=1:length(segZ.sample)
////        diffYZ=[diffYZ segY.sample(i)-segZ.sample(i)];
////        if segZ.sample(i)~= 0
////        ratioYZ=[ratioYZ segY.sample(i)/segZ.sample(i)];
////        end;
////        end;
////        end;
////        avgDiffYZ=mean(diffYZ);
////        avgRatioYZ=mean(ratioYZ);
////
////        ratioZX=[];
////        diffZX=[];
////        if length(segZ.sample)<length(segX.sample)
////        for i=1:length(segZ.sample)
////        diffZX=[diffZX segZ.sample(i)-segX.sample(i)];
////        if segX.sample(i)~= 0
////        ratioZX=[ratioZX segZ.sample(i)/segX.sample(i)];
////        end;
////        end;
////        else
////        for i=1:length(segX.sample)
////        diffZX=[diffZX segZ.sample(i)-segX.sample(i)];
////        if segX.sample(i)~= 0
////        ratioZX=[ratioZX segZ.sample(i)/segX.sample(i)];
////        end;
////        end;
////        end;
////        avgDiffZX=mean(diffZX);
////        avgRatioZX=mean(ratioZX);
////
////        %calculation of mean crossings
////        %t = 1:length(segX);
////        [crossingX,t1,t1]=crossing(segX.sample,segX.timestamp,meanX);
////        [crossingY,t1,t1]=crossing(segY.sample,segY.timestamp,meanY);
////        [crossingZ,t1,t1]=crossing(segZ.sample,segZ.timestamp,meanZ);
////
////        %avgDiffXY=mean(abs(segX-segY));
////        %avgDiffYZ=mean(abs(segY-segZ));
////        %avgDiffZX=mean(abs(segZ-segX));
////
////        len=min([length(segX.sample) length(segY.sample) length(segZ.sample)]);
////
////
////        Energy = segX.sample(1:len).^2+segY.sample(1:len).^2+segZ.sample(1:len).^2;
////        avgEnergy = mean(Energy);
////        medianEnergy = median(Energy);
////        varEnergy = var(Energy);
////        stddevEnergy = std(Energy);
////        fdEnergy=mean(diff(Energy));
////        rangeEnergy=max(Energy)-min(Energy);
////        [crossingsEnergy,t1,t1] = crossing(Energy,segX.timestamp(1:len),avgEnergy);
////
////
////
////
////        features{FR.MEANX} = double(meanX);
////        features{FR.MEDIANX} = double(medX);
////        features{FR.VARIANCEX} = double(varX);
////        features{FR.STDDEVX} = double(stdevX);
////        features{FR.AVGFDX} = double(fdX);
////
////        features{FR.MEANY} = double(meanY);
////        features{FR.MEDIANY} = double(medY);
////        features{FR.VARIANCEY} = double(varY);
////        features{FR.STDDEVY} = double(stdevY);
////        features{FR.AVGFDY} = double(fdY);
////
////        features{FR.MEANZ} = double(meanZ);
////        features{FR.MEDIANZ} = double(medZ);
////        features{FR.VARIANCEZ} = double(varZ);
////        features{FR.STDDEVZ} = double(stdevZ);
////        features{FR.AVGFDZ} = double(fdZ);
////
////        features{FR.AVGDIFFXY} = double(avgDiffXY);
////        features{FR.AVGDIFFYZ} = double(avgDiffYZ);
////        features{FR.AVGDIFFZX} = double(avgDiffZX);
////
////        features{FR.AVGRATIOXY} = double(avgRatioXY);
////        features{FR.AVGRATIOYZ} = double(avgRatioYZ);
////        features{FR.AVGRATIOZX} = double(avgRatioZX);
////
////        features{FR.RANGEX} = double(rangeX);
////        features{FR.RANGEY} = double(rangeY);
////        features{FR.RANGEZ} = double(rangeZ);
////
////        features{FR.MEANCROSSINGX} = double(length(crossingX));
////        features{FR.MEANCROSSINGY} = double(length(crossingY));
////        features{FR.MEANCROSSINGZ} = double(length(crossingZ));
////
////        features{FR.AVGENERGY}=double(avgEnergy);
////        features{FR.MEDIANENERGY}=double(medianEnergy);
////        features{FR.VARIANCEENERGY}=double(varEnergy);
////        features{FR.STDDEVENERGY}=double(stddevEnergy);
////        features{FR.AVGFDENERGY}=double(fdEnergy);
////        features{FR.RANGEENERGY}=double(rangeEnergy);
////        features{FR.MEANCROSSINGENERGY}=double(length(crossingsEnergy));
////
////        end
//
//
//        return null;
//    }
//
//    private Object get_model(int i, int i1) {
////        global SENSOR
////        if rip == 1
////        THEMODEL.NAME='stress';
////        THEMODEL.SENSORLIST=[SENSOR.R_RIPID];
////        THEMODEL.WINDOWTYPE='cycle';
////        THEMODEL.MISSINGRATE=0.66; % 66% of missing
////        THEMODEL.GOODQUALITY=0.66;
////        else
////        THEMODEL.NAME='stress';
////        THEMODEL.SENSORLIST=[SENSOR.R_ECGID,SENSOR.R_ACLXID,SENSOR.R_ACLYID,SENSOR.R_ACLZID,SENSOR.R_RIPID];
////        THEMODEL.WINDOWTYPE='window';
////        THEMODEL.MISSINGRATE=0.66; % 66% of missing
////        THEMODEL.GOODQUALITY=0.66;
////        THEMODEL.WINDOW_LEN=winlen*1000; % 60 seconds
////                end
//        return null;
//    }
//
//    private Object detect_peakvalley_v2(ArrayList<AutosenseSample> rip) {
//
////        peakvalley.METADATA='VP';
////        peakvalley.sample=[];
////        peakvalley.timestamp=[];
////        peakvalley.matlabtime=[];
////        if isempty(sample)
////        return;
////        end
////
////                [valley_ind,peak_ind]=peakvalley_v2(sample,timestamp);
////
////        if isempty(valley_ind)
////        return;
////        end
////
////        valleyPeak=[];
////        for i=1:length(valley_ind)
////        valleyPeak=[valleyPeak valley_ind(i) peak_ind(i)];
////        end
////
////        peakvalley.timestamp=timestamp(valleyPeak);
////        peakvalley.sample=sample(valleyPeak);
////        peakvalley.matlabtime=convert_timestamp_matlabtimestamp(G,peakvalley.timestamp);
////
////        end
//
//
//        Object peakindexes = peakvalley_v2(rip);
//
//        return null;
//    }
//
//    private Object peakvalley_v2(ArrayList<AutosenseSample> rip) {
////        %developed by Rummana Bari - Date: 21 January 2014
////        function [valley_ind,peak_ind]=peakvalley_v2(sample,timestamp)%,matlabtime)
////        % Returns indices of valleys and peaks. format of data: Valley - peak
////                % -valley
////        peak_ind=[];
////        valley_ind=[];
////        peak_index=[];
////        valley_index=[];
////        if isempty(sample);
////        return;
////        end
////                % originalSample=sample;
////        %Outlier Removal from Raw sample
////                % range_threshold = 100;
////        % windowlength = 50;
////        % slope_threshold=1500;
////        %
////        % figure;
////        % % plot(sample,'m');hold on;
////        % for i=1:windowlength:length(sample)-(windowlength-1)
////                %     win = i:i+windowlength-1;
////        %     mins = min(sample(win));
////        %     maxs = max(sample(win));
////        %     range=maxs-mins;
////        %     %    median_slope = median(abs(diff(sample(win))));
////        %     %    prctile_slope=prctile(diff(sample(win)),95);
////        %     maximum_slope=(max(diff(sample(win))));
////        %
////        %     if (range<range_threshold && maxs > 4000) || mins==0 || maximum_slope > slope_threshold % if variation/range of windowed signal is less than threshold
////                %         sample(win) = nan;
////        %         timestamp(win) = nan;
////        % %         matlabtime(win)=nan;
////        %     end
////                % end
////                %
////        % if (length(sample)-i)>0
////                %     sample(i:end)=nan;
////        %     timestamp(i:end)=nan;
////        % %     matlabtime(i:end)=nan;
////        % end
////                % sample = sample(~isnan(sample));
////        % timestamp = timestamp(~isnan(timestamp));
////        % matlabtime=matlabtime(~isnan(matlabtime));
////        % hold on;plot(sample,'k');
////        Object y1=smooth(sample,5);  //% Moving average filter by default n=5.
////                % plot(y1,'g');hold on;
////        sample=y1;
////
////        % Median Filter Oreder
////                % Median Filter
////                % n=3;
////        % y = medfilt1(sample,n);
////        % plot(y,'r');hold on;
////        % sample=y;
////        % % Filter (Bandpass)
////                % fs=21.33;
////        % f=2/fs;
////        % delp=0.02;
////        % dels1=0.02;
////        % dels2=0.02;
////        % % F = [0 0.1*f  0.25*f 0.6*f  0.75*f 1]; %RIP Frequency band edges [Passband 0.25- Hz]
////        % F = [0 0.1*f  0.25*f 2.35*f  2.50*f 1]; %RIP Frequency band edges [Passband 0.25- Hz]
////        % A = [0  0   1  1  0 0]; % Desired amplitude [1=Passband; 0= Stop band]
////        % w=[1000/dels1 1/delp 1/dels2];
////        % fl=256;
////        % B = firls(fl,F,A,w);
////        % x_filtered= conv(sample,B,'same');
////        % sample=x_filtered;
////        % plot(sample,'g');
////
////        % peak_index=[];
////        % valley_index=[];
////        up_intercept=[];
////        down_intercept=[];
////        up_intercept_index=[];
////        down_intercept_index=[];
////        Timewindow=8;  % In second, TImewindow should include at least one breathe cycle
////                Object windowlength=round(Timewindow*21.33);
////        % plot(sample,'r');hold on;
////        % Moving Average Curve MAC from filtered samples
////        MAC=[];
////        for i=(windowlength+1):1:length(sample)-(windowlength+1)
////        MAC(end+1) = mean(sample(i-windowlength:i+windowlength)); % Moving Average Curve over window
////                end
////        j=(windowlength+1):1:length(sample)-(windowlength+1);
////        % plot(j,MAC,'k','Linewidth',2)
////
////                %% MAC Intercept (Up and Down Intercept)
////        for i=2:length(MAC)
////        if (sample(i+windowlength-1)<=MAC(i-1) && sample(i+windowlength)>=MAC(i))
////            up_intercept= [up_intercept MAC(i)];
////        up_intercept_index=[up_intercept_index i+windowlength-1];
////
////        elseif (sample(i+windowlength-1)>=MAC(i-1) && sample(i+windowlength)<=MAC(i))
////        down_intercept=[down_intercept MAC(i)];
////        down_intercept_index=[down_intercept_index i+windowlength-1];
////
////        end
////                end
////        % plot(up_intercept_index,sample(up_intercept_index),'kd','MarkerSize',9)
////                % plot(down_intercept_index,sample(down_intercept_index),'ro','MarkerSize',9)
////
////                %% Intercept Outlier Removal
////
////                [UI,DI]=Intercept_outlier_detector_RIP_lamia(up_intercept_index,down_intercept_index,sample,timestamp,Timewindow);
//        Object v = Intercept_outlier_detector_RIP_lamia(0,1);//up_intercept_index,down_intercept_index,sample,timestamp,Timewindow);)
////        if isempty(UI)
////        return;
////        end
////        if isempty(DI)
////        return;
////        end
////                % plot(UI,sample(UI),'m^','MarkerFaceColor','m');
////        % plot(DI,sample(DI),'bv','MarkerFaceColor','b');
////
////        %% Peak-Valley
////        for i=1:length(DI)-1  % As UI>DI, set in Intercept_outlier_detector
////        temp=sample(UI(i):DI(i+1));
////        pkvalue=max(temp);
////        ind=find(temp==pkvalue);
////        if isempty(ind)
////        continue;
////        end
////
////                pkindex=UI(i)+ind(1)-1;
////        peak_index=[peak_index pkindex];
////
////        temp=sample(DI(i):UI(i));
////        [maxtab, mintab]=localMaxMin(temp,1);
////        if isempty(mintab)
////        vlvalue=min(temp);
////        ind=find(temp==vlvalue);
////        if isempty(ind)
////        continue;
////        end
////                vlindex=DI(i)+ind(1)-1;
////        else
////        vlindex=DI(i)+mintab(end,1)-1;
////        end
////        valley_index=[valley_index vlindex];
////        end
////
////                % plot(peak_index,sample(peak_index),'bo');
////        % plot(valley_index,sample(valley_index),'go');
////
////        %% Inspiration Expiration Amplitude outlier removal
////                %
////                Inspiration_amplitude=[];
////        Expiration_amplitude=[];
////
////        for i=1:length(valley_index)-1
////        Inspiration_amplitude=[Inspiration_amplitude sample(peak_index(i))-sample(valley_index(i))];
////        Expiration_amplitude=[Expiration_amplitude abs(sample(valley_index(i+1))-sample(peak_index(i)))];
////        end
////                Mean_Inspiration_Amp=mean(Inspiration_amplitude);
////        Mean_Expiration_Amp=mean(Expiration_amplitude);
////
////        % Inspiration Expiration outlier from amplitude threshold
////        % If Inspiration amplitude is less than 20% of mean ispiration amplitude,
////        % remove that valley-peak pair
////        final_peak_index=[];
////        final_valley_index=[];
////        % Remove small amplitude Inspiration
////        for i=1:length(Inspiration_amplitude)
////        if (Inspiration_amplitude(i)>0.15*Mean_Inspiration_Amp)
////            final_peak_index=[final_peak_index peak_index(i)];
////        final_valley_index=[final_valley_index valley_index(i)];
////
////        end
////                end
////        %
////        % plot(final_peak_index,sample(final_peak_index),'bo','MarkerFaceColor','m');
////        % plot(final_valley_index,sample(final_valley_index),'ro','MarkerFaceColor','g');
////
////        %%
////        % If Expiration amplitude is less than 20% of mean expiration amplitude,
////        % remove that peak- valley pair
////        Expiration_amplitude=[];
////        for i=1:length(final_valley_index)-1
////        Expiration_amplitude=[Expiration_amplitude abs(sample(final_valley_index(i+1))-sample(final_peak_index(i)))];
////        end
////                Mean_Expiration_Amp=mean(Expiration_amplitude);
////
////        peak_ind=[];
////        valley_ind=[];
////        valley_ind=[valley_ind final_valley_index(1)];
////        % Remove small amplitude Expiration
////        for i=1:length(Expiration_amplitude)
////        if (Expiration_amplitude(i)>0.15*Mean_Expiration_Amp)
////            valley_ind=[valley_ind final_valley_index(i+1)];
////        peak_ind=[peak_ind final_peak_index(i)];
////        end
////                end
////        peak_ind=[peak_ind final_peak_index(end)];
////        % plot(peak_ind,sample(peak_ind),'bo','MarkerFaceColor','k');
////        % plot(valley_ind,sample(valley_ind),'ro','MarkerFaceColor','k');
////        % length(peak_ind)
////                % length(valley_ind)
////                %% Locate Actual Valley using local mininma algorithm between any valley peak pair.
////        % Valley_Index=[];
////        % for i=1:length(valley_ind)
////                %
////        %     temp=sample(valley_ind(i):peak_ind(i));
////        %     [maxtab, mintab]=localMaxMin(temp,1);
////        %     if isempty(mintab)
////                %         vlind=valley_ind(i);
////        %     else
////        %         vlind=valley_ind(i)+mintab(end,1)-1;
////        %     end
////                %     Valley_Index=[Valley_Index vlind];
////        %     plot(vlind,sample(vlind),'ro','MarkerFaceColor','c');
////        % end
////                % plot(Valley_Index,sample(Valley_Index),'ro','MarkerFaceColor','c');
////
////        end
//
//
//
//        return null;
//    }
//
//    private Object Intercept_outlier_detector_RIP_lamia(int i, int i1) {
////        function [Up_intercept2,Down_intercept2]=Intercept_outlier_detector_RIP_lamia(upInterceptIndex,DownInterceptIndex,sample,timestamp,T)
////                % function [UI,DI]=Intercept_outlier_detector_RIP_lamia(upInterceptIndex,DownInterceptIndex,sample,timestamp,T)
////        Up_intercept2=[];
////        Down_intercept2=[];
////        count=[];
////        UI=[];
////        DI=[];
////        D=DownInterceptIndex;
////        U=upInterceptIndex;
////        % Making U and D of equal length
////                minimumLength=min(length(U),length(D));
////        D=DownInterceptIndex(1:minimumLength);
////        U=upInterceptIndex(1:minimumLength);
////
////        % Removal of  2 or more consecutive Up intercepts or consecutive Down Intercept
////                i=1;j=1;
////        while(i<length(U))
////
////            if j>=(length(D)-1)
////        a=1;
////        break;
////        end
////
////        while(j<length(D))
////        %         i
////                %         j
////
////        ind=[];
////        if U(1)<D(1)  % Up Intercept comes earlier
////
////        if i==length(U)
////        break;
////        elseif j==length(D)
////        break;
////        end
////        if U(i)<D(j) && D(j)<U(i+1)
////        UI=[UI U(i)];
////        ind=find(D>D(j) & D<U(i+1));
////        if isempty(ind)
////        DI=[DI D(j)];
////        j=j+1;
////        else
////        DI=[DI D(ind(end))];
////        j=ind(end)+1;
////        end
////                i=i+1;
////        elseif  U(i)<D(j) && D(j)>U(i+1)
////        ind=find(U>U(i) & U<D(j));
////        if isempty(ind)
////        UI=[UI U(i)];
////        i=i+1;
////        else
////        UI=[UI U(ind(end))];
////        i=ind(end)+1;
////        end
////        DI=[DI D(j)];
////        j=j+1;
////        end
////
////
////        elseif D(1)<U(1)  %% Down Intercept comes earlier
////        if i==length(D)
////        break;
////        elseif j==length(U)
////        break;
////        end
////        if D(i)<U(j) && U(j)<D(i+1)
////        DI=[DI D(i)];
////        ind=find(U>U(j) & U<D(i+1));
////        if isempty(ind);
////        UI=[UI U(j)];
////        j=j+1;
////        else UI=[UI U(ind(end))];
////        j=ind(end)+1;
////        end
////
////                i=i+1;
////        elseif D(i)<U(j) && U(j)>D(i+1)
////        UI=[UI U(j)];
////        ind=find(D>D(i) & D<U(j));
////        if isempty(ind)
////        DI=[DI D(i)];
////        i=i+1;
////        else DI=[DI D(ind(end))];
////        i=ind(end)+1;
////        end
////                j=j+1;
////
////        end
////
////                end
////
////        end
////                end
////        if isempty(UI)
////        if isempty(DI)
////        return;
////        end
////                end
////        if UI(1)<DI(1)  % To start calculation from Down Intercept
////                UI=UI(2:end);
////        end
////
////                minLength=min(length(UI),length(DI)); % make UI and DI equal length
////        UI=UI(1:minLength);
////        DI=DI(1:minLength);
////
////        % Keep intercept pair (consecutive down and up) if brething frequency is
////                % within range of: 8 breathe/min <fr< 65 breathe/min
////        fr=[];
////        Down_intercept=[];
////        Up_intercept=[];
////        for i=1:length(DI)-1
////        fr=60/(timestamp(DI(i+1))-timestamp(DI(i)));
////        if 8<=fr<=65
////        Down_intercept=[Down_intercept DI(i)];
////        Up_intercept=[Up_intercept UI(i)];
////        end
////                end
////
////        % If two Intercepts (consecutive up and down)come within T/20 second time gap, remove those
////                % intercept pair
////
////                % Up_intercept2=[];
////        % Down_intercept2=[];
////        Down_intercept2=[Down_intercept2 Down_intercept(1)];
////
////        equivalent_sample_points=(T/20)*21.33;
////        upTOdown_distance=[];
////        for i=1:length(Down_intercept)-1
////        upTOdown_distance=Down_intercept(i+1)-Up_intercept(i)+1;
////
////        if upTOdown_distance>equivalent_sample_points
////        Up_intercept2=[Up_intercept2 Up_intercept(i)];
////        Down_intercept2=[Down_intercept2 Down_intercept(i+1)];
////
////        end
////                end
////
////        end
//
//
//        return null;
//    }
//
//    private Object detect_RR(ArrayList<AutosenseSample> ecg) {
//        int RR_OUTLIER_WINLEN=10*60*1000;
//
//        int Rpeak_index = ddetect_Rpeak(ecg,sensorConfig.getFrequency("ECG"));
//        Object pkT= null; //timestamp(Rpeak_index); //Indexing, not a method
//
//        Object rr_value = null; //diff(pkT)/1000.0;
//        Object rr_timestamp = 0; //pkT(1:end-1);
//        Object rr_outlier = detect_outlier_v2(rr_value,rr_timestamp,RR_OUTLIER_WINLEN);
//
//        double mu = 0; //mean(rr_value_with_outliers);
//        double sigma = 0; //std(rr_value_with_outliers);
//
//        rr_outlier = null; //abs(rr_value_outliers - mu) > 3*sigma;
//
//        return null;
//    }
//
//    private Object detect_outlier_v2(Object rr_value, Object rr_timestamp, int rr_outlier_winlen) {
////        outlier=[];
////        if isempty(timestamp), return;end;
////
////        ind=find(sample>0.3 & sample<2);
////        valid_rrInterval=sample(ind);
////        valid_timestamp=timestamp(ind);
////
//        Object diff_rrInterval= null; //abs(diff(valid_rrInterval));
//        double  MED=0;//4.5*0.5*iqr(diff_rrInterval);
//        double MAD=0;//(median(valid_rrInterval)-2.9*0.5*iqr(diff_rrInterval))/3;
//        double CBD=(MED+MAD)/2;
//        if (CBD<0.2) {
//            CBD = 0.2;
//        }
////
////                outlier=ones(1,size(sample,2))*G.QUALITY.BAD;
////        outlier(1)=G.QUALITY.GOOD;
////        standard_rrInterval=valid_rrInterval(1);
////        prev_beat_bad=0;
////        for i=2:length(valid_rrInterval)-1
////        ref=valid_rrInterval(i);
////        if (valid_rrInterval(i)>.3 && valid_rrInterval(i)<2)
////        %
////        %
////        beat_diff_prevGood=abs(standard_rrInterval-valid_rrInterval(i));
////        beat_diff_pre=abs(valid_rrInterval(i-1)-valid_rrInterval(i));
////        beat_diff_post=abs(valid_rrInterval(i)-valid_rrInterval(i+1));
////        if(prev_beat_bad==1 && beat_diff_prevGood<CBD)
////            ind1=find(timestamp==valid_timestamp(i));
////        outlier(ind1)=G.QUALITY.GOOD;
////        prev_beat_bad=0;
////        standard_rrInterval=valid_rrInterval(i);
////        elseif (prev_beat_bad==1 && beat_diff_prevGood>CBD && beat_diff_pre<=CBD && beat_diff_post<=CBD)
////        ind1=find(timestamp==valid_timestamp(i));
////        outlier(ind1)=G.QUALITY.GOOD;
////        prev_beat_bad=0;
////        standard_rrInterval=valid_rrInterval(i);
////
////        elseif (prev_beat_bad==1 && beat_diff_prevGood>CBD && (beat_diff_pre>CBD || beat_diff_post>CBD))
////        prev_beat_bad=1;
////
////        elseif (prev_beat_bad==0 && beat_diff_pre<=CBD)
////        ind1=find(timestamp==valid_timestamp(i));
////        outlier(ind1)=G.QUALITY.GOOD;
////        prev_beat_bad=0;
////        standard_rrInterval=valid_rrInterval(i);
////        elseif(prev_beat_bad==0 && beat_diff_pre>CBD)
////        prev_beat_bad=1;
////
////        end
////                end
////
////        end
//
//
//
//        return null;
//    }
//
//    private int ddetect_Rpeak(ArrayList<AutosenseSample> autosenseSample, double fs) {
//
////        window_l = ceil(fs/5);
////        thr1 = 0.5;
////        f=2/fs;
////        delp=0.02;
////        dels1=0.02;
////        dels2=0.02;
////        F = [0 4.5*f  5*f 20*f  20.5*f 1]
////        A = [0  0   1  1  0 0]
////        w=[500/dels1 1/delp 500/dels2]
////        fl = 256;
////        b = firls(fl,F,A,w);
////        y2 = conv(sample,b,'same');
////        y2 = y2/prctile(y2,90);
////
////
////
////        h_D = [-1 -2 0 2 1]*1/8;
////        y3 = conv(y2, h_D,'same');
////        y3 = y3/prctile(y3,90);
////
////        y4 = y3.^2;
////        y4 = y4/prctile(y4,90);
////
////        h_I = blackman(window_l);
////        y5 = conv (y4 ,h_I,'same');
////        y5 = y5/prctile(y5,90);
////
////        count = 1;
////        for i = 3:length(y5)-2
////            if (y5(i-2) < y5(i-1)&& y5(i-1) < y5(i)&& y5(i)>= y5(i+1)&& y5(i+1) > y5(i+2))
////                pkt(count) = i;
////                valuepks(count) = y5(i);
////                count = count+1;
////            end
////        end
////
////
////        rr_ave = sum(diff(pkt))/(length(pkt)-1);
////        thr2 = 0.5*thr1;
////        sig_lev = 4*thr1;
////        noise_lev = 0.1*sig_lev;
////        c1 = 1;
////        c2 = []
////        i = 1;
////        thresholds = []
////        Rpeak_temp1 = []
//////        % If CURRENTPEAK > THR_SIG, that location is identified as a QRS complex
//////        % candidate and the signal level (SIG_LEV) is updated:
//////        % SIG _ LEV = 0.125 ×CURRENTPEAK + 0.875× SIG _ LEV
//////                % If THR_NOISE < CURRENTPEAK < THR_SIG, then that location is identified as a
//////                % noise peak and the noise level (NOISE_LEV) is updated:
//////        % NOISE _ LEV = 0.125×CURRENTPEAK + 0.875× NOISE _ LEV
//////                % Based on new estimates of the signal and noise levels (SIG_LEV and NOISE_LEV,
//////        % respectively) at that point in the ECG, the thresholds are adjusted as follows:
//////        % THR _ SIG = NOISE _ LEV + 0.25 × (SIG _ LEV ? NOISE _ LEV )
//////        % THR _ NOISE = 0.5× (THR _ SIG)
////        while i <= length(pkt);
////            if isempty(Rpeak_temp1)
////                if y5(pkt(i)) >= thr1 && y5(pkt(i)) < 3*sig_lev;
////                    Rpeak_temp1(c1) = pkt(i) ;
////                    sig_lev = 0.125*y5(pkt(i))+0.875*sig_lev;
////                    c2(c1) = i;
////                    c1 = c1+1;
////                    elseif y5(pkt(i)) < thr1 && y5(pkt(i))> thr2;
////                    noise_lev = 0.125*y5(pkt(i))+0.875*noise_lev;
////                end
////                thr1 = noise_lev+0.25*(sig_lev-noise_lev);
////                thr2 = 0.5*thr1;
////                i = i+1;
////                rr_ave = rr_ave_update(Rpeak_temp1,rr_ave);
////            else
////                if (pkt(i)-pkt(c2(c1-1))>1.66*rr_ave) && (i-c2(c1-1)>1)
////                    searchback_array = valuepks(c2(c1-1)+1:i-1)
////        searchback_array_inrange = searchback_array(find(searchback_array<3*sig_lev & searchback_array>thr2));
////                    if isempty(searchback_array_inrange)
////                    else
////                        searchback_array_inrange_index = find(searchback_array<3*sig_lev & searchback_array>thr2);
////                        [searchback_max, searchback_max_index] = max(searchback_array_inrange);
////                        Rpeak_temp1(c1) = pkt(c2(c1-1)+searchback_array_inrange_index(searchback_max_index));
////                        sig_lev = 0.125*y5(Rpeak_temp1(c1))+0.875*sig_lev;
////                        c2(c1) = c2(c1-1)+searchback_array_inrange_index(searchback_max_index);
////                        i = c2(c1)+1;
////                        c1 = c1+1;
////                        thr1 = noise_lev+0.25*(sig_lev-noise_lev);
////                        thr2 = 0.5*thr1;
////                        rr_ave = rr_ave_update(Rpeak_temp1,rr_ave);
////                        continue;
////                    end
////                elseif y5(pkt(i)) >= thr1 && y5(pkt(i)) < 3*sig_lev;
////                    Rpeak_temp1(c1) = pkt(i) ;
////                    sig_lev = 0.125*y5(pkt(i))+0.875*sig_lev;
////                    c2(c1) = i;
////                    c1 = c1+1;
////                    elseif  y5(pkt(i)) < thr1 && y5(pkt(i))> thr2;
////                    noise_lev = 0.125*y5(pkt(i))+0.875*noise_lev;
////                end
////                thr1 = noise_lev+0.25*(sig_lev-noise_lev);
////                thr2 = 0.5*thr1;
////                i = i+1;
////                rr_ave = rr_ave_update(Rpeak_temp1,rr_ave);
////            end
////            thresholds(1,i-1) = thr1;
////            thresholds(2,i-1) = thr2;
////            thresholds(3,i-1) = 3*sig_lev;
////        end;
////
//////        %Elimination
////        difference = 0;
////        Rpeak_temp2 = Rpeak_temp1;
////        while difference~=1
////            length_Rpeak_temp2 = length(Rpeak_temp2);
////            comp_index1 = Rpeak_temp2(find(diff(Rpeak_temp2)<0.5*fs));
////            comp_index2 = Rpeak_temp2(find(diff(Rpeak_temp2)<0.5*fs)+1);
////            comp1 = sample(comp_index1)';
////            comp2 = sample(comp_index2)';
////                [eli_valie,eli_index] = min([comp1;comp2])
////        Rpeak_temp2(find(diff(Rpeak_temp2)<0.5*fs)-1+eli_index) = []
////        difference = isequal(length_Rpeak_temp2, length(Rpeak_temp2));
////        end
////
//////                %T wave discrimination
//////        % T_wave_temp1_index = find(diff(Rpeak_temp2)>=0.2*fs & diff(Rpeak_temp2)<=0.36*fs)+1;
//////        % if isempty(T_wave_temp1_index)
//////                %     Rpeak_temp3 = Rpeak_temp2;
//////        %     T_wave_temp = [];
//////        % else
//////        % for i = 1:length(T_wave_temp1_index)
//////                %     slope1 = y5(Rpeak_temp2(T_wave_temp1_index(i))) - y5(Rpeak_temp2(T_wave_temp1_index(i))-1);
//////        %     slope2 = y5(Rpeak_temp2(T_wave_temp1_index(i))-1) - y5(Rpeak_temp2(T_wave_temp1_index(i)-1)-1);
//////        %     if slope1 < slope2
//////        %         T_wave_index(i) = 1;
//////        %     else
//////        %         T_wave_index(i) = 0;
//////        %     end
//////                % end
//////                % Rpeak_temp3 = Rpeak_temp2(find(T_wave_index == 0));
//////        % T_wave_temp = Rpeak_temp2(find(T_wave_index == 1));
//////        % end
//////                %
//////        % for   i = 2:length(Rpeak_temp1);
//////        %     rr = Rpeak_temp1(i)- Rpeak_temp1(i-1);
//////        %     if rr > rr_avg
//////                %         [v,l] = max(y5(Rpeak_temp1(i-1)+1:Rpeak_temp1(i)-1))
//////        %         Rpeak_temp2(c2) = find()
////
////
////
////        for i=2:length(Rpeak_temp2)-1
////                [v,l] = max(sample(Rpeak_temp2(i)-ceil(fs/10):Rpeak_temp2(i)+ceil(fs/10)))
////        Rpeak_temp3(i) = Rpeak_temp2(i)-ceil(fs/10)+l-1;
////        end
////        Rpeak_temp3(1) = Rpeak_temp2(1);
////        Rpeak_index = sort(Rpeak_temp3);
//
//        return 0;
//    }
//
//    private Object process_data() {
//
//        ArrayList<DoubleSample> RIP_Quality = calculateRIPDataQuality(this.RIP);
//        RIP_Quality = (ArrayList<DoubleSample>) furtherOutlierDetection(this.RIP, RIP_Quality);
//        Object RIP_Data = saveGoodData(this.RIP, RIP_Quality);
//
//
//        ArrayList<DoubleSample>  ECG_Quality = calculateECGDataQuality(this.ECG);
//        Object ECG_Data = saveGoodData(this.ECG, ECG_Quality);
//        ECG_Data = filter_bad_ecg(this.ECG, ECG_Data);
//
//
//        Object ACCELX_Data = remove_drift_accel(this.ACCELX);
//        Object ACCELY_Data = remove_drift_accel(this.ACCELY);
//        Object ACCELZ_Data = remove_drift_accel(this.ACCELZ);
//
//        return null;
//    }
//
//    private Object filter_bad_ecg(ArrayList<AutosenseSample> data, Object d) {
//        return null;
//    }
//
//    private Object furtherOutlierDetection(ArrayList<AutosenseSample> data, Object d) {
//        return null;
//    }
//
//    private Object remove_drift_accel(ArrayList<AutosenseSample> accelx) {
//        return null;
//    }
//
//    private Object saveGoodData(ArrayList<AutosenseSample> rip, Object rip_quality) {
//        return null;
//    }
//
//    private ArrayList<ArrayList<AutosenseSample>> window(ArrayList<AutosenseSample> data, int windowSize) {
//
//        ArrayList<ArrayList<AutosenseSample>> result = new ArrayList<>();
//
//        long startTime = data.get(0).timestamp;
//        ArrayList<AutosenseSample> temp = new ArrayList<>();
//        for(AutosenseSample s: data) {
//            if (s.timestamp < startTime+windowSize ) {
//                temp.add(s);
//            } else {
//                result.add(temp);
//                temp = new ArrayList<>();
//                temp.add(s);
//                startTime = s.timestamp;
//            }
//        }
//        return result;
//    }
//
//    private ArrayList<DoubleSample> calculateRIPDataQuality(ArrayList<AutosenseSample> rip) {
//        int WINDOWSIZE = 3*1000;
//
//        ArrayList<DoubleSample> quality = new ArrayList<>();
//
//        ArrayList<ArrayList<AutosenseSample>> windowedRIP = window(rip,WINDOWSIZE);
//
//        for(ArrayList<AutosenseSample> s: windowedRIP) {
//            RipQualityCalculation rq = new RipQualityCalculation();
//            quality.add(new DoubleSample(s.get(0).timestamp, rq.currentQuality(s)));
//        }
//
//        return quality;
//    }
//
//    private ArrayList<DoubleSample> calculateECGDataQuality(ArrayList<AutosenseSample> ECG) {
//        int WINDOWSIZE = 3*1000;
//
//        ArrayList<DoubleSample> quality = new ArrayList<>();
//
//        ArrayList<ArrayList<AutosenseSample>> windowedECG = window(ECG,WINDOWSIZE);
//
//        for(ArrayList<AutosenseSample> s: windowedECG) {
//            ECGQualityCalculation rq = new ECGQualityCalculation();
//            quality.add(new DoubleSample(s.get(0).timestamp, rq.currentQuality(s)));
//        }
//
//        return quality;
//    }


    public double process() {

        DataPoint[] accelerometerX = generateDataPointArray(ACCELX, sensorConfig.getFrequency("ACCELX"));
        DataPoint[] accelerometerY = generateDataPointArray(ACCELY, sensorConfig.getFrequency("ACCELY"));
        DataPoint[] accelerometerZ = generateDataPointArray(ACCELZ, sensorConfig.getFrequency("ACCELZ"));
        DataPoint[] ecg = generateDataPointArray(ECG, sensorConfig.getFrequency("ECG"));
        DataPoint[] rip = generateDataPointArray(RIP, sensorConfig.getFrequency("RIP"));

        System.out.println(debugOutput());
        if (accelerometerX.length >= 16 && accelerometerY.length >= 16 && accelerometerZ.length >= 16)
            accelFeatures = new AccelerometerFeatures(accelerometerX, accelerometerY, accelerometerZ, sensorConfig.getFrequency("ACCELX"));

        if (ecg.length >= 16)
            ecgFeatures = new ECGFeatures(ecg, sensorConfig.getFrequency("ECG"));


        return 0.0;
    }

    private DataPoint[] generateDataPointArray(ArrayList<AUTOSENSE_PACKET> data, double frequency) {
        ArrayList<DataPoint> result = new ArrayList<>();

        for (AUTOSENSE_PACKET ap : data) { //Convert packets into datapoint arrays based on sampling frequency
            for (int i = 0; i < 5; i++) {
                DataPoint dp = new DataPoint();
                dp.value = ap.data[i];
                dp.timestamp = ap.timestamp - (long) Math.floor((4 - i) / frequency);
                result.add(dp);
            }
        }
        DataPoint[] dpArray = new DataPoint[result.size()];
        result.toArray(dpArray);
        return dpArray;
    }

    private String debugOutput() {
        return "SIZES: " + this.ECG.size() + ", " + this.RIP.size() + ", " + this.ACCELX.size() + ", " + this.ACCELY.size() + ", " + this.ACCELZ.size();
    }


    public void add(AUTOSENSE_PACKET ap) {

        if (this.windowStartTime < 0)
            this.windowStartTime = ap.timestamp;

        if ((ap.timestamp - windowStartTime) >= this.windowSize) { //Process the buffer every windowSize milliseconds
            process();
            resetBuffers();
            this.windowStartTime = ap.timestamp;
        }

        switch (ap.channelID) {
            case AUTOSENSE.CHEST_ECG:
                this.ECG.add(ap);
                break;

            case AUTOSENSE.CHEST_RIP:
                this.RIP.add(ap);
                break;

            case AUTOSENSE.CHEST_ACCEL_X:
                this.ACCELX.add(ap);
                break;

            case AUTOSENSE.CHEST_ACCEL_Y:
                this.ACCELY.add(ap);
                break;

            case AUTOSENSE.CHEST_ACCEL_Z:
                this.ACCELZ.add(ap);
                break;

            default:
//                System.out.println("NOT INTERESTED: " + ap);
                break;

        }
    }

    private void resetBuffers() {
        this.ECG = new ArrayList<>();
        this.RIP = new ArrayList<>();
        this.ACCELX = new ArrayList<>();
        this.ACCELY = new ArrayList<>();
        this.ACCELZ = new ArrayList<>();
    }


}
