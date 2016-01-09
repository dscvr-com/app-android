package co.optonaut.optonaut.views;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

import co.optonaut.optonaut.opengl.Sphere;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-08
 */
public class CardboardRendererTest implements CardboardView.StereoRenderer {

    public CardboardRendererTest() {
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        checkGLError("onNewFrame");
    }

    @Override
    public void onDrawEye(Eye eye) {
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glColorMask(true, true, true, true);
        GLES20.glDepthMask(true);
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        checkGLError("onDrawEye");
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        checkGLError("onFinishFrame");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // projection is already handled by eyes
        checkGLError("onSurfaceChanged");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        // Set the background frame color

        checkGLError("clearColor");

        checkGLError("onSurfaceCreated");
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
        checkGLError("onRendererShutdown");
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(Constants.DEBUG_TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
