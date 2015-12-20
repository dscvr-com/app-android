package co.optonaut.optonaut.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

        public MyGLSurfaceView(Context context){
            super(context);

            // Create an OpenGL ES 2.0 context
            setEGLContextClientVersion(2);

            gl2Renderer = new GL2Renderer(context);

            // Set the Renderer for drawing on the GLSurfaceView
            setRenderer(gl2Renderer);
        }
    }

}
