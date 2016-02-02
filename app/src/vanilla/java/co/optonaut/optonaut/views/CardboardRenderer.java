package co.optonaut.optonaut.views;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.squareup.picasso.Picasso;

import javax.microedition.khronos.egl.EGLConfig;

import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.util.ImageUrlBuilder;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-08
 */
public class CardboardRenderer implements CardboardView.StereoRenderer {
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private Float hfov;

    private final float[] modelViewProjection = new float[16];

    private final float[] view = new float[16];
    private float[] camera = new float[16];
    private Cube leftCube;

    private Cube rightCube;
    public CardboardRenderer() {
        initializeCubes();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up
    }

    @Override
    public void onDrawEye(Eye eye) {
        if (hfov == null) {
            // need only half hfov as each view has half size
            hfov = (eye.getFov().getLeft() + eye.getFov().getRight());
            Timber.v("hfov: %s", hfov);
        }

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, view, 0);

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        if (eye.getType() == Eye.Type.LEFT) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            this.leftCube.draw(modelViewProjection);
        } else if (eye.getType() == Eye.Type.RIGHT) {
            // Set the background frame color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            this.rightCube.draw(modelViewProjection);
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
        this.leftCube.initialize();
        this.rightCube.initialize();
    }

    private void initializeCubes() {
        this.leftCube = new Cube();
        this.rightCube = new Cube();
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
    }

    public Cube getRightCube() {
        return rightCube;
    }

    public Cube getLeftCube() {
        return leftCube;
    }

    public Float getHfov() {
        return hfov;
    }
}
