
package co.optonaut.optonaut.views;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class VRModeActivity extends Activity implements SurfaceHolder.Callback
{
	// Load the gles3jni library right away to make sure JNI_OnLoad() gets called as the very first thing.
	static
	{
		System.loadLibrary( "vrcubeworld" );
	}

	private static final String TAG = "VrCubeWorld";

	private SurfaceView mView;
	private SurfaceHolder mSurfaceHolder;
	private long mNativeHandle;

	@Override protected void onCreate( Bundle icicle )
	{
		Log.v( TAG, "----------------------------------------------------------------" );
		Log.v( TAG, "VRModeActivity::onCreate()" );
		super.onCreate( icicle );

		mView = new SurfaceView( this );
		setContentView( mView );
		mView.getHolder().addCallback( this );

		// Force the screen to stay on, rather than letting it dim and shut off
		// while the user is watching a movie.
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

		// Force screen brightness to stay at maximum
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = 1.0f;
		getWindow().setAttributes( params );

		mNativeHandle = GLES3JNILib.onCreate( this );
	}

	@Override protected void onStart()
	{
		Log.v( TAG, "VRModeActivity::onStart()" );
		super.onStart();
		GLES3JNILib.onStart( mNativeHandle );
	}

	@Override protected void onResume()
	{
		Log.v( TAG, "VRModeActivity::onResume()" );
		super.onResume();
		GLES3JNILib.onResume( mNativeHandle );
	}

	@Override protected void onPause()
	{
		Log.v( TAG, "VRModeActivity::onPause()" );
		GLES3JNILib.onPause( mNativeHandle );
		super.onPause();
	}

	@Override protected void onStop()
	{
		Log.v( TAG, "VRModeActivity::onStop()" );
		GLES3JNILib.onStop( mNativeHandle );
		super.onStop();
	}

	@Override protected void onDestroy()
	{
		Log.v( TAG, "VRModeActivity::onDestroy()" );
		if ( mSurfaceHolder != null )
		{
			GLES3JNILib.onSurfaceDestroyed( mNativeHandle );
		}
		GLES3JNILib.onDestroy( mNativeHandle );
		super.onDestroy();
		mNativeHandle = 0;
	}

	@Override public void surfaceCreated( SurfaceHolder holder )
	{
		Log.v( TAG, "VRModeActivity::surfaceCreated()" );
		if ( mNativeHandle != 0 )
		{
			GLES3JNILib.onSurfaceCreated( mNativeHandle, holder.getSurface() );
			mSurfaceHolder = holder;
		}
	}

	@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
	{
		Log.v( TAG, "VRModeActivity::surfaceChanged()" );
		if ( mNativeHandle != 0 )
		{
			GLES3JNILib.onSurfaceChanged( mNativeHandle, holder.getSurface() );
			mSurfaceHolder = holder;
		}
	}
	
	@Override public void surfaceDestroyed( SurfaceHolder holder )
	{
		Log.v( TAG, "VRModeActivity::surfaceDestroyed()" );
		if ( mNativeHandle != 0 )
		{
			GLES3JNILib.onSurfaceDestroyed( mNativeHandle );
			mSurfaceHolder = null;
		}
	}

	@Override public boolean dispatchKeyEvent( KeyEvent event )
	{
		if ( mNativeHandle != 0 )
		{
			int keyCode = event.getKeyCode();
			int action = event.getAction();
			if ( action != KeyEvent.ACTION_DOWN && action != KeyEvent.ACTION_UP )
			{
				return super.dispatchKeyEvent( event );
			}
			if ( action == KeyEvent.ACTION_UP )
			{
				Log.v( TAG, "VRModeActivity::dispatchKeyEvent( " + keyCode + ", " + action + " )" );
			}
			GLES3JNILib.onKeyEvent( mNativeHandle, keyCode, action );
		}
		return true;
	}

	@Override public boolean dispatchTouchEvent( MotionEvent event )
	{
		if ( mNativeHandle != 0 )
		{
			int action = event.getAction();
			float x = event.getRawX();
			float y = event.getRawY();
			if ( action == MotionEvent.ACTION_UP )
			{
				Log.v( TAG, "VRModeActivity::dispatchTouchEvent( " + action + ", " + x + ", " + y + " )" );
			}
			GLES3JNILib.onTouchEvent( mNativeHandle, action, x, y );
		}
		return true;
	}
}
