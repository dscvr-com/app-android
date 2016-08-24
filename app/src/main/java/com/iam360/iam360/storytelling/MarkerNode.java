package com.iam360.iam360.storytelling;

import com.iam360.iam360.opengl.Sphere;

/**
 * Created by Joven on 8/19/2016.
 */
public class MarkerNode extends Sphere {

    public String markerName;
    //text, music, story
    public String markerType;

    /**
     * Sphere constructor.
     *
     * @param depth  integer representing the split of the sphere. Will be clamped to internal variable {@code MAXIMUM_ALLOWED_DEPTH}
     * @param radius The spheres radius.
     */
    public MarkerNode(int depth, float radius) {
        super(depth, radius);
    }

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    public String getMarkerName() {
        return markerName;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public String getMarkerType() {
        return markerType;
    }
}
