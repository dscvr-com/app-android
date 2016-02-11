package co.optonaut.optonaut.record;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import co.optonaut.optonaut.opengl.Cube;
import co.optonaut.optonaut.sensors.CoreMotionListener;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-02-10
 */
public class RecorderOverlayRenderer implements GLSurfaceView.Renderer {
    private List<LineNode> lineNodes;
    private Cube cube;

    private static final float FIELD_OF_VIEW_Y = 95.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;

    private final float[] mvpMatrix = new float[16];
    private final float[] projection = new float[16];
    private final float[] camera = new float[16];
    private float[] rotationMatrix = new float[16];

    boolean addedNewLineNode;


    public RecorderOverlayRenderer() {
        lineNodes = new LinkedList<>();
        addedNewLineNode = false;
        cube = new Cube();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Timber.v("onSurfaceCreated");
        cube.initialize();
        setCubePosition(0.9f, 0, 0);

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up

        // Set the background frame color as transparent!
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearDepthf(1.0f);
    }

    public void setCubePosition(float x, float y, float z) {
        cube.setCubeTransform(new float[]{
                0.01f, 0, 0, 0,
                0, 0.01f, 0, 0,
                0, 0, 0.01f, 0,
                x, y, z, 1
        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Timber.v("onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(addedNewLineNode) {
            for (LineNode node: lineNodes) {
                if (!node.isProgramInitialized()) {
                    node.initializeProgram();
                }
            }
        }

        float[] view = new float[16];
        rotationMatrix = CoreMotionListener.getInstance().getRotationMatrixInverse();

        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);

        // Draw lines
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        for (LineNode node : lineNodes) {
            node.draw(mvpMatrix);
        }

        cube.draw(mvpMatrix);
    }

    public void addChildNode(LineNode edgeNode) {
        addedNewLineNode = true;
        lineNodes.add(edgeNode);
    }
}
