# Stream Processor - A real-time high-frequency framework for mCerebrum's data sources
[![Build Status](https://travis-ci.org/MD2Korg/stream-processor.svg)](https://travis-ci.org/MD2Korg/stream-processor)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/a1e60f1fdf66413194166d33f5fbf4f1)](https://www.codacy.com/app/twhnat/stream-processor)

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


# Release History
- 0.1.0 Initial release

# Contributors
 - Timothy Hnat ([twhnat](https://github.com/twhnat)) <twhnat@memphis.edu>
 - Karen Hovsepian ([karoaper](https://github.com/karoaper)) <karoaper@gmail.com>
 - Hillol Sarker ([hillolsarker](https://github.com/hillolsarker)) <hillolsarker@gmail.com>

# License
[BSD 2-Clause](LICENSE)

## More information
- [MD2K](https://md2k.org/)
- [Documentation and Training](http://docs.md2k.org)
- [MD2K GitHub Organization](https://github.com/MD2Korg/)

## Provide feedback or submit a bug report
[http://docs.md2k.org/feedback](http://docs.md2k.org/feedback)

# Support
[MD2K](https://md2k.org) is supported by the [National Institutes of Health](https://www.nih.gov/) [Big Data to Knowledge Initiative](https://datascience.nih.gov/bd2k) Grant **#1U54EB020404**

Team: 
[Cornell Tech](http://tech.cornell.edu/), 
[GA Tech](http://www.gatech.edu/), 
[U Memphis](http://www.memphis.edu/), 
[Northwestern](http://www.northwestern.edu/), 
[Ohio State](https://www.osu.edu/), 
[Open mHealth](http://www.openmhealth.org/), 
[Rice](http://www.rice.edu/), 
[UCLA](http://www.ucla.edu/), 
[UCSD](http://www.ucsd.edu/), 
[UCSF](http://www.ucsf.edu/), 
[U Mass](http://www.umass.edu/), 
[U Michigan](https://www.umich.edu/), 
[WVU](http://www.wvu.edu/)
