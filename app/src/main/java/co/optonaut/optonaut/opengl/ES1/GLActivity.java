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
    private GLSurfaceView glView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.glView = new GLSurfaceView(this);
        this.glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.glView.setRenderer(new GLRenderer(this));
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
}
