package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.R;

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


    public GL2Renderer(Context context) {
        this.context = context;
    }


    public void onDrawFrame(GL10 unused) {
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
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.scale = 1.0f;

        // initialize sphere
        this.sphere = new GL2Sphere(5, 20);

        // load texture
        this.sphere.loadGLTexture(this.context, R.drawable.abc_ic_voice_search_api_mtrl_alpha);

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.perspectiveM(projectionMatrix, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        Log.d("Optonaut", "New scale: " + String.valueOf(scale));
        this.scale = scale;
    }
}
