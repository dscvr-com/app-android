package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: http://www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class GL2Activity extends AppCompatActivity {
    private MyGLSurfaceView glView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.glView = new MyGLSurfaceView(this);
        this.setContentView(this.glView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.glView.onResume();
    }

    @Override
    protected void onPause() {
        this.glView.onPause();
        super.onPause();
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final GL2Renderer gl2Renderer;
        // TODO: add scale support
        //private final ScaleGestureDetector scaleGestureDetector;

        public MyGLSurfaceView(Context context) {
            super(context);


            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            gl2Renderer = new GL2Renderer(context);

            // scaleGestureDetector = new ScaleGestureDetector(context, new ScaleDetectorListener());

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(gl2Renderer);
        }

        /*
        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            return scaleGestureDetector.onTouchEvent(motionEvent);
        }
        */


        private class ScaleDetectorListener implements ScaleGestureDetector.OnScaleGestureListener{

            float zoomScale = 1;

            float scaleFocusX = 0;
            float scaleFocusY = 0;

            public boolean onScale(ScaleGestureDetector arg0) {
                gl2Renderer.setScale(gl2Renderer.getScale() * arg0.getScaleFactor());

                requestRender();

                return true;
            }

            public boolean onScaleBegin(ScaleGestureDetector arg0) {
                invalidate();

                scaleFocusX = arg0.getFocusX();
                scaleFocusY = arg0.getFocusY();

                return true;
            }

            public void onScaleEnd(ScaleGestureDetector arg0) {
                scaleFocusX = 0;
                scaleFocusY = 0;
            }
        }

    }



}
