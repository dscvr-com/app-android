package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: http://www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class GL2Renderer implements GLSurfaceView.Renderer {
    /** Clear colour, alpha component. */
    private static final float CLEAR_RED = 0.0f;

    /** Clear colour, alpha component. */
    private static final float CLEAR_GREEN = 0.0f;

    /** Clear colour, alpha component. */
    private static final float CLEAR_BLUE = 0.0f;

    /** Clear colour, alpha component. */
    private static final float CLEAR_ALPHA = 0.5f;

    /** Perspective setup, field of view component. */
    private static final float FIELD_OF_VIEW_Y = 45.0f;

    /** Perspective setup, near component. */
    private static final float Z_NEAR = 0.1f;

    /** Perspective setup, far component. */
    private static final float Z_FAR = 100.0f;


    private final float[] mvpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];

    private GL2Sphere sphere;

    private Context context;
    public volatile float angle;

    private volatile float scale;
    private Bitmap texture;
    private boolean isTextureChanged;

    public GL2Renderer(Context context) {
        this.context = context;
        this.isTextureChanged = false;
    }

    public GL2Renderer(Context context, Bitmap texture) {
        this.context = context;
        this.isTextureChanged = false;
        this.texture = texture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d("Optonaut", "onSurfaceCreated");
        this.scale = 1.0f;

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);

        if (isTextureChanged) {
            Log.d("Optonaut", "Texture changed in Surface Created");
            reinitialize();
            isTextureChanged = false;
        } else {
            initializeSphere();
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (isTextureChanged) {
            Log.d("Optonaut", "Texture changed in drawFrame");
            reinitialize();
            isTextureChanged = false;
        }

        GLES20.glClearColor(CLEAR_RED, CLEAR_GREEN, CLEAR_BLUE, CLEAR_ALPHA);

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 0, // eye
                0f, 0f, -10f, // center
                0f, 1.0f, 0f); // up

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Scale view
        // Matrix.scaleM(mvpMatrix, 0, scale, scale, scale);

        // Draw shape
        sphere.draw(mvpMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        Log.d("Optonaut", "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.perspectiveM(projectionMatrix, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }


    private void reinitialize() {
        initializeSphere();
        initializeTexture();
    }

    private void initializeTexture() {
        this.sphere.loadGLTexture(this.texture, false);
    }

    private void initializeSphere() {
        this.sphere = new GL2Sphere(5, 20);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        Log.d("Optonaut", "New scale: " + String.valueOf(scale));
        this.scale = scale;
    }

    public void updateTexture(Bitmap bitmap) {
        this.texture = bitmap;
        isTextureChanged = true;
    }
}
