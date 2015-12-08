package md2k.mCerebrum.cStress.util;

/*
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Nazir Saleneen <nsleheen@memphis.edu>
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
