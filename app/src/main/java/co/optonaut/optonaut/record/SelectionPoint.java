package co.optonaut.optonaut.record;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class SelectionPoint {
    private float[] extrinsics;
    private int globalId;
    private int ringId;
    private int localId;

    public SelectionPoint(float[] extrinsics, int globalId, int ringId, int localId) {
        this.extrinsics = extrinsics;
        this.globalId = globalId;
        this.ringId = ringId;
        this.localId = localId;
    }

    public long getRingId() {
        return ringId;
    }

    public float[] getExtrinsics() {
        return extrinsics;
    }
}
