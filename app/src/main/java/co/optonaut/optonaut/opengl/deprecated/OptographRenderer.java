package co.optonaut.optonaut.opengl.deprecated;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.sensors.RotationVectorListener;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: http://www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class OptographRenderer implements GLSurfaceView.Renderer, SensorEventListener {
    private static final float FIELD_OF_VIEW_Y = 45.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private final float[] mvpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    private RotationVectorListener rotationVectorListener;

    private Sphere sphere;
    private int id = 0;
    private Bitmap texture;

    //
    private boolean forceRedrawTexture;

    // did the texture change from an external source?
    private boolean textureUpdated;

    public OptographRenderer(Context context, int id) {
        resetContent();
        this.id = id;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the camera position
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 0, // eye
                0f, 0f, 1f, // center
                0f, 1.0f, 0f); // up

        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);

        // Log.d(Constants.DEBUG_TAG, "Reinitialize onSurfaceCreated in renderer " + id);
        initializeSphere();
        if (textureUpdated) {
            reinitializeTexture();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.perspectiveM(projectionMatrix, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        redrawTexture();

        // rotate viewMatrix to allow for user-interaction
        float[] viewMatrixRotated = new float[16];
        Matrix.multiplyMM(viewMatrixRotated, 0, viewMatrix, 0, rotationMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrixRotated, 0);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        sphere.draw(mvpMatrix);
    }


    protected void reinitialize() {
        initializeSphere();
        reinitializeTexture();
    }

    private void reinitializeTexture() {
        initializeTexture();
        forceRedrawTexture = true;
    }

    private void initializeTexture() {
        if (this.sphere != null && this.texture != null) {
            this.sphere.loadGLTexture(this.texture, false);
            textureUpdated = false;
        } else {
            Log.d(Constants.DEBUG_TAG, "Reinitialize texture with no texture");
        }
    }

    private void initializeSphere() {
        this.sphere = new Sphere(5, 1);
    }

    private boolean isTextureBound() {
        if(texture != null) {
            if (sphere != null) {
                return sphere.hasTexture();
            }
        }
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // only listen for Rotationvector Sensor, and if texture is loaded
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // pipe sensor event to our rotationVectorListener and obtain inverse rotation
            rotationVectorListener.handleSensorEvent(event);
            rotationMatrix = rotationVectorListener.getRotationMatrixInverse();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private void redrawTexture() {
        // if texture was loaded but sphere has no texture yet (or it was lost), (re-)load it.
        if (forceRedrawTexture || (!isTextureBound() && texture != null)) {
            forceRedrawTexture = false;
            Log.d(Constants.DEBUG_TAG, "Force redraw in renderer " + id);
            initializeTexture();
        }
    }

    public void clearTexture() {
        if (sphere != null) {
            this.sphere.clearTexture();
        }
        this.texture = null;
        forceRedrawTexture = true;
    }

    public void resetContent() {
        this.forceRedrawTexture = false;
        this.rotationVectorListener = new RotationVectorListener();
        Matrix.setIdentityM(rotationMatrix, 0);
        initializeSphere();
        this.texture = null;
    }

    public void setTexture(Bitmap texture) {
        this.texture = texture;
        this.textureUpdated = true;
    }
}
