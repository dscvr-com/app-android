package com.iam360.dscvr.views.activity;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.iam360.dscvr.BR;
import com.iam360.dscvr.CreateStoryDetailsBinding;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.SendStoryResponse;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.network.Api2Consumer;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.GeneralUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by Joven on 10/3/2016.
 */
public class StoryCreatorActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener  {

    private SensorManager sensorManager;
    private Optograph optograph;
    private boolean isFullScreenMode = false;
    private CreateStoryDetailsBinding binding;
    private Cache cache;
    private DBHelper mydb;
    private ArrayList<String> fileName;
    private ArrayList<String> fileNamePath;
    private String bgmMusName;
    private String bgmMusNamePath;
    private Person person;

    protected Api2Consumer apiConsumer;

    private int viewsWithSoftKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer =new Api2Consumer(token.equals("") ? null : token, "story");
        optograph = getIntent().getExtras().getParcelable("opto");
        mydb = new DBHelper(this);

        if (getIntent().getExtras().getParcelable("notif")!=null) {
            new GeneralUtils().decrementBadgeCount(cache, this);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_story);
        binding.setVariable(BR.optograph, optograph);
        binding.setVariable(BR.person, optograph.getPerson());
        binding.setVariable(BR.location, optograph.getLocation());

        Log.d("mytTag", " delete: opto person's id: "+optograph.getPerson().getId()+" currentUserId: "+cache.getString(Cache.USER_ID)+" isLocal? "+optograph.is_local());

        instatiateFeedDisplayButton();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        registerAccelerationListener();

        binding.optograph2dview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isFullScreenMode) {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP) {
                            //TODO uncomment for zooming
//                            binding.optograph2dview.toggleZoom();
                        }
                        //finish();
                        else toggleFullScreen();
                    } else {
                    }
                } else {
                    if (GestureDetectors.singleClickDetector.onTouchEvent(event)) {
                        if (GestureDetectors.TAP_TYPE == GestureDetectors.DOUBLE_TAP) {
                            //TODO uncomment for zooming
//                            binding.optograph2dview.toggleZoom();
                        }
                        //finish();
                        else toggleFullScreen();
                    } else {
                        // need to return true here to prevent touch-stealing of parent!
//                        return true;
                    }
                }
                return binding.optograph2dview.getOnTouchListener().onTouch(v, event);
            }
        });


        binding.closeBtn.setOnClickListener(this);
        binding.closeContainer.setOnClickListener(this);
