package md2k.mCerebrum;

import md2k.mCerebrum.cStress.Autosense.AUTOSENSE;
import md2k.mCerebrum.cStress.PuffMarker;
import md2k.mCerebrum.cStress.Structs.CSVDataPoint;
import md2k.mCerebrum.cStress.Structs.DataPoint;
import md2k.mCerebrum.cStress.Structs.StressProbability;
import md2k.mCerebrum.cStress.cStress;
import md2k.mCerebrum.cStress.util.PuffMarkerUtils;

/**
 * Created by joy on 12/7/2015.
 */
public class PuffMarkerMain {
    public static void main(String[] args) {
//        String path = "/Users/hnat/Downloads/processedrawdata/SI";
        String path = "D:\\smoking_memphis\\data\\Memphis_Smoking_Lab\\dataset_csv\\";
//        String[] pIds = {"p01", "p02", "p03", "p04", "p05", "p06"};
//        String[][] sIds = {{"s02", "s03", "s04", "s05"}, {"s03", "s04", "s05", "s06"}, {"s01", "s02", "s03"}, {"s01"}, {"s01"}, {"s01"}};
        String[] pIds = {"p01"};
        String[][] sIds = {{"s02"}};
        int ii = 0;
        System.out.println("Left/Right,Total Segment,Height(filtered),Duration (filtered),Roll-Pitch(filtered),TP,Total Puffs");
        for (String pId : pIds) {
            for (String sId : sIds[ii++]) {
                CSVParser tp = new CSVParser();
                String person = pId;
                String session = sId;
                for (int id : PuffMarkerUtils.IDs) {
                    String sensorId = String.format("%02d", id);
                    String fileName = path + person + "_" + session + "_" + sensorId + ".csv";
                    tp.importData(fileName, id);

                }
                PuffMarker puffMarker = new PuffMarker(6000*1000, null, null, person);
                StressProbability output;
                for (CSVDataPoint ap : tp) {
                    DataPoint dp = new DataPoint(ap.timestamp, ap.value);
                    output = puffMarker.add(ap.channel, dp);
                    if (output != null) {
                        System.out.println(output.label + " " + output.probability);
                    }
                }
                puffMarker.process();
            }
        }

    }
}
