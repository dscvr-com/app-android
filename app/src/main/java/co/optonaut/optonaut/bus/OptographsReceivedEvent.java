package co.optonaut.optonaut.bus;

import java.util.List;

import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-12-01
 */
public class OptographsReceivedEvent {
    List<Optograph> optographs;

    public OptographsReceivedEvent(List<Optograph> optographs) {
        this.optographs = optographs;
    }

    public List<Optograph> getOptographs() {
        return optographs;
    }
}
