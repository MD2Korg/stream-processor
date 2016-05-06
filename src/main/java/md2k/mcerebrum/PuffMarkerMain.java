package md2k.mcerebrum;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by nsleheen on 4/5/2016.
 */
public class PuffMarkerMain {
//    static int[][] sIds = {{}, {2, 3, 4, 5}, {3, 4, 5, 6, 8}, {1, 2, 3}, {1}, {1}, {1}};
    static int[][] sIds = {{}, {2, 3, 4, 5}, {3, 4, 5, 6}, {1, 2, 3}, {1}, {1}, {1}};
public static int puffcount=0;
    /**
     * Main driver class for replaying AutoSense data through StreamProcessor
     *
     * @param args Arguments to the program
     */

    public static void main(String[] args) {

//        String path = args[0];
        String path = "C:\\Users\\nsleheen\\DATA\\6smoker_lab_csv_data\\";

//        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 1; i < 7; i++) {
            String person = "p" + String.format("%02d", i);
            for (int sid : sIds[i]) {
                doProcess(person, sid);
            }
        }
        System.setOut(System.out);

//        System.out.println("Finished all threads");

/*        // Feature file with label
        for (int p = 1; p < 7; p++) {
            for (int sid : sIds[p]) {
                generateFeatureFile(p, sid);
            }
        }*/
System.out.println("total puff "+puffcount);
    }

    private static void generateFeatureFile(int p, int sid) {
        List<Long> left_puff_timing = new ArrayList<Long>();
        List<Long> right_puff_timing = new ArrayList<Long>();
        String path = "C:\\Users\\nsleheen\\DATA\\6smoker_lab_csv_data\\";
        try {
            for (int i = 1; i <= 2; i++) {
                Scanner in = new Scanner(new File(path + "puff_timing\\p0" + p + "_s0" + sid + "_all_puff_timestamp_" + i + ".csv"));
                while (in.hasNext()) {
                    String ss = in.next();
                    if (ss == null || ss.length() == 0) continue;
                    if (i == 1)
                        left_puff_timing.add((long) Double.parseDouble(ss));
                    else
                        right_puff_timing.add((long) Double.parseDouble(ss));
                }
            }
            String fname = "p0" + p + "_s0" + sid + "feature_vector.csv";
            String outfname = "p0" + p + "_s0" + sid + "feature_vector_with_label.csv";

            Scanner in = new Scanner(new File(path + fname));

            PrintWriter writer = new PrintWriter(path + outfname, "UTF-8");

            int totalPuff = 0;
            while (in.hasNext()) {
                String str = in.next();
                String[] toks = str.split(",");
                String s = toks[0];

                long sTime = (long) Double.parseDouble(toks[toks.length - 2]);
                long eTime = (long) Double.parseDouble(toks[toks.length - 3]);
//                System.out.println(sTime + ", "+eTime+", diff "+(eTime-sTime));

                String wrist = toks[toks.length - 1];
                int clss = 0;
                if (".rightwrist".equals(wrist)) {
                    clss = getClass(right_puff_timing, sTime, eTime);
                } else {
                    clss = getClass(left_puff_timing, sTime, eTime);
                }

                for (int i = 1; i < toks.length - 3; i++) {
                    s = s + "," + toks[i];
                }
                s = s + "," + clss;
                writer.println(s);
                totalPuff += clss;
            }
            writer.close();
            System.out.println("P" + p + ", s" + sid + ", totalPuff = " + totalPuff + " out of " + (left_puff_timing.size() + right_puff_timing.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int getClass(List<Long> puff_timing, long sTime, long eTime) {
        for (long puffTime : puff_timing)
            if (puffTime >= sTime && puffTime <= eTime)
                return 1;
        return 0;
    }

    private static void doProcess(String person, int sid) {
        String path = "C:\\Users\\nsleheen\\DATA\\6smoker_lab_csv_data\\";

        ExecutorService executor = Executors.newFixedThreadPool(4);
//        System.out.println("------------------- pid = "+person + "; sid = "+sid + "-------------------------");
        String session = "s" + String.format("%02d", sid);
//                Runnable worker = new WorkerThread(path + person + "\\"+session+"\\", args[1], args[2]);
/*        try {
            System.setOut(new PrintStream(new File(path+ person + "_"+session+ "feature_vector.csv")));
        } catch (Exception e) {
            e.printStackTrace();
        }*/
//        Runnable worker = new WorkerThread(path + person + "\\" + session + "\\", "", "", Path_To_PuffMarker_Model_File);
        String Path_To_PuffMarker_Model_File="C:\\Users\\nsleheen\\projects\\MD2Korg\\stream-processor\\_puffMarkerModel.json";
        Runnable worker = new WorkerThread(path + person + "\\" + session + "\\", "", "", Path_To_PuffMarker_Model_File);
        executor.execute(worker);
        executor.shutdown();
        while (!executor.isTerminated()) ;
    }
}
