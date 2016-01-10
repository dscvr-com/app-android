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
public class CardboardRenderer implements CardboardView.StereoRenderer {
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private final float[] modelViewProjection = new float[16];
    private final float[] view = new float[16];
    private float[] camera = new float[16];

    private Sphere sphereLeft;
    private Bitmap textureLeft;

    public CardboardRenderer() {

    }

    public CardboardRenderer(Bitmap texture) {
        Log.d(Constants.DEBUG_TAG, "Renderer Constructor");
        this.textureLeft = texture;

    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0, 0, 0, // eye
                0f, 0f, 1f, // center
                0f, 1.0f, 0f); // up
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, view, 0);

        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f);
        if (eye.getType() == Eye.Type.LEFT) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            this.sphereLeft.draw(modelViewProjection);
        } else {
            // Set the background frame color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        // projection is already handled by eyes
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.d(Constants.DEBUG_TAG, "onSurfaceCreated");
        initializeSpheres();
        initializeTexture(Eye.Type.LEFT);
    }

    private void initializeSpheres() {
        // TODO: set eye translate to 0 instead of big sphere
        this.sphereLeft = new Sphere(3, 100);
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
    }

    private void initializeTexture(int type) {
        // Sphere sphere = getSphere(type);
        //Bitmap texture = getTexture(type);

        if (sphereLeft != null && textureLeft != null) {
            Log.d(Constants.DEBUG_TAG, "Reinitialize texture " + String.valueOf(type));
            sphereLeft.loadGLTexture(textureLeft, false);
        } else {
            Log.d(Constants.DEBUG_TAG, "Reinitialize texture with no texture");
        }
    }

    /*
    public void setTexture(Bitmap texture) {
        Log.d(Constants.DEBUG_TAG, "Texture updated!");
        initializeSpheres();
        textureLeft = texture;
        initializeTexture(Eye.Type.LEFT);
    }

    private Bitmap getTexture(int type) {
        if (type == Eye.Type.LEFT) {
            return this.textureLeft;
        } else if (type == Eye.Type.RIGHT) {
            return null;
        } else {
            throw new RuntimeException("There is no monocular texture!");
        }
    }

    private Sphere getSphere(int type) {
        if (type == Eye.Type.LEFT) {
            return this.sphereLeft;
        } else if (type == Eye.Type.RIGHT) {
            return null;
        } else {
            throw new RuntimeException("There is no monocular sphere!");
        }
    }
    */
}
