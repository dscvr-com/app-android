package com.iam360.dscvr.opengl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CircleCountDownView;
import com.iam360.dscvr.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * @author Nilan Marktanner
 * @date 2015-12-23
 */
public class Optograph2DCubeView extends GLSurfaceView {
    private Optograph2DCubeRenderer optograph2DCubeRenderer;
    private Optograph optograph;
    private ScaleGestureDetector mScaleDetector;

    private final float MIN_ZOOM = 1.0f;
    private final float MAX_ZOOM = 5.0f;
    private float mScaleFactor = MIN_ZOOM;

    private Activity myAct;
    private String storyType;
    private ImageButton deleteStoryMarkerImage;

    public Optograph2DCubeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public Optograph2DCubeView(Context context) {
        super(context);
        initialize(context);
    }

    private void initialize(Context context) {
        setEGLContextClientVersion(2);
        optograph2DCubeRenderer = new Optograph2DCubeRenderer(context);
        setRenderer(optograph2DCubeRenderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        registerRendererOnSensors();
    }

    public void setSensorMode(int mode) {
        Log.v("mcandres", "cube renderer");
        this.optograph2DCubeRenderer.setMode(mode);
    }

    public void toggleRegisteredOnSensors() {
        if (optograph2DCubeRenderer.isRegisteredOnSensors()) {
            unregisterRendererOnSensors();
        } else {
            registerRendererOnSensors();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        registerRendererOnSensors();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterRendererOnSensors();
    }

    public void registerRendererOnSensors() {
        optograph2DCubeRenderer.registerOnSensors();
    }


    public void unregisterRendererOnSensors() {
        optograph2DCubeRenderer.unregisterOnSensors();
    }

    public void initializeTextures() {
        for (int i = 0; i < Cube.FACES.length; ++i) {
            String uri = ImageUrlBuilder.buildCubeUrl(this.optograph, true, Cube.FACES[i]);
            if (optograph.is_local()) {
                Picasso.with(getContext())
                        .load(new File(uri))
                        .into(optograph2DCubeRenderer.getTextureTarget(Cube.FACES[i]));
            } else {
                Picasso.with(getContext())
                        .load(uri)
                        .into(optograph2DCubeRenderer.getTextureTarget(Cube.FACES[i]));
            }
        }
    }



    public void setOptograph(Optograph optograph) {

        // this view is set with the same optograph - abort
        optograph2DCubeRenderer.setType(optograph.getOptograph_type());

        optograph2DCubeRenderer.setWithStory(optograph.isWithStory());
        if (optograph.equals(this.optograph)) {
            return;
        }

        // this view is being reused with another optograh - reset renderer
        if (this.optograph != null) {
            optograph2DCubeRenderer.reset(optograph.getOptograph_type());
        }

        // actually set optograph
        this.optograph = optograph;
        initializeTextures();

        optograph2DCubeRenderer.setOriginalOpto(optograph);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optograph2DCubeView that = (Optograph2DCubeView) o;

        if (optograph2DCubeRenderer != null ? !optograph2DCubeRenderer.equals(that.optograph2DCubeRenderer) : that.optograph2DCubeRenderer != null)
            return false;
        return !(optograph != null ? !optograph.equals(that.optograph) : that.optograph != null);
    }

    @Override
    public int hashCode() {
        int result = optograph2DCubeRenderer != null ? optograph2DCubeRenderer.hashCode() : 0;
        result = 31 * result + (optograph != null ? optograph.hashCode() : 0);
        return result;
    }

    public OnTouchListener getOnTouchListener() {
        return (v, event) -> {
            Point point = new Point((int) event.getX(), (int) event.getY());

            //TODO uncomment for zooming
//            mScaleDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!mScaleDetector.isInProgress())
                        optograph2DCubeRenderer.touchStart(point);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!mScaleDetector.isInProgress())
                        optograph2DCubeRenderer.touchMove(point);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mScaleDetector.isInProgress())
                        optograph2DCubeRenderer.touchEnd(point);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // release touching state also for cancel action
                    if (!mScaleDetector.isInProgress())
                        optograph2DCubeRenderer.touchEnd(point);
                    break;
                default:
                    // ignore eventt
                    return false;
            }

            // we dealt with the event
            return true;
        };
    }

    public void toggleZoom() {
        if(mScaleFactor == MIN_ZOOM) {
            mScaleFactor = MAX_ZOOM;
        } else {
            mScaleFactor = MIN_ZOOM;
        }

        optograph2DCubeRenderer.setScaleFactor(mScaleFactor);
    }


    public void addMarker(SendStoryChild chld) {
        optograph2DCubeRenderer.addStoryChildren(chld);
    }

    public void planeSetter(SendStoryChild stryChld) {
        optograph2DCubeRenderer.planeSetter(stryChld);
    }

    public void setLoadingScreen(RelativeLayout loadingScreen, CircleCountDownView countDownView){
        optograph2DCubeRenderer.setLoadingScreen(loadingScreen, countDownView);
    }

    public boolean isSurfaceCreated() {
        return optograph2DCubeRenderer.isSurfaceCreated();
    }

    public void setMarker(boolean type){
        optograph2DCubeRenderer.setMarkerShown(type);
    }

    public void setBubbleText(TextView bubbleText) {
        queueEvent(new Runnable(){
            @Override
            public void run() {
                optograph2DCubeRenderer.setBubbleText(bubbleText);
            }});

    }
    public void setBubbleTextLayout(LinearLayout bubbleTextLayout) {
        queueEvent(new Runnable(){
            @Override
            public void run() {
                optograph2DCubeRenderer.setBubbleTextLayout(bubbleTextLayout);
            }
        });
    }

    public void setStoryType(String storyType) {
        this.storyType = storyType;
        queueEvent(new Runnable(){
            @Override
            public void run() {
                if(storyType.equals("create")) {
                    optograph2DCubeRenderer.setStoryType(0);
                }else if(storyType.equals("view")){
                    optograph2DCubeRenderer.setStoryType(1);
                }else if(storyType.equals("edit")){
                    optograph2DCubeRenderer.setStoryType(2);
                }
            }});
    }
     public void setDeleteStoryMarkerImage(ImageButton imgBtn){
         queueEvent(new Runnable(){
             @Override
             public void run() {
                 optograph2DCubeRenderer.setDeleteStoryMarkerImage(imgBtn);
             }});
     }

    public void removeMarker() {
        optograph2DCubeRenderer.removePin();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_ZOOM, Math.min(mScaleFactor, MAX_ZOOM));
            optograph2DCubeRenderer.setScaleFactor(mScaleFactor);
            invalidate();
            return true;
        }
    }

    public SendStory getSendStory(){
        Cache cache = Cache.open();
        SendStory stry = optograph2DCubeRenderer.getMyStory();
        stry.setStory_optograph_id(optograph.getId());
        stry.setStory_person_id(cache.getString(Cache.USER_ID));
        return stry;
    }

    public String getStoryType() {
        return storyType;
    }

    public void setMyAct(Activity myAct) {
        queueEvent(new Runnable(){
            @Override
            public void run() {
                optograph2DCubeRenderer.setActvty(myAct);
            }});
    }
}
