package md2k.mCerebrum.cStress.tests;

import junit.framework.TestCase;
import md2k.mCerebrum.cStress.Statistics.BinnedStatistics;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Timothy Hnat <twhnat@memphis.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p/>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
public class BinnedStatisticsTest extends TestCase {

    BinnedStatistics stats;

    public void setUp() throws Exception {
        super.setUp();

        stats = new BinnedStatistics(0,1000);

        for(int i=0; i<100; i++) {
            stats.add(i);
        }

    }

    public void testReset() throws Exception {
        stats.reset();
        stats.getMean();
    }

    public void testGetMean() throws Exception {
        double mean = stats.getMean();
        assertEquals(49.5,mean, 1e-9);
    }

    public void testGetStdev() throws Exception {
        double stdev = stats.getStdev();
        assertEquals(29.011491975882, stdev, 1e-9);
    }

    public void testGetWinsorizedMean() throws Exception {
        this.setUp();
        for(int i=0; i<1000; i++) {
            stats.add(1000);
        }
        double winMean = stats.getWinsorizedMean();
        assertEquals(1000.0, winMean, 1e-9);
    }

    public void testGetWinsorizedStdev() throws Exception {
        this.setUp();
        for(int i=0; i<1000; i++) {
            stats.add(1000);
        }
        double winStd = stats.getWinsorizedStdev();
        assertEquals(0.0, winStd, 1e-9);
    }
}