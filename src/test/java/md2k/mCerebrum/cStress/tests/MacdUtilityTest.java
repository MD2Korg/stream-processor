package md2k.mcerebrum.cstress.tests;

import md2k.mcerebrum.cstress.library.signalprocessing.Smoothing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by hsarker on 2/3/2016.
 */
public class MacdUtilityTest {


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testGetEpisodes() throws Exception {
        double[] samples = new double[]{0.019792, 0.014284, 0.030163, 0.017618, 0.042817, 0.026549, 0.002695, 0.007828,
                0.107254, 0.107254, 0.107254, 0.107254, 0.002817, 0.025224, 0.004559, 0.003418, 0.000012, 0.024865,
                0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.017831,
                0.017831, 0.017831, 0.017831, 0.017831, 0.017831, 0.203600, 0.438155, 0.912904, 0.912904, 0.160391,
                0.467280, 0.137003, 0.137003, 0.976708, 0.994188, 0.994616, 0.994616, 0.994616, 0.994616, 0.994616,
                0.994616, 0.994616, 0.489216, 0.489216, 0.942148, 0.952150, 0.942398, 0.942398, 0.942398, 0.406546,
                0.042369, 0.014318, 0.014318, 0.014318, 0.754262, 0.636237, 0.824496, 0.824496, 0.824496, 0.003622,
                0.003275, 0.006647, 0.006647, 0.006647, 0.249211, 0.249211, 0.198126, 0.198126, 0.198126, 0.100370,
                0.004219, 0.004219, 0.003656, 0.033878, 0.033878, 0.033878, 0.009439, 0.000000, 0.008124, 0.000001,
                0.000009, 0.000009, 0.000009, 0.000004, 0.060868, 0.053274, 0.011435, 0.003149, 0.000001, 0.004570,
                0.000008, 0.000008, 0.000008, 0.000005, 0.000018};
        ArrayList<MacdUtility.Episode> episodes = MacdUtility.getEpisodes(samples);
        for (MacdUtility.Episode episode : episodes) {
            System.out.println(episode.start + "-" + episode.middle + "-" + episode.end);
            //System.out.println((episode.start+1) + "-" + (episode.middle+1) + "-" + (episode.end+1));
        }

        int a = 7, b = 19, c = 2;
        double emaFast = samples[0];
        double emaSlow = samples[0];
        //double macd = 0;
        double signal = 0;
        double histogram = 0;
        for (int i = 0; i < samples.length; i++) {
            emaFast = Smoothing.ewma(samples[i], emaFast, 2.0 / (a + 1));
            emaSlow = Smoothing.ewma(samples[i], emaSlow, 2.0 / (b + 1));
            double macd = emaFast - emaSlow;
            signal = Smoothing.ewma(macd, signal, 2.0 / (c + 1));
            double histogramPrev = histogram;
            histogram = macd - signal;
            if (histogramPrev < 0 && histogram > 0) {
                System.out.println(i);
            } else if (histogramPrev > 0 && histogram < 0) {
                System.out.println("-----" + i);
            }
        }

        /*
        Participant	From Index	Reactive Index	To Index
        1	1	-1	41
        1	42	48	53
        1	54	57	62
        1	63	68	72
        1	73	79	103
        */
    }

}
