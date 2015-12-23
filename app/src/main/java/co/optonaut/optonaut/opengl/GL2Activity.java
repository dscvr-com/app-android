package co.optonaut.optonaut.opengl;

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
}
