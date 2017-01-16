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
import android.widget.Toast;

import com.iam360.dscvr.BR;
import com.iam360.dscvr.CreateStoryDetailsBinding;
import com.iam360.dscvr.R;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.model.Person;
import com.iam360.dscvr.model.SendStory;
import com.iam360.dscvr.model.SendStoryChild;
import com.iam360.dscvr.model.SendStoryResponse;
import com.iam360.dscvr.model.StoryChild;
import com.iam360.dscvr.network.Api2Consumer;
import com.iam360.dscvr.sensors.CombinedMotionManager;
import com.iam360.dscvr.sensors.GestureDetectors;
import com.iam360.dscvr.util.BubbleDrawable;
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
import java.util.List;

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
    private String storyType;

    private int viewsWithSoftKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cache = Cache.open();
        String token = cache.getString(Cache.USER_TOKEN);
        apiConsumer =new Api2Consumer(token.equals("") ? null : token, "story");
        optograph = getIntent().getExtras().getParcelable("opto");
        storyType = getIntent().getExtras().getString("type");
        mydb = new DBHelper(this);

        if (getIntent().getExtras().getParcelable("notif")!=null) {
            new GeneralUtils().decrementBadgeCount(cache, this);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_story);
        binding.setVariable(BR.optograph, optograph);
        binding.setVariable(BR.person, optograph.getPerson());
        binding.setVariable(BR.location, optograph.getLocation());
        Log.d("StoryCreatorActivity", "storyType = "+storyType);
        if(storyType != null){
            optograph.setWithStory(true);
            BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER);
            myBubble.setCornerRadius(20);
            myBubble.setPadding(25, 25, 25, 25);
            binding.bubbleTextLayout.setBackgroundDrawable(myBubble);

            binding.optograph2dview.setBubbleTextLayout(binding.bubbleTextLayout);
            binding.optograph2dview.setBubbleText(binding.bubbleText);

            binding.optograph2dview.setStoryType(storyType);
            binding.optograph2dview.setMyAct(this);

            binding.optograph2dview.setMarker(true);

            binding.optograph2dview.setDeleteStoryMarkerImage(binding.deleteStoryMarker);
            binding.deleteStoryMarker.setOnClickListener(this);
        }


        BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER);
        myBubble.setCornerRadius(20);
        myBubble.setPadding(25, 25, 25, 25);
        binding.bubbleTextLayout.setBackgroundDrawable(myBubble);


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

                binding.storyFixTxt.setText(v.getText().toString());
                binding.storyFixTxt.setVisibility(View.VISIBLE);
                return true;
            }
            return false;
        });

        adjustIfHasSoftKeys();
        getWindow().getDecorView().setSystemUiVisibility(viewsWithSoftKey);

        initStoryChildrens();
    }

    public void initStoryChildrens() {
        if(optograph.getStory() != null && optograph.getStory().getId() != null &&!optograph.getStory().getId().equals("") && optograph.getStory().getChildren().size() > 0){
            Log.d("MARK","initStoryChildrens  optograph.getStory().getId = "+optograph.getStory().getId());
            Log.d("MARK","initStoryChildrens  optograph.getStory().getChildren().size() = "+optograph.getStory().getChildren().size());
            Log.d("MARK","initStoryChildrens  optograph.getStory().getId = "+optograph.getStory().getId());
            List<StoryChild> chldrns = optograph.getStory().getChildren();
            for(int a=0; a < chldrns.size(); a++){
                Log.d("MARK","initStoryChildrens  chldrns.get(a).getStory_object_media_type() = "+chldrns.get(a).getStory_object_media_type());
                if(chldrns.get(a).getStory_object_media_type().equals("MUS")){
                    Log.d("MARK","initStoryChildrens  chldrns.get(a).getStory_object_media_fileurl() = "+chldrns.get(a).getStory_object_media_fileurl());
//                    playBGM(chldrns.get(a).getStory_object_media_fileurl());
                }else if(chldrns.get(a).getStory_object_media_type().equals("FXTXT")){
                    showFixTxt(chldrns.get(a).getStory_object_media_additional_data());
                }else{
                    SendStoryChild stryChld = new SendStoryChild();
                    stryChld.setStory_object_media_face(chldrns.get(a).getStory_object_media_face());
                    stryChld.setStory_object_media_type(chldrns.get(a).getStory_object_media_type());
//                    stryChld.setStory_object_rotation(chldrns.get(a).getStory_object_rotation());
//                    stryChld.setStory_object_position(chldrns.get(a).getStory_object_position());
                    stryChld.setStory_object_rotation(Arrays.asList("0","0","0"));
                    stryChld.setStory_object_position(Arrays.asList("0","0","0"));

                    stryChld.setStory_object_phi(String.valueOf(chldrns.get(a).getStory_object_phi()));
                    stryChld.setStory_object_theta(String.valueOf(chldrns.get(a).getStory_object_theta()));

                    stryChld.setStory_object_media_additional_data(chldrns.get(a).getStory_object_media_additional_data());

                    if(chldrns.get(a).getStory_object_phi() != null  && chldrns.get(a).getStory_object_theta() != null  && !String.valueOf(chldrns.get(a).getStory_object_phi()).equals("") && !String.valueOf(chldrns.get(a).getStory_object_theta()).equals("")){
                        binding.optograph2dview.planeSetter(stryChld);
                    }
                }
            }
//            binding.optograph2dview.setLoadingScreen(binding.loadingScreen);
        }
    }

    private void showFixTxt(String txt){
        binding.storyFixTxt.setText(txt);
        binding.storyFixTxt.setVisibility(View.VISIBLE);
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
                    binding.markerTextEdittxt.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.markerTextEdittxt, InputMethodManager.SHOW_IMPLICIT);
                }
                binding.markerTextEdittxt.setText("");
                binding.markerText.setText("");
                break;
            case R.id.add_image:
                intent = new Intent(this, ImagePickerActivity.class);
                intent.putExtra("person",optograph.getPerson());
                intent.putExtra("optoId",optograph.getId());
                intent.putExtra(ImagePickerActivity.PICKER_MODE, ImagePickerActivity.CREATE_STORY_MODE2);
                Log.d("MARK","add_image");
                startActivityForResult(intent, 11);
                break;
            case R.id.add_text:
                if(binding.staticTextEdittxt.getVisibility() == View.VISIBLE){
                    binding.staticTextEdittxt.setVisibility(View.GONE);
                }else{
                    binding.staticTextEdittxt.setVisibility(View.VISIBLE);
                    binding.staticTextEdittxt.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.staticTextEdittxt, InputMethodManager.SHOW_IMPLICIT);
                }
                break;
            case R.id.done_add:
                sendStory();
                break;
            case R.id.delete_story_marker:
                binding.optograph2dview.removeMarker();
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
                String[] separated = pathFromUri.split("/");
                String fName = separated[separated.length - 1];
                bgmMusName = fName;
                bgmMusNamePath = pathFromUri;

                SendStoryChild chld = createChildStory("MUS", "mus data");
                chld.setStory_object_media_filename(fName);
                chld.setStory_object_position(Arrays.asList("0","0","0"));
                chld.setStory_object_rotation(Arrays.asList("0","0","0"));

                binding.optograph2dview.addMarker(chld);
            }
        }else if(resultCode == RESULT_OK && requestCode == 11){
            Optograph opto = data.getExtras().getParcelable("opto");
            Log.d("MARK","onActivityResult opto_id = "+opto.getId());
            Log.d("MARK","onActivityResult opto = "+opto);

            SendStoryChild chld = createChildStory("NAV", "next optograph");
            chld.setStory_object_media_additional_data(opto.getId());

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
            stryChld.setStory_object_rotation(Arrays.asList("0","0","0"));
            stryChld.setStory_object_position(Arrays.asList("0","0","0"));
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
        SendStory stry = binding.optograph2dview.getSendStory();
        Log.d("MARK","sendStory getSendStory optograph.getStory() = "+optograph.getStory());
//        Log.d("MARK","sendStory getSendStory optograph.getStory().getId = "+DBHelper.nullChecker(optograph.getStory().getId()));
        Log.d("MARK","sendStory getSendStory getStory_optograph_id= "+DBHelper.nullChecker(stry.getStory_optograph_id()));
        Log.d("MARK","sendStory getSendStory story_person_id= "+DBHelper.nullChecker(stry.getStory_person_id()));
        Log.d("MARK","sendStory getSendStory size = "+stry.getChildren().size());
        Log.d("MARK","binding.optograph2dview.getStoryType() = "+DBHelper.nullChecker(binding.optograph2dview.getStoryType()));
        if(stry.getChildren().size() == 0){
            return;
        }
        for(int a =0; a < stry.getChildren().size(); a++){
            Log.d("MARK","sendStory getStory_object_media_type = "+stry.getChildren().get(a).getStory_object_media_type());
            Log.d("MARK","sendStory getStory_object_media_face = "+stry.getChildren().get(a).getStory_object_media_face());
            Log.d("MARK","sendStory getStory_object_media_description = "+stry.getChildren().get(a).getStory_object_media_description());
            Log.d("MARK","sendStory getStory_object_media_additional_data = "+stry.getChildren().get(a).getStory_object_media_additional_data());
            Log.d("MARK","sendStory getStory_object_position = "+stry.getChildren().get(a).getStory_object_position());
            Log.d("MARK","sendStory getStory_object_rotation = "+stry.getChildren().get(a).getStory_object_rotation());
            Log.d("MARK","sendStory getStory_object_phi = "+stry.getChildren().get(a).getStory_object_phi());
            Log.d("MARK","sendStory getStory_object_theta = "+stry.getChildren().get(a).getStory_object_theta());
            Log.d("MARK","sendStory getStory_object_media_filename = "+stry.getChildren().get(a).getStory_object_media_filename());
            Log.d("MARK","sendStory getStory_object_media_fileurl = "+stry.getChildren().get(a).getStory_object_media_fileurl());
            Log.d("MARK","sendStory getStory_object_name = "+stry.getChildren().get(a).getStory_object_name());
        }
        if(binding.optograph2dview.getStoryType().equals("edit")){
            updateStory(stry);
        }else{
            createStory(stry);
        }
    }

    private void createStory(SendStory stry){
        apiConsumer.sendStories(stry, new Callback<SendStoryResponse>() {
            @Override
            public void onResponse(Response<SendStoryResponse> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    try {
                        Log.d("MARK","createStory response.errorBody().string() = "+response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d("MARK","createStory response.message = "+response.message());
                    return;
                }
                SendStoryResponse response1 = response.body();
                boolean toUpload = false;
                String fnamePath = bgmMusNamePath;//fileNamePath.get(fileName.indexOf(r1.getStory_object_media_filename()));
                Log.d("MARK","createStory fnamePath = "+fnamePath);
                for(int a =0; a < response1.getData().getChildren().size();a++){
                    StoryChild r1 = response1.getData().getChildren().get(a);
                    try {
                        Log.d("MARK","createStory r1.getStory_object_id() = "+r1.getStory_object_id());
                        Log.d("MARK","createStory r1.getStory_object_media_type() = "+r1.getStory_object_media_type());

                        if(bgmMusNamePath != null && r1.getStory_object_media_type().equals("MUS") || r1.getStory_object_media_type().equals("IMAGE")){
                            sendStory2(fnamePath, r1.getStory_object_id(), response1.getData().getStory_id());
                            toUpload = true;
                        }
                    } catch (IOException e) {
                        Log.d("MARK","createStory SendStoryResponse error = "+e.getMessage());
                        e.printStackTrace();
                    }
                }
                if(!toUpload){
                    Toast.makeText(StoryCreatorActivity.this, "Story successfully created.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StoryCreatorActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                Log.d("MARK","createStory response.getMessage = "+response1.getMessage());
                Log.d("MARK","createStory response.getStatus = "+response1.getStatus());
                Log.d("MARK","createStory response.getData().getStory_id = "+response1.getData().getStory_id());
                Log.d("MARK","createStory response.getData().getChildren().size() = "+response1.getData().getChildren().size());
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","sendStory onFailure = "+t.getMessage());
                Toast.makeText(StoryCreatorActivity.this, "Story creation failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateStory(SendStory stry){
        apiConsumer.updateStories(optograph.getStory().getId(), stry, new Callback<SendStoryResponse>() {
            @Override
            public void onResponse(Response<SendStoryResponse> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Log.d("MARK","updateStory response.isSuccess = "+response.errorBody());
                    return;
                }
                SendStoryResponse response1 = response.body();
                boolean toUpload = false;
                for(int a =0; a < response1.getData().getChildren().size();a++){
                    StoryChild r1 = response1.getData().getChildren().get(a);
                    String fnamePath = bgmMusNamePath;//fileNamePath.get(fileName.indexOf(r1.getStory_object_media_filename()));
                    try {
                        Log.d("MARK","updateStory fnamePath = "+fnamePath);
                        Log.d("MARK","updateStory r1.getStory_object_id() = "+r1.getStory_object_id());
                        Log.d("MARK","updateStory r1.getStory_object_media_type() = "+r1.getStory_object_media_type());

                        if(bgmMusNamePath != null && r1.getStory_object_media_type().equals("MUS") || r1.getStory_object_media_type().equals("IMAGE")){
                            sendStory2(fnamePath, r1.getStory_object_id(), response1.getData().getStory_id());
                            toUpload = true;
                        }
                    } catch (IOException e) {
                        Log.d("MARK","updateStory SendStoryResponse error = "+e.getMessage());
                        e.printStackTrace();
                    }
                }
                if(!toUpload){
                    Toast.makeText(StoryCreatorActivity.this, "Story successfully updated.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(StoryCreatorActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                Log.d("MARK","updateStory response.getMessage = "+response1.getMessage());
                Log.d("MARK","updateStory response.getStatus = "+response1.getStatus());
                Log.d("MARK","updateStory response.getData().getStory_id = "+response1.getData().getStory_id());
                Log.d("MARK","updateStory response.getData().getChildren().size() = "+response1.getData().getChildren().size());
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(StoryCreatorActivity.this, "Story update failed.", Toast.LENGTH_SHORT).show();
                Log.d("MARK","updateStory onFailure = "+t.getMessage());
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
                Toast.makeText(StoryCreatorActivity.this, "Story successfully saved.", Toast.LENGTH_SHORT).show();
                Toast.makeText(StoryCreatorActivity.this, "Story successfully saved.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(StoryCreatorActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("MARK","sendStory2 onFailure = "+t.getMessage());
                try {
                    sendStory2(filePath, stry_obj_id, story_id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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