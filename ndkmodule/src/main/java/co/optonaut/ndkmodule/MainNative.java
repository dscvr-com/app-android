package co.optonaut.ndkmodule;

public class MainNative {

    private static final String TAG = "MainNative";

    private native int callWithArguments(String deviceName, int width, int height);

    static {
        //NOTE: this comes from the module name that we will define in our build.gradle
        System.loadLibrary("ndkmodule");
    }

    public MainNative() {
        //TODO implement a useful constructor
    }

    public int callNativeMethod(String deviceName, int width, int height) {
        return callWithArguments(deviceName, width, height);
    }
}