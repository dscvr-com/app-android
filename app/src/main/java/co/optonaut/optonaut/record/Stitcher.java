package co.optonaut.optonaut.record;

/**
 * @author Nilan Marktanner
 * @date 2016-02-11
 */
public class Stitcher {
    static {
        System.loadLibrary("ndkmodule");
    }

    public static native void getResult(String path, String sharedPath);

}
