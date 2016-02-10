# Stream Processor - A real-time high-frequency framework for mCerebrum's data sources
[![Build Status](https://travis-ci.org/MD2Korg/stream-processor.svg)](https://travis-ci.org/MD2Korg/stream-processor)

# Overview
Stream Processor is a library and data processing tool that contains online implementations
of algorithms designed to run on mCerebrum.  This codebase also be run in a standalone
fashion on most computing platforms.

It contains implementations of the following algorithms:
## Algorithms
- cStress: A continuous stress assessment algorithm

## References
- [UbiComp 2015](http://ubicomp.org/ubicomp2015/program/accepted-papers.html)
*cStress: Towards a Gold Standard for Continuous Stress Assessment in the Mobile Environment*
Karen Hovsepian, Mustafa al'absi, Emre Ertin, Thomas Kamarck, Motoshiro Nakajima, Santosh Kumar [pdf](http://dl.acm.org/citation.cfm?id=2807526)

# Install
Clone repository `git clone https://github.com/MD2Korg/stream-processor`

or import into Intellij IDEA through `New->Project from Version Control->Github`

# Usage
Import data for replay through Stream Processor
```
CSVParser tp = new CSVParser();
tp.importData(path + "/rip.txt", AUTOSENSE.CHEST_RIP);
tp.importData(path + "/ecg.txt", AUTOSENSE.CHEST_ECG);
tp.importData(path + "/accelx.txt", AUTOSENSE.CHEST_ACCEL_X);
tp.importData(path + "/accely.txt", AUTOSENSE.CHEST_ACCEL_Y);
tp.importData(path + "/accelz.txt", AUTOSENSE.CHEST_ACCEL_Z);

tp.sort();
```

Setup the Stream Processor object with a 60 second window, define a path for exporting data streams, and load the cStress model file.
```
int windowSize = 60000;

StreamProcessor streamProcessor = new StreamProcessor(windowSize);
streamProcessor.setPath(path);
streamProcessor.loadModel(cStressModelPath);
```

Define datapoint callbacks for `DataPoint` and `DataPointArray`.
```
streamProcessor.dpInterface = new DataPointInterface() {
    @Override
    public void dataPointHandler(String stream, DataPoint dp) {
        System.out.println(path + "/" + stream + " " + dp);
    }

    @Override
    public void dataPointArrayHandler(String stream, DataPointArray dp) {
        System.out.println(path + "/" + stream + " " + dp);
    }
};
```

Register callbacks for particular named data streams.
```
streamProcessor.registerCallbackDataArrayStream(StreamConstants.ORG_MD2K_CSTRESS_FV);
streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_ACCEL_ACTIVITY);
streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_PROBABILITY);
streamProcessor.registerCallbackDataStream(StreamConstants.ORG_MD2K_CSTRESS_STRESSLABEL);
```

Replay logic for sending data through Stream Processor and processing windows `streamProcessor.go()`.
```
    long windowStartTime = -1;
    long st = -1;
    int count = 0;
    for (CSVDataPoint ap : tp) {
        DataPoint dp = new DataPoint(ap.timestamp, ap.value);

        if (windowStartTime < 0) {
            windowStartTime = Time.nextEpochTimestamp(dp.timestamp, windowSize);
            st = System.currentTimeMillis();
        }

        if ((dp.timestamp - windowStartTime) >= windowSize) { //Process the buffer every windowSize milliseconds
            streamProcessor.go();
            windowStartTime += windowSize;
        }

        streamProcessor.add(ap.channel, dp);

    }
```

# Training
- [Stream Processor documentation](https://mhealth.md2k.org/)
- [Support forum](https://mhealth.md2k.org/)

# Release History
- 0.1.0 Initial release

# Contributors
 - Timothy Hnat <twhnat@memphis.edu>
 - Karen Hovsepian <karoper@gmail.com>
 - Hillol Sarker <hillolsarker@gmail.com>

# License
[BSD 2-Clause](LICENSE)

# Support
This research was supported by grant U54EB020404 awarded by the National Institute of Biomedical Imaging and Bioengineering (NIBIB) through funds provided by the trans-NIH Big Data to Knowledge (BD2K) initiative ([www.bd2k.nih.gov](http://www.bd2k.nih.gov)).
