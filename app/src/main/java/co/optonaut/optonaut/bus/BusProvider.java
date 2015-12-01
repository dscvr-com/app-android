package co.optonaut.optonaut.bus;

import com.squareup.otto.Bus;

/**
 * @author Nilan Marktanner
 * @date 2015-12-01
 */

// Provided by Square under the Apache License
public final class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
