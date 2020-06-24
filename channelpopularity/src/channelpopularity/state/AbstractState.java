package channelpopularity.state;

import java.util.Map;

import channelpopularity.context.ContextI;
import channelpopularity.userException.VideoDoesNotExist;
import channelpopularity.userException.NegativeViewException;
import channelpopularity.userException.VideoAlreadyPresent;
import channelpopularity.util.Results;

public abstract class AbstractState implements StateI {

    public void addVideo(String inAddFile, ContextI channelCntxt, Results result) throws VideoAlreadyPresent {

        if (channelCntxt.getVideoDataMap().containsKey(inAddFile)) {
            throw new VideoAlreadyPresent("Video is already present!");
        } else {
            result.storeResult(channelCntxt.getCurrentState() + "__VIDEO_ADDED::" + inAddFile);
            channelCntxt.setVideoDataMap(inAddFile, new VideoMetricsScore());
        }

        Double avgPopularityScore = 0.0;

        avgPopularityScore = avgCalulation(channelCntxt);

        channelCntxt.setChannelPopularityScore(avgPopularityScore);

        stateChanger(avgPopularityScore, channelCntxt);

    }

    public void calculateMetrics(String inFile, Map<String, Integer> inMetricCal, ContextI channelCntxt, Results result)
            throws NegativeViewException {

        channelCntxt.getVideoDataMap().get(inFile).update(
                inMetricCal.get(VideoProperties.VIEWS.getVideoPropertiesValue()),
                inMetricCal.get(VideoProperties.LIKES.getVideoPropertiesValue()),
                inMetricCal.get(VideoProperties.DISLIKES.getVideoPropertiesValue()));

        channelCntxt.getVideoDataMap().get(inFile).setPopularityScore();

        channelCntxt.setVideoDataMap(inFile, channelCntxt.getVideoDataMap().get(inFile));

        Double avgPopularityScore = 0.0;

        avgPopularityScore = avgCalulation(channelCntxt);
        channelCntxt.setChannelPopularityScore(avgPopularityScore);
        result.storeResult(channelCntxt.getCurrentState() + "__POPULARITY_SCORE_UPDATED::"
                + channelCntxt.getChannelPopularityScore());

        stateChanger(avgPopularityScore, channelCntxt);

    }

    public void removeVideo(String inRemoveFile, ContextI channelCntxt, Results result) throws VideoDoesNotExist {

        Double avgPopularityScore = 0.0;
        if (!channelCntxt.getVideoDataMap().containsKey(inRemoveFile)) {
            throw new VideoDoesNotExist("Video asked to remove does not exist!");
        } else {
            result.storeResult(channelCntxt.getCurrentState() + "__VIDEO_REMOVED::" + inRemoveFile);
            channelCntxt.getVideoDataMap().remove(inRemoveFile);

            avgPopularityScore = avgCalulation(channelCntxt);

            channelCntxt.setChannelPopularityScore(avgPopularityScore);

            stateChanger(avgPopularityScore, channelCntxt);

        }

    }

    protected double avgCalulation(ContextI channelCntxt) {
        Double avgPopularityScore = 0.0;
        for (Map.Entry<String, VideoMetricsScore> entry : channelCntxt.getVideoDataMap().entrySet()) {
            avgPopularityScore += entry.getValue().getPopularityScore();
        }
        if((avgPopularityScore /= channelCntxt.getVideoDataMap().size()) < 0){
            avgPopularityScore = 0.0; 
        }
        return avgPopularityScore;
    }

    protected void stateChanger(double inAvgScore, ContextI channelCntxt) {

        if (inAvgScore >= (double) 0 && inAvgScore <= (double) 1000) {
            channelCntxt.setCurrentState(StateName.UNPOPULAR);
        } else if (inAvgScore > (double) 1000 && inAvgScore <= (double) 10000) {
            channelCntxt.setCurrentState(StateName.MILDLY_POPULAR);
        } else if (inAvgScore > (double) 10000 && inAvgScore <= (double) 100000) {
            channelCntxt.setCurrentState(StateName.HIGHLY_POPULAR);
        } else if (inAvgScore > (double) 100000 && inAvgScore <= (double) Integer.MAX_VALUE) {
            channelCntxt.setCurrentState(StateName.ULTRA_POPULAR);
        }

    }

}