//        binding.gyroButton.setOnClickListener(this);
//        binding.exportButton.setOnClickListener(this);
//        binding.vrButton.setOnClickListener(this);
        binding.doneAdd.setOnClickListener(this);
        binding.addMusic.setOnClickListener(this);
        binding.addText.setOnClickListener(this);
        binding.addTextPin.setOnClickListener(this);
        binding.addImage.setOnClickListener(this);

        binding.markerTextEdittxt.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) ) {
                binding.optograph2dview.addMarker(createChildStory("TXT",v.getText().toString()));
                binding.markerTextEdittxt.setVisibility(View.GONE);
                InputMethodManager inputManager = (InputMethodManager)
                        getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);

                binding.markerText.setText(v.getText().toString());
                return true;
            }
            return false;
        });
        binding.staticTextEdittxt.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) ) {
                binding.optograph2dview.addMarker(createChildStory("FXTXT",v.getText().toString()));
                binding.staticTextEdittxt.setVisibility(View.GONE);
                InputMethodManager inputManager = (InputMethodManager)
                        getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);

                binding.staticText.setText(v.getText().toString());
                binding.staticText.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        adjustIfHasSoftKeys();
        getWindow().getDecorView().setSystemUiVisibility(viewsWithSoftKey);

        binding.optograph2dview.setMarker(true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_btn:
            case R.id.close_container:
                finish();
                break;
            case R.id.add_music:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 10);
                break;
            case R.id.add_text_pin:
                if(binding.markerTextEdittxt.getVisibility() == View.VISIBLE){
                    binding.markerTextEdittxt.setVisibility(View.GONE);
                }else{
                    binding.markerTextEdittxt.setVisibility(View.VISIBLE);
                }
                binding.markerText.setText("");
                break;
            case R.id.add_image:
                intent = new Intent(this, ImagePickerActivity.class);
                intent.putExtra("person",optograph.getPerson());
                intent.putExtra(ImagePickerActivity.PICKER_MODE, ImagePickerActivity.CREATE_STORY_MODE2);
                Log.d("MARK","add_image");
                startActivityForResult(intent, 11);
                break;
            case R.id.add_text:
                if(binding.staticTextEdittxt.getVisibility() == View.VISIBLE){
                    binding.staticTextEdittxt.setVisibility(View.GONE);
                }else{
                    binding.staticTextEdittxt.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.done_add:
                sendStory();
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MARK","add_image requestCode = "+requestCode);
        Log.d("MARK","add_image resultCode = "+resultCode);

        if(resultCode == RESULT_OK && requestCode == 10){
            Uri selectedMusicUri = data.getData();
            if (selectedMusicUri != null){
                String pathFromUri = getRealPathFromURI(this, selectedMusicUri);
                Log.d("MARK","getRealPathFromURI = "+pathFromUri);
//                fileNamePath.add(pathFromUri);
                String[] separated = pathFromUri.split("/");
                String fName = separated[separated.length - 1];
//                fileName.add(fName);
                bgmMusName = fName;
                bgmMusNamePath = pathFromUri;

                SendStoryChild chld = createChildStory("MUS", "mus data");
                chld.setStory_object_media_filename(fName);
                chld.setStory_object_position(Arrays.asList("0","0","0"));
                chld.setStory_object_rotation(Arrays.asList("0","0","0"));

                binding.optograph2dview.addMarker(chld);

//                MediaPlayer mp = new MediaPlayer();
//                try {
//                    mp.setDataSource(this, Uri.parse(pathFromUri));
//                    mp.prepare();
//                    mp.start();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }else if(resultCode == RESULT_OK && requestCode == 11){
            Log.d("MARK","onActivityResult opto_id = "+data.getStringExtra("opto_id"));
            SendStoryChild chld = createChildStory("NAV", "next optograph");
            chld.setStory_object_media_additional_data(optograph.getId());
            binding.optograph2dview.addMarker(chld);
        }
    }


    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] projection = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, projection, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    private SendStoryChild createChildStory(String type, String data){
        SendStoryChild stryChld = new SendStoryChild();
        stryChld.setStory_object_media_type(type);
        stryChld.setStory_object_media_face("pin");
        stryChld.setStory_object_media_description("test descrp");
        stryChld.setStory_object_media_additional_data(data);
        if(type.equals("FXTXT") || type.equals("MUS")){
            stryChld.setStory_object_media_face("nopin");
            if(type.equals("MUS")){
                stryChld.setStory_object_media_additional_data("audio data");
            }
        }
        Log.d("MARK","addChildrenStory type = "+type+"  value = "+data);

        return stryChld;
    }

    private void registerAccelerationListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationListener() {
        sensorManager.unregisterListener(this);
    }


    private void instatiateFeedDisplayButton() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);

        if(gyro)
            binding.optograph2dview.setSensorMode(CombinedMotionManager.GYRO_MODE);
        else if(!gyro && !lilPlanet)
            binding.optograph2dview.setSensorMode(CombinedMotionManager.STILL_MODE); //PANNING MODE
        else
            binding.optograph2dview.setSensorMode(CombinedMotionManager.STILL_MODE);

//        binding.gyroButton.setBackgroundResource(gyro?R.drawable.gyro_small_active_icn:R.drawable.gyro_small_inactive);
    }

    //    http://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically
