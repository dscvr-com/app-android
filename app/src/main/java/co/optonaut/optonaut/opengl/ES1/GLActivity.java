package co.optonaut.optonaut.opengl.ES1;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Nilan Marktanner
 * @date 2015-12-18
 */
// source: http://www.jimscosmos.com/code/android-open-gl-texture-mapped-spheres/
public class GLActivity extends AppCompatActivity {
    /** The OpenGL view. */
    private GLSurfaceView mGlSurfaceView;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState The instance state.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mGlSurfaceView = new GLSurfaceView(this);
        this.mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.mGlSurfaceView.setRenderer(new GLRenderer(this));
        this.setContentView(this.mGlSurfaceView);
    }

    /**
     * Remember to resume the glSurface.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.mGlSurfaceView.onResume();
    }

    /**
     * Also pause the glSurface.
     */
    @Override
    protected void onPause() {
        this.mGlSurfaceView.onPause();
        super.onPause();
    }
}
