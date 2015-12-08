package md2k.mCerebrum.cStress.util;

/**
 * Created by joy on 12/7/2015.
 */
public class PuffMarkerUtils {

    public static int TH_HEIGHT = 50;
    public static int GYR_MAG_FIRST_MOVING_AVG_SMOOTHING_SIZE=13;
    public static int GYR_MAG_SLOW_MOVING_AVG_SMOOTHING_SIZE=131;

    //------ Sensor KEY ------//
    public static final String KEY_DATA_RIP = "org.md2k.puffMarker.data.rip";

    public static final String KEY_DATA_ACCEL_X = "org.md2k.puffMarker.data.accel.x";
    public static final String KEY_DATA_ACCEL_Y = "org.md2k.puffMarker.data.accel.y";
    public static final String KEY_DATA_ACCEL_Z = "org.md2k.puffMarker.data.accel.z";
    public static final String KEY_DATA_GYRO_X  = "org.md2k.puffMarker.data.gyro.x";
    public static final String KEY_DATA_GYRO_Y  = "org.md2k.puffMarker.data.gyro.y";
    public static final String KEY_DATA_GYRO_Z  = "org.md2k.puffMarker.data.gyro.z";

    public static final String LEFT_WRIST= ".leftwrist";
    public static final String RIGHT_WRIST= ".rightwrist";

    public static final String KEY_DATA_LEFTWRIST_ACCEL_X =KEY_DATA_ACCEL_X + LEFT_WRIST;
    public static final String KEY_DATA_LEFTWRIST_ACCEL_Y =KEY_DATA_ACCEL_Y + LEFT_WRIST;
    public static final String KEY_DATA_LEFTWRIST_ACCEL_Z =KEY_DATA_ACCEL_Z + LEFT_WRIST;
    public static final String KEY_DATA_LEFTWRIST_GYRO_X = KEY_DATA_GYRO_X  + LEFT_WRIST;
    public static final String KEY_DATA_LEFTWRIST_GYRO_Y = KEY_DATA_GYRO_Y  + LEFT_WRIST;
    public static final String KEY_DATA_LEFTWRIST_GYRO_Z = KEY_DATA_GYRO_Z  + LEFT_WRIST;

    public static final String KEY_DATA_RIGHTWRIST_ACCEL_X =KEY_DATA_ACCEL_X + RIGHT_WRIST;
    public static final String KEY_DATA_RIGHTWRIST_ACCEL_Y =KEY_DATA_ACCEL_Y + RIGHT_WRIST;
    public static final String KEY_DATA_RIGHTWRIST_ACCEL_Z =KEY_DATA_ACCEL_Z + RIGHT_WRIST;
    public static final String KEY_DATA_RIGHTWRIST_GYRO_X = KEY_DATA_GYRO_X  + RIGHT_WRIST;
    public static final String KEY_DATA_RIGHTWRIST_GYRO_Y = KEY_DATA_GYRO_Y  + RIGHT_WRIST;
    public static final String KEY_DATA_RIGHTWRIST_GYRO_Z = KEY_DATA_GYRO_Z  + RIGHT_WRIST;
    //------ Sensor KEY ------//

    //------ Sensor ID ------//
    public static final int RIP = 1;

    public static final int LEFTWRIST_ACCEL_X = 26;
    public static final int LEFTWRIST_ACCEL_Y = 27;
    public static final int LEFTWRIST_ACCEL_Z = 28;
    public static final int LEFTWRIST_GYRO_X = 29;
    public static final int LEFTWRIST_GYRO_Y = 30;
    public static final int LEFTWRIST_GYRO_Z = 31;

    public static final int RIGHTWRIST_ACCEL_X = 33;
    public static final int RIGHTWRIST_ACCEL_Y = 34;
    public static final int RIGHTWRIST_ACCEL_Z = 35;
    public static final int RIGHTWRIST_GYRO_X = 36;
    public static final int RIGHTWRIST_GYRO_Y = 37;
    public static final int RIGHTWRIST_GYRO_Z = 38;

    public static final int[] WristIDs = {LEFTWRIST_ACCEL_X, LEFTWRIST_ACCEL_Y, LEFTWRIST_ACCEL_Z, LEFTWRIST_GYRO_X, LEFTWRIST_GYRO_Y, LEFTWRIST_GYRO_Z
            , RIGHTWRIST_ACCEL_X, RIGHTWRIST_ACCEL_Y, RIGHTWRIST_ACCEL_Z, RIGHTWRIST_GYRO_X, RIGHTWRIST_GYRO_Y, RIGHTWRIST_GYRO_Z};

    public static final int[] IDs = {RIP, LEFTWRIST_ACCEL_X, LEFTWRIST_ACCEL_Y, LEFTWRIST_ACCEL_Z, LEFTWRIST_GYRO_X, LEFTWRIST_GYRO_Y, LEFTWRIST_GYRO_Z
            , RIGHTWRIST_ACCEL_X, RIGHTWRIST_ACCEL_Y, RIGHTWRIST_ACCEL_Z, RIGHTWRIST_GYRO_X, RIGHTWRIST_GYRO_Y, RIGHTWRIST_GYRO_Z};
    //------ Sensor ID ------//

}