//    adjustIfHasSoftKeys and isTablet
    private void adjustIfHasSoftKeys() {
        viewsWithSoftKey = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        if(!hasMenuKey && !hasBackKey) {
            //The device has a navigation bar
            Resources resources = this.getResources();

            int orientation = getResources().getConfiguration().orientation;
            int resourceId;
            if (isTablet(this)) {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
            } else {
                resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_width", "dimen", "android");
            }

            if (resourceId > 0) {
                Log.d("myTag", " softkey: resourceId >0: " + resources.getDimensionPixelSize(resourceId) + " instance of Margin? " + (binding.profileBar.getLayoutParams() instanceof ViewGroup.MarginLayoutParams));
                if (binding.profileBar1.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) binding.profileBar1.getLayoutParams();
                    p.setMargins(0, 0, 0, resources.getDimensionPixelSize(resourceId));
                    binding.profileBar1.requestLayout();
                }
            }
        }
    }

    private boolean isTablet(Context c) {
        return (c.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void toggleFullScreen() {
        adjustIfHasSoftKeys();
        if(!isFullScreenMode) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            binding.closeContainer.setVisibility(View.INVISIBLE);
            binding.profileBar.setVisibility(View.INVISIBLE);
            isFullScreenMode = true;
        } else {
            getWindow().getDecorView().setSystemUiVisibility(viewsWithSoftKey);

            binding.closeContainer.setVisibility(View.VISIBLE);
            binding.profileBar.setVisibility(View.VISIBLE);
            isFullScreenMode = false;
        }
    }

    private void sendStory(){
        Log.d("MARK","sendStory getSendStory = "+binding.optograph2dview.getSendStory().getStory_optograph_id());
        Log.d("MARK","sendStory getSendStory size = "+binding.optograph2dview.getSendStory().getChildren().size());

        apiConsumer.sendStories(binding.optograph2dview.getSendStory(), new Callback<SendStoryResponse>() {
            @Override
            public void onResponse(Response<SendStoryResponse> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    return;
                }
                SendStoryResponse response1 = response.body();
                for(int a =0; a < response1.getData().getChildren().size();a++){
                    StoryChild r1 = response1.getData().getChildren().get(a);
                    String fnamePath = bgmMusNamePath;//fileNamePath.get(fileName.indexOf(r1.getStory_object_media_filename()));
                    try {
                        sendStory2(fnamePath, r1.getStory_object_id(), response1.getData().getStory_id());
                    } catch (IOException e) {
                        Log.d("MARK","sendStory SendStoryResponse error = "+e.getMessage());
                        e.printStackTrace();
                    }
                }
                Log.d("MARK","sendStory response.getMessage = "+response1.getMessage());
                Log.d("MARK","sendStory response.getStatus = "+response1.getStatus());
                Log.d("MARK","sendStory response.getData().getStory_id = "+response1.getData().getStory_id());
                Log.d("MARK","sendStory response.getData().getChildren().size() = "+response1.getData().getChildren().size());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","sendStory onFailure = "+t.getMessage());
            }
        });
    }

    private void sendStory2(String filePath, String stry_obj_id, String story_id) throws IOException {
        String[] separated = filePath.split("/");
        String fileName = separated[separated.length - 1];
        Log.d("MARK","sendStory2 filePath = "+filePath);
        Log.d("MARK","sendStory2 stry_obj_id = "+stry_obj_id);
        Log.d("MARK","sendStory2 story_id = "+story_id);

        InputStream fileInputStream =  new FileInputStream(filePath);

        byte[] bytes = inputStreamToByteArray(fileInputStream);

        String encoded = Base64.encodeToString(bytes, 0);
        Log.d("MARK","sendStory2 fileName = "+fileName);

        RequestBody fbody = RequestBody.create(MediaType.parse("audio/mp3"), bytes);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart(stry_obj_id, fileName, fbody)
                .addFormDataPart("story_id", story_id)
                .addFormDataPart("story_person_id", cache.getString(Cache.USER_ID))
                .addFormDataPart("story_object_ids", stry_obj_id)
                .build();

        apiConsumer.uploadBgm(fbodyMain, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("MARK","sendStory2 success");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","sendStory2 onFailure = "+t.getMessage());
            }
        });
    }

    public byte[] inputStreamToByteArray(InputStream inStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) > 0) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }
}