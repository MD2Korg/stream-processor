package md2k.mCerebrum;

import md2k.mCerebrum.cStress.AUTOSENSE_PACKET;
import md2k.mCerebrum.cStress.cStress;

public class Main {

    public static void main(String[] args) {
	// write your code here

        TOSParser tp = new TOSParser();
        tp.importData(args[0]);

        cStress stress = new cStress();

        for(AUTOSENSE_PACKET ap: tp) {
            stress.add(ap);
        }



    }
}
