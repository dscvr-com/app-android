package co.optonaut.optonaut.util;

import org.joda.time.DateTime;

import java.util.List;

import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-12-01
 */
public class FeedMerger {
    private FeedMerger() {

    }


    public static List<Optograph> mergeOptographsIntoFeed(List<Optograph> feed, List<Optograph> incoming) {
        List<Optograph> resultFeed;

        if(feed.isEmpty()) {
            return incoming;
        } else if (incoming.isEmpty()) {
            return feed;
        }

        Optograph newestInFeed = feed.get(0);
        Optograph oldestInFeed = feed.get(feed.size() - 1);

        Optograph newestInIncoming = incoming.get(0);
        Optograph oldestInIncoming = incoming.get(incoming.size() - 1);

        if (oldestInIncoming.getCreated_atDateTime().isAfter(newestInFeed.getCreated_atDateTime())) {
            resultFeed = incoming;
            resultFeed.addAll(feed);
        } else if (newestInIncoming.getCreated_atDateTime().isBefore(oldestInFeed.getCreated_atDateTime())) {
            resultFeed = feed;
            resultFeed.addAll(incoming);
        } else {
            // TODO: do "exhaustive" merge
            return feed;
        }
        return resultFeed;
    }


}
