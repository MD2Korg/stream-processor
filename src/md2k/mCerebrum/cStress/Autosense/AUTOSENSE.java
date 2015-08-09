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

    public static final int QUALITY_GOOD = 0;
    public static final int QUALITY_NOISE = 1;
    public static final int QUALITY_BAD = 2;
    public static final int QUALITY_BAND_OFF = 3;
    public static final int QUALITY_MISSING = 4;
    public static final int QUALITY_BAND_LOOSE = 2;

    public static final int NOT_STRESSED = 0;
    public static final int STRESSED = 1;

}
