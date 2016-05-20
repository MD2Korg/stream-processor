package md2k.mcerebrum.cstress.features;

import md2k.mcerebrum.cstress.StreamConstants;
import md2k.mcerebrum.cstress.library.datastream.DataPointStream;
import md2k.mcerebrum.cstress.library.datastream.DataStreams;
import md2k.mcerebrum.cstress.library.structs.DataPoint;

import java.util.List;

/**
 * Created by nsleheen on 5/19/2016.
 */
public class SmokingEpisodeGeneration {
    public static int PUFF_BUFFER_SIZE = 5;

    public SmokingEpisodeGeneration(DataStreams datastreams) {

        DataPointStream puffCountPerMinuteDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFF_COUNT_PER_MINUTE);
        puffCountPerMinuteDataStream.setHistoricalBufferSize(PUFF_BUFFER_SIZE);

        DataPointStream puffLabelDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_PUFFLABEL);
        int puffCount = 0;
        for (DataPoint dp : puffLabelDataStream.data)
            if(dp.value ==1)
                puffCount++;
        puffCountPerMinuteDataStream.add(new DataPoint(datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).data.get(0).timestamp, puffCount));

        DataPointStream smokingEpisodeDataStream = datastreams.getDataPointStream(StreamConstants.ORG_MD2K_PUFFMARKER_SMOKING_EPISODE);
        int totalPrevPuffCount = getSumOfLastNHistoricalValue(puffCountPerMinuteDataStream, 4) + puffCount;
        if(totalPrevPuffCount > 5) {
            System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: EPISODE :::::::::::::::::::"+puffCount +", "+(totalPrevPuffCount-puffCount));
            smokingEpisodeDataStream.add(new DataPoint(datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).data.get(0).timestamp, 1));
        }
        else
            smokingEpisodeDataStream.add(new DataPoint(datastreams.getDataPointStream(StreamConstants.ORG_MD2K_CSTRESS_DATA_RIP).data.get(0).timestamp, 0));
    }

    public int getSumOfLastNHistoricalValue(DataPointStream dataPointStream, int n) {
        List<DataPoint> listHistory = dataPointStream.getHistoricalNValues(n);
        if (listHistory.size() == 0) {
            return 0;
        }
        int total = 0;
        for (DataPoint dp : listHistory)
            total+=dp.value;
        return total;
    }
}
