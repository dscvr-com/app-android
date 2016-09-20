package com.iam360.iam360.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.iam360.iam360.R;
import com.iam360.iam360.sensors.CombinedMotionManager;
import com.iam360.iam360.sensors.TouchEventListener;
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
    private static final float V_DISTANCE = 20f;
    private float scaleFactor = 1.f;
    private float ratio = 1.f;

    private static final float DAMPING_FACTOR = 0.9f;

    private final float[] mvpMatrix = new float[16];
    private final float[] mvpMatrix2 = new float[16];


    private final float[] projection = new float[16];
    private final float[] projection2 = new float[16];
    private final float[] camera = new float[16];
    private final float[] camera2 = new float[16];
    private float[] rotationMatrix = new float[16];

    private float[] unInverseRotationMatrix = new float[16];
    private float sphereRadius = 50f;
    private CombinedMotionManager combinedMotionManager;

    private Cube cube;
    private String optoType;

//    private MarkerNode sphere;
    private List<MarkerNode> spheres = new ArrayList<MarkerNode>();

    private Sphere sphere;

    private Context context;

    private float[] mProjMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mVPMatrix = new float[16];


    private int[] textures = new int[1];

    public Optograph2DCubeRenderer(Context context) {
        Timber.v("cube renderer constructor");
        this.context = context;
        this.cube = new Cube();
        this.combinedMotionManager = new CombinedMotionManager(DAMPING_FACTOR, Constants.getInstance().getDisplayMetrics().widthPixels, Constants.getInstance().getDisplayMetrics().heightPixels, FIELD_OF_VIEW_Y);
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Timber.v("onSurfaceCreated");
        // Create the GLText

        this.cube.initialize();

//        sphere = new MarkerNode(5, sphereRadius);
        sphere = new Sphere(5, sphereRadius);
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


        GLES20.glGenTextures(1, textures, 0);

        if (textures[0] == GLES20.GL_FALSE)
            throw new RuntimeException("Error loading texture");

        // bind the texture and set parameters
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load a bitmap from resources folder and pass it to OpenGL
        // in the end, we recycle it to free unneeded resources
        Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_mini_icn);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
        b.recycle();
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

        //x=rsinφcosθ; y=rsinφsinθ; z=rcosφ
//        float x = (float) (1f * (Math.sin(currPhi) * Math.cos(currTheta)));
//        float y = (float) (1f * (Math.sin(currPhi) * Math.sin(currTheta)));
//        float z = (float) (1f * Math.cos(currPhi));

        float xDeg = (float) Math.toDegrees(currPhi);
        float yDeg = (float) Math.toDegrees(currTheta);

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
        float[] view2 = new float[16];

        rotationMatrix = combinedMotionManager.getRotationMatrixInverse();
        Matrix.multiplyMM(view, 0, camera, 0, rotationMatrix, 0);
        Matrix.multiplyMM(view2, 0, camera2, 0, unInverseRotationMatrix, 0);


        if (optoType!=null && optoType.equals("optograph_1")) Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y_ZOOM / scaleFactor, ratio, Z_NEAR, Z_FAR);
        else Matrix.perspectiveM(projection, 0, FIELD_OF_VIEW_Y / scaleFactor, ratio, Z_NEAR, Z_FAR);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, projection, 0, view, 0);
        Matrix.multiplyMM(mvpMatrix2, 0, projection2, 0, view2, 0);


        Log.d("onDrawFrame","newPosition[0] = "+ newPosition[0]+"  newPosition[1] = "+newPosition[1]+"  newPosition[2] = "+newPosition[2]);

        setSpherePosition(newPosition[0], newPosition[1], newPosition[2]);

        // Draw shape
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        cube.draw(mvpMatrix);

        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        Log.d("MARK","mVPMatrix = "+ Arrays.toString(mVPMatrix));

        sphere.draw(mvpMatrix);
        Log.d("currPhicurrTheta","currPhi = "+currPhi+"  currTheta = "+currTheta);

        sphere.setCenter(x, y, z);

        for(int a=0; a< spheres.size(); a++){
            if(spheres.get(a).isInitiliazed()){
//                Log.d("MARK2","overlapSpheres trufalse = "+overlapSpheres(sphere, spheres.get(a)));
                if(overlapSpheres(sphere, spheres.get(a))){
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("MARK","marker name : "+spheres.get(a).getMarkerName());
                }
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
