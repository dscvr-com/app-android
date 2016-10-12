package com.iam360.iam360.opengl;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.model.SendStory;
import com.iam360.iam360.model.SendStoryChild;
import com.iam360.iam360.model.StoryChild;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

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

    public void initStoryChildrens(){
        if(!optograph.getStory().getId().equals("") && optograph.getStory().getChildren().size() > 0){
            List<StoryChild> chldrns = optograph.getStory().getChildren();
            for(int a=0; a < chldrns.size(); a++){
                SendStoryChild stryChld = new SendStoryChild();
                stryChld.setStory_object_media_face(chldrns.get(a).getStory_object_media_face());

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

        initStoryChildrens();
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

    public void setMarker(boolean type){
        optograph2DCubeRenderer.setMarkerShown(type);
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
        return optograph2DCubeRenderer.getMyStory();
    }

}
