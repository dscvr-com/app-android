package com.iam360.iam360.opengl;

import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.iam360.iam360.sensors.CombinedMotionManager;
import com.iam360.iam360.storytelling.MarkerNode;
import com.iam360.iam360.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-17
 */
public class Optograph2DCubeRenderer implements GLSurfaceView.Renderer {
    private static final float FIELD_OF_VIEW_Y = 95.0f;
    private static final float FIELD_OF_VIEW_Y_ZOOM = 70.0f;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 120.0f;
    private float scaleFactor = 1.f;
    private float ratio = 1.f;

    private static final float DAMPING_FACTOR = 0.9f;

    private final float[] mvpMatrix = new float[16];
    private final float[] projection = new float[16];
    private final float[] camera = new float[16];
    private float[] rotationMatrix = new float[16];

    private CombinedMotionManager combinedMotionManager;

    private Cube cube;
    private String optoType;

    private MarkerNode sphere;
    private List<MarkerNode> spheres = new ArrayList<MarkerNode>();
//    private float[] markerPost = new float[16];

    public Optograph2DCubeRenderer() {
        Timber.v("cube renderer constructor");
        this.cube = new Cube();
        this.combinedMotionManager = new CombinedMotionManager(DAMPING_FACTOR, Constants.getInstance().getDisplayMetrics().widthPixels, Constants.getInstance().getDisplayMetrics().heightPixels, FIELD_OF_VIEW_Y);
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Timber.v("onSurfaceCreated");
        this.cube.initialize();

        sphere = new MarkerNode(5, 2);
        sphere.initializeProgram();
        setSpherePosition(2.0f, 1.0f, 2.0f);

        for(int a=0; a < 20; a++){
            spheres.add(new MarkerNode(5,5));
            spheres.get(spheres.size() - 1).initializeProgram();
            spheres.get(spheres.size() - 1).setTransform(sphere.getTransform());
            spheres.get(spheres.size() - 1).setInitiliazed(false);
        }

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye
                0.0f, 0.0f, 0.01f, // center
                0.0f, 1.0f, 0.0f); // up

        // Set the background frame color
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClearDepthf(1.0f);
    }

    public void setSpherePosition(float x, float y, float z) {
        //x=left/right; +right,-left
        //y=top/bot pos; +up,-down
        //z=depth; bigger number is farther
        sphere.setTransform(new float[]{
                0.01f, 0, 0, 0,
                0, 0.01f, 0, 0,
                0, 0, 0.01f, 0,
                x, y, z, 1
        });
        Log.d("MARK2","setSpherePosition x="+x+"  y="+y+"  z="+z);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Timber.v("onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
        ratio = (float) width / height;

        Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y, ratio, Z_NEAR, Z_FAR);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // rotate viewMatrix to allow for user-interaction
        float[] view = new float[16];
        rotationMatrix = combinedMotionManager.getRotationMatrixInverse();
        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);
        Log.d("MARK3","view = "+ Arrays.toString(view));

        if (optoType!=null && optoType.equals("optograph_1")) Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y_ZOOM / scaleFactor, ratio, Z_NEAR, Z_FAR);
        else Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y / scaleFactor, ratio, Z_NEAR, Z_FAR);

        Log.d("MARK3","mvpMatrix = "+ Arrays.toString(mvpMatrix));

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);

        float[] invertMvp = new float[16];
        Matrix.invertM(invertMvp, 0, mvpMatrix, 0);

        Log.d("MARK3","invertMvp = "+ Arrays.toString(invertMvp));

        setSpherePosition(invertMvp[12],invertMvp[13],invertMvp[14]);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);

        sphere.draw(mvpMatrix);
//        Log.d("MARK","sphere mvpMatrix = "+Arrays.toString(mvpMatrix));
//        Log.d("MARK","spheres size() = "+spheres.size());
        for(int a=0; a< spheres.size(); a++){
//            Log.d("MARK","spheres isInitiliazed() = "+ spheres.get(a).isInitiliazed());
            if(spheres.get(a).isInitiliazed()){
//                Log.d("MARK","spheres transform "+ Arrays.toString(spheres.get(a).getTransform()));
                Log.d("MARK2","overlapSpheres trufalse = "+overlapSpheres(sphere, spheres.get(a)));
                spheres.get(a).draw(mvpMatrix);
            }
        }
    }

    public TextureSet.TextureTarget getTextureTarget(int face) {
        return this.cube.getCubeTextureSet().getTextureTarget(face);
    }

    public void setMode(int mode) { Log.v("mcandres", "cube renderer"); combinedMotionManager.setMode(mode);}

    public void reset() {
        this.cube.resetTextures();
    }

    public void reset(String type) {
        this.optoType = type;
        this.cube.resetTextures();
    }

    public void touchStart(Point point) {
        combinedMotionManager.touchStart(point);
    }

    public void touchMove(Point point) {
        combinedMotionManager.touchMove(point);
    }

    public void touchEnd(Point point) {
        combinedMotionManager.touchEnd(point);
    }

    public void registerOnSensors() {
        combinedMotionManager.registerOnCoreMotionListener();
    }

    public void unregisterOnSensors() {
        combinedMotionManager.unregisterOnCoreMotionListener();
    }

    public boolean isRegisteredOnSensors() {
        return combinedMotionManager.isRegisteredOnCoreMotionListener();
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public CombinedMotionManager getCombinedMotionManager() {
        return combinedMotionManager;
    }

    public void setType(String type) {
        this.optoType = type;
    }

    public void addMarker(){
        for(int a=0; a< spheres.size(); a++){
//            Log.d("MARK","spheres.get(a) = "+ Arrays.toString(spheres.get(a).getTransform()));
            if(!spheres.get(a).isInitiliazed()){
                spheres.get(a).setMarkerType("text");
                spheres.get(a).setMarkerName("text-"+a);
//                Log.d("MARK","addMarker = "+ Arrays.toString(sphere.getTransform()));
                spheres.get(a).setTransform(sphere.getTransform());
                spheres.get(a).setInitiliazed(true);
                break;
            }
        }
    }

    public static boolean overlapSpheres(Sphere point, Sphere marker) {
        // we are using multiplications because it's faster than calling Math.pow
//        double distance = Math.sqrt((point.center.x - marker.center.x) * (point.center.x - marker.center.x) +
//                (point.center.y - marker.center.y) * (point.center.y - marker.center.y) +
//                (point.center.z - marker.center.z) * (point.center.z - marker.center.z));


        Log.d("MARK2","overlapSpheres point.center.x="+point.center.x+"  point.center.y="+point.center.y+"  point.center.z="+point.center.z);
        Log.d("MARK2","overlapSpheres marker.center.x="+marker.center.x+"  marker.center.y="+marker.center.y+"  marker.center.z="+marker.center.z);
        double distance = Math.sqrt((point.center.x - marker.center.x) * (point.center.x - marker.center.x) +
                (point.center.y - marker.center.y) * (point.center.y - marker.center.y) +
                (point.center.z - marker.center.z) * (point.center.z - marker.center.z));

//        float x, y, z;
//        x = point.center.x - marker.center.x;
//        y = point.center.y - marker.center.y;
//        z = point.center.z - marker.center.z;
//
//        double distance = Math.sqrt( x*x + y*y + z*z );

        Log.d("MARK2","distance == "+distance);
        Log.d("MARK2","Math.sqrt(marker.radius) == "+marker.radius);

        return distance < marker.radius;

    }
}
