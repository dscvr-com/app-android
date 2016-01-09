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
    private static final float FIELD_OF_VIEW_Y = 45.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private final float[] modelViewProjection = new float[16];
    private final float[] view = new float[16];
    //private float[] rotation = new float[16];
    private float[] camera = new float[16];
    private float[] headView = new float[16];

    //private RotationVectorListener rotationVectorListener;

    private Sphere sphereLeft;
    private Bitmap textureLeft;

    private Sphere sphereRight;
    private Bitmap textureRight;

    private boolean forceRedrawTexture;

    // did the texture change from an external source?
    private boolean textureLeftUpdated;
    private boolean textureRightUpdated;

    public CardboardRenderer() {
        //this.rotationVectorListener = new RotationVectorListener();
        //Matrix.setIdentityM(rotation, 0);
        this.textureLeftUpdated = false;
        this.textureRightUpdated = false;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        //redrawTexture();

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0, 0, 0, // eye
                0f, 0f, 1f, // center
                0f, 1.0f, 0f); // up

        // rotate camera to allow for user-interaction
        //Matrix.multiplyMM(camera, 0, cameraRaw, 0, rotation, 0);


        headTransform.getHeadView(headView, 0);
    }

    @Override
    public void onDrawEye(Eye eye) {
        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, view, 0);

        if (eye.getType() == Eye.Type.LEFT || eye.getType() == Eye.Type.MONOCULAR) {
            redrawTexture();

            // Set the background frame color
            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.5f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            this.sphereLeft.draw(modelViewProjection);
        } else {
            //this.sphereRight.draw(modelViewProjection);
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
        initializeSpheres();
        /*if (textureLeftUpdated) {
            reinitializeTexture(Eye.Type.LEFT);
        }
        if (textureRightUpdated) {
            reinitializeTexture(Eye.Type.RIGHT);
        }*/
    }

    private void initializeSpheres() {
        this.sphereLeft = new Sphere(5, 1);

        this.sphereRight = new Sphere(5, 1);
    }

    @Override
    public void onRendererShutdown() {
        // do nothing
    }

    public void setTexture(int type, Bitmap texture) {
        this.textureLeft = texture;
        this.textureLeftUpdated = true;
    }

    private void reinitializeTexture(int type) {
        initializeTexture(type);
        forceRedrawTexture = true;
    }

    private void initializeTexture(int type) {
        Sphere sphere = getSphere(type);
        Bitmap texture = getTexture(type);

        if (sphere != null && texture != null) {
            sphere.loadGLTexture(texture, false);
            setTextureUpdated(type, false);
        } else {
            Log.d(Constants.DEBUG_TAG, "Reinitialize texture with no texture");
        }
    }

    private void setTextureUpdated(int type, boolean textureUpdated) {
        if (type == Eye.Type.LEFT) {
            textureLeftUpdated = textureUpdated;
        } else if (type == Eye.Type.RIGHT) {
            textureRightUpdated = textureUpdated;
        } else {
            throw new RuntimeException("There is no monocular texture!");
        }
    }

    private Bitmap getTexture(int type) {
        if (type == Eye.Type.LEFT) {
            return this.textureLeft;
        } else if (type == Eye.Type.RIGHT) {
            return this.textureRight;
        } else {
            throw new RuntimeException("There is no monocular texture!");
        }
    }

    private Sphere getSphere(int type) {
        if (type == Eye.Type.LEFT) {
            return this.sphereLeft;
        } else if (type == Eye.Type.RIGHT) {
            return this.sphereRight;
        } else {
            throw new RuntimeException("There is no monocular sphere!");
        }
    }

    private void redrawTexture() {
        // if texture was loaded but sphere has no texture yet (or it was lost), (re-)load it.
        if (forceRedrawTexture || (!isTextureBound(Eye.Type.LEFT) && getTexture(Eye.Type.LEFT) != null)) {
            forceRedrawTexture = false;
            initializeTexture(Eye.Type.LEFT);
            Log.d(Constants.DEBUG_TAG, "Force redraw in cardboard renderer");
        }
    }

    private boolean isTextureBound(int type) {
        if(getTexture(type) != null) {
            if (getSphere(type) != null) {
                return getSphere(type).hasTexture();
            }
        }
        return false;
    }

    public void resetContent() {
        this.forceRedrawTexture = false;
        initializeSpheres();
        this.textureLeft = null;
        this.textureRight = null;
        this.textureLeftUpdated = false;
        this.textureRightUpdated = false;
    }

    public void clearTexture(int type) {
        if (getSphere(type) != null) {
            getSphere(type).clearTexture();
        }
        setTexture(type, null);
        forceRedrawTexture = true;
    }
}
