package md2k.mcerebrum;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Copyright (c) 2016, The University of Memphis, MD2K Center
 * - Nazir Saleheen <nsleheen@memphis.edu>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

public class PuffMarkerMain {
    static int[][] sIds = {{}, {2, 3, 4, 5}, {3, 4, 5, 6}, {1, 2, 3}, {1}, {1}, {1}};

    /**
     * Main driver class for replaying AutoSense data through StreamProcessor
     *
     * @param args Arguments to the program
     */

    public static void main(String[] args) {

        String path = args[0];
        String pathToPuffMarkerModelFile = args[1];

        for (int i = 1; i < 7; i++) {
            String person = "p" + String.format("%02d", i);
            for (int sid : sIds[i]) {
                doProcess(person, sid, path, pathToPuffMarkerModelFile);
            }
        }
    }


    private static void doProcess(String pid, int sid, String path, String pathToPuffMarkerModelFile) {

        ExecutorService executor = Executors.newFixedThreadPool(4);
        String session = "s" + String.format("%02d", sid);
        Runnable worker = new WorkerThread(path + pid + "\\" + session + "\\", "", "", pathToPuffMarkerModelFile);
        executor.execute(worker);
        executor.shutdown();
        while (!executor.isTerminated()) ;
    }
}
