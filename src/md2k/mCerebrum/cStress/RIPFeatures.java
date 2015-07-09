package md2k.mCerebrum.cStress;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class RIPFeatures{

    //ripfeature_extraction.m

    public double InspDuration;
    public double ExprDuration;
    public double RespDuration;
    public double Stretch;
    public double StretchUp;
    public double StretchDown;
    public double IERatio;
    public double RSA;


    public RIPFeatures(DataPoint[] seg, DataPoint[] ecg) {

        DescriptiveStatistics statsSeg = new DescriptiveStatistics();
        for(DataPoint d: seg) {
            statsSeg.addValue(d.data);
        }
        double min = statsSeg.getMin();
        double max = statsSeg.getMax();

        double median = 0; //TODO: Figure out median;


        this.Stretch = max - min;
        this.StretchUp = max - median;
        this.StretchDown = median - min;

        int peakindex = 0;
        double peakValue = -1e9;
        for(int i=0; i<seg.length; i++) {
            if (seg[i].data > peakValue) {
                peakindex = i;
            }
        }

        this.InspDuration = seg[peakindex].timestamp - seg[0].timestamp;
        this.ExprDuration = seg[seg.length].timestamp - seg[peakindex].timestamp;
        this.RespDuration = seg[seg.length].timestamp - seg[0].timestamp;

        this.RSA = rsaCalculateCycle(seg, ecg);

        this.IERatio = InspDuration / ExprDuration;


    }

    private double rsaCalculateCycle(DataPoint[] seg, ECGFeatures ecgFeatures) {
        //TODO: Fix me

        DataPoint[] ecg_rr = ecgFeatures.computeRR();


        return 0;
    }



}
