package md2k.mCerebrum.cStress.Autosense;

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
public class AUTOSENSE {

    public static final int CHEST_ECG = 0;
    public static final int CHEST_ACCEL_X = 1;
    public static final int CHEST_ACCEL_Y = 2;
    public static final int CHEST_ACCEL_Z = 3;
    public static final int CHEST_GSR = 4;

    public static final int CHEST_RIP = 7;
    public static final int CHEST_BATTERY_SKINTEMP_AMBIENTTEMP = 8;

    public static final int WRIST_ACCEL_X_RIGHT = 19;
    public static final int WRIST_ACCEL_Y_RIGHT = 20;
    public static final int WRIST_ACCEL_Z_RIGHT = 21;
    public static final int WRIST_GYRO_X_RIGHT = 22;
    public static final int WRIST_GYRO_Y_RIGHT = 23;
    public static final int WRIST_GYRO_Z_RIGHT = 24;

    public static final int WRIST_ACCEL_X_LEFT = 26;
    public static final int WRIST_ACCEL_Y_LEFT = 27;
    public static final int WRIST_ACCEL_Z_LEFT = 28;
    public static final int WRIST_GYRO_X_LEFT = 29;
    public static final int WRIST_GYRO_Y_LEFT = 30;
    public static final int WRIST_GYRO_Z_LEFT = 31;


    public static final int G_QUALITY_GOOD = 0;
    public static final int G_QUALITY_MISSING = 4;
    public static final int G_QUALITY_NOISE = 1;
    public static final int G_QUALITY_BAND_LOOSE = 2;
    public static final int G_QUALITY_BAND_OFF = 3;
    public static final int G_QUALITY_BAD = 2;

    public static final int MAX_SEQ_SIZE = 4096;


}
