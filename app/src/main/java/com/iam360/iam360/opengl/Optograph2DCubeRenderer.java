package com.iam360.iam360.opengl;

import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.iam360.iam360.sensors.CombinedMotionManager;
import com.iam360.iam360.sensors.TouchEventListener;
import com.iam360.iam360.storytelling.MarkerNode;
import com.iam360.iam360.util.Constants;

import java.util.ArrayList;
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
    private static final float V_DISTANCE = 20f;
    private float scaleFactor = 1.f;
    private float ratio = 1.f;

    private static final float DAMPING_FACTOR = 0.9f;

    private final float[] mvpMatrix = new float[16];

    private final float[] projection = new float[16];
    private final float[] camera = new float[16];
    private float[] rotationMatrix = new float[16];

    private float[] unInverseRotationMatrix = new float[16];
    private float sphereRadius = 50f;
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

        sphere = new MarkerNode(5, sphereRadius);
        sphere.initializeProgram();
        setSpherePosition(2.0f, 1.0f, 2.0f);

        for(int a=0; a < 20; a++){
            spheres.add(new MarkerNode(5,sphereRadius));
            spheres.get(spheres.size() - 1).initializeProgram();
            spheres.get(spheres.size() - 1).setTransform(sphere.getTransform());
            spheres.get(spheres.size() - 1).setInitiliazed(false);
        }

        // Set the camera position
        Matrix.setLookAtM(camera, 0,
                0.0f, 0.0f, 0.0f, // eye; location
                0.0f, 0.0f, 0.01f, // center; looking toward center
                0.0f, 1.0f, 0.0f); // up; pointing along Y-axis

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
//        Log.d("MARK2","setSpherePosition x="+x+"  y="+y+"  z="+z);
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
        TouchEventListener touchEventListener = combinedMotionManager.getTouchEventListener();
        float currTheta = touchEventListener.getTheta();
        float currPhi = touchEventListener.getPhi();
//        double longRadians = Math.toRadians(currTheta);
//        double latRadians = Math.toRadians(currPhi);

        //x=rsinφcosθ; y=rsinφsinθ; z=rcosφ
//        float x = (float) (1f * (Math.sin(currPhi) * Math.cos(currTheta)));
//        float y = (float) (1f * (Math.sin(currPhi) * Math.sin(currTheta)));
//        float z = (float) (1f * Math.cos(currPhi));


        double xy_distance = V_DISTANCE * Math.cos(currPhi);
        float x = (float) (xy_distance * Math.cos(currTheta));
        float y = (float) (V_DISTANCE * Math.sin(currPhi));
        float z = (float) (xy_distance * Math.sin(currTheta));


        Log.d("currPhicurrTheta","x = "+x+"  y = "+y+"  z = "+z);

        unInverseRotationMatrix = combinedMotionManager.getRotationMatrix();
        float[] vector = {0, 0, V_DISTANCE, 0};
        float[] newPosition = new float[4];
        Matrix.multiplyMV(newPosition, 0, unInverseRotationMatrix, 0, vector, 0);

        // rotate viewMatrix to allow for user-interaction
        float[] view = new float[16];
        rotationMatrix = combinedMotionManager.getRotationMatrixInverse();
        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);

        if (optoType!=null && optoType.equals("optograph_1")) Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y_ZOOM / scaleFactor, ratio, Z_NEAR, Z_FAR);
        else Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y / scaleFactor, ratio, Z_NEAR, Z_FAR);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);

        Log.d("onDrawFrame","newPosition[0] = "+ newPosition[0]+"  newPosition[1] = "+newPosition[1]+"  newPosition[2] = "+newPosition[2]);

        setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);

        sphere.draw(mvpMatrix);
        Log.d("currPhicurrTheta","currPhi = "+currPhi+"  currTheta = "+currTheta);
//        sphere.setCenter(currPhi, currTheta, 1f);


        sphere.setCenter(x, y, z);
//        sphere.setCenter(newPosition[0], newPosition[1], newPosition[2]);

        for(int a=0; a< spheres.size(); a++){
            if(spheres.get(a).isInitiliazed()){
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
                Log.d("MARK2","overlapSpheres addMarker .x="+sphere.getCenter().x+"  .y="+sphere.getCenter().y+"  .z="+sphere.getCenter().z);
                spheres.get(a).setCenter(sphere.getCenter().x, sphere.getCenter().y, sphere.getCenter().z);
                spheres.get(a).setInitiliazed(true);
                break;
            }
        }
    }

    public static boolean overlapSpheres(Sphere point, Sphere marker) {
        // we are using multiplications because it's faster than calling Math.pow

        Log.d("MARK2","overlapSpheres point.center.x="+point.getCenter().x+"  point.center.y="+point.getCenter().y+"  point.center.z="+point.getCenter().z);
        Log.d("MARK2","overlapSpheres marker.center.x="+marker.getCenter().x+"  marker.center.y="+marker.getCenter().y+"  marker.center.z="+marker.getCenter().z);

        float x, y, z;
        x = point.getCenter().x - marker.getCenter().x;
        y = point.getCenter().y - marker.getCenter().y;
        z = point.getCenter().z - marker.getCenter().z;

        double distance = Math.sqrt( x*x + y*y + z*z );

        Log.d("MARK2","distance == "+distance);
        Log.d("MARK2","marker.radius == "+marker.radius);

        //idk why?
        return distance <= 1;//marker.radius;
    }
}
