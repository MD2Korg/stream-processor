# Stream Processor

mCerebrum is a configurable smartphone software platform for mobile and wearable sensors. It provides support for reliable data collection from mobile and wearable sensors, and offers real-time processing of these data.

Stream Processor is a library and data processing tool that contains online implementations
of algorithms designed to run on mCerebrum.  This codebase also be run in a standalone
fashion on most computing platforms.

It contains implementations of the following algorithms:
#### Algorithms
- cStress: A continuous stress assessment algorithm

You can find more information about MD2K software on our [software website](https://md2k.org/software) or the MD2K organization on our [MD2K website](https://md2k.org/).

## Install
Clone repository `git clone https://github.com/MD2Korg/stream-processor`

or import into Intellij IDEA through `New->Project from Version Control->Github`
- Use this url `https://github.com/MD2Korg/stream-processor`
- Check `Use auto-import`
- Check `Create directories ofr empyt content roots automatically`
- Choose `Use gralde wrapper task configuration`
- Specify a Gradle JVM `(jdk>=1.7)`
- Wait for Gradle to resolve dependencies and build project
- Define the `Project SDK` and add the same JDK you are utilizing from the previous step
- Open `Edit Configurations`
- Add `Application`
  - Main class: `CC_Main or Main`
  - Program Arguments: 
    - Directory to Cerebral Cortex data files
    - Path to `cStressModelV5.json`
    - Path to `cStressModelRIPv4.json`
    - Path to `model_puffmarker.json`
   - Specify classpath of module: `streamm-processor_main`

## Examples

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

## References
- [UbiComp 2015](http://ubicomp.org/ubicomp2015/program/accepted-papers.html)
*cStress: Towards a Gold Standard for Continuous Stress Assessment in the Mobile Environment*
Karen Hovsepian, Mustafa al'absi, Emre Ertin, Thomas Kamarck, Motoshiro Nakajima, Santosh Kumar [pdf](http://dl.acm.org/citation.cfm?id=2807526)

## Contributing
Please read our [Contributing Guidelines](https://md2k.org/software/under-the-hood/contributing) for details on the process for submitting pull requests to us.

We use the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

Our [Code of Conduct](https://md2k.org/software/CodeofConduct) is the [Contributor Covenant](https://www.contributor-covenant.org/).

Bug reports can be submitted through [JIRA](https://md2korg.atlassian.net/secure/Dashboard.jspa).

Our discussion forum can be found [here](https://discuss.md2k.org/).

## Versioning

We use [Semantic Versioning](https://semver.org/) for versioning the software which is based on the following guidelines.

MAJOR.MINOR.PATCH (example: 3.0.12)

  1. MAJOR version when incompatible API changes are made,
  2. MINOR version when functionality is added in a backwards-compatible manner, and
  3. PATCH version when backwards-compatible bug fixes are introduced.

For the versions available, see [this repository's tags](https://github.com/MD2Korg/stream-processor/tags).

## Contributors

Link to the [list of contributors](https://github.com/MD2Korg/stream-processor/graphs/contributors) who participated in this project.

## License

This project is licensed under the BSD 2-Clause - see the [license](https://md2k.org/software-under-the-hood/software-uth-license) file for details.

## Acknowledgments

* [National Institutes of Health](https://www.nih.gov/) - [Big Data to Knowledge Initiative](https://datascience.nih.gov/bd2k)
  * Grants: R01MD010362, 1UG1DA04030901, 1U54EB020404, 1R01CA190329, 1R01DE02524, R00MD010468, 3UH2DA041713, 10555SC
* [National Science Foundation](https://www.nsf.gov/)
  * Grants: 1640813, 1722646
* [Intelligence Advanced Research Projects Activity](https://www.iarpa.gov/)
  * Contract: 2017-17042800006
