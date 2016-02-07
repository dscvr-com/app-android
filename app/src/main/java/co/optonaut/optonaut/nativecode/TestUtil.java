package co.optonaut.optonaut.nativecode;

/**
 * @author Nilan Marktanner
 * @date 2016-02-07
 */
public class TestUtil {
    static {
        System.loadLibrary("ndkmodule");
    }

    private native void test(String logThis);


    public void logNative() {
        test("This will log to LogCat via native Call");
    }
}