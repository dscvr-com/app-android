package co.optonaut.optonaut.record;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class SelectionPoint {
    private float[] extrinsics;
    private long globalId;
    private long ringId;
    private long localId;


    public long getRingId() {
        return ringId;
    }

    public float[] getExtrinsics() {
        return extrinsics;
    }
}
