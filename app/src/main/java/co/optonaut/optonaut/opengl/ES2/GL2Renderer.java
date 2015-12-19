package co.optonaut.optonaut.opengl.ES2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.opengl.Triangle;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
public class GL2Renderer implements GLSurfaceView.Renderer {
    /** Tilt the spheres a little. */
    private static final int AXIAL_TILT_DEGREES = 30;

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

    /** Object distance on the screen. move it back a bit so we can see it! */
    private static final float OBJECT_DISTANCE = -10.0f;


    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mvpMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] rotationMatrix = new float[16];

    private GL2Sphere sphere;

    private Context context;
    public volatile float angle;


    public GL2Renderer(Context context) {
        this.context = context;
    }


    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        GLES20.glClearColor(CLEAR_RED, CLEAR_GREEN, CLEAR_BLUE, CLEAR_ALPHA);

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -30, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Create a rotation for the triangle
        Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, rotationMatrix, 0);

        // Draw shape
        //triangle.draw(scratch);
        sphere.draw(scratch);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // initialize sphere
        this.sphere = new GL2Sphere(5, 2);

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
}
