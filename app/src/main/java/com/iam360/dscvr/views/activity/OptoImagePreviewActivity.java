package com.iam360.dscvr.views.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.gson.JsonObject;
import com.iam360.dscvr.DscvrApp;
import com.iam360.dscvr.R;
import com.iam360.dscvr.bus.BusProvider;
import com.iam360.dscvr.bus.RecordFinishedEvent;
import com.iam360.dscvr.bus.RecordFinishedPreviewEvent;
import com.iam360.dscvr.model.GeocodeDetails;
import com.iam360.dscvr.model.GeocodeReverse;
import com.iam360.dscvr.model.LocationToUpdate;
import com.iam360.dscvr.model.LogInReturn;
import com.iam360.dscvr.model.OptoData;
import com.iam360.dscvr.model.OptoDataUpdate;
import com.iam360.dscvr.model.Optograph;
import com.iam360.dscvr.network.ApiConsumer;
import com.iam360.dscvr.network.PersonManager;
import com.iam360.dscvr.record.GlobalState;
import com.iam360.dscvr.util.Cache;
import com.iam360.dscvr.util.CameraUtils;
import com.iam360.dscvr.util.Constants;
import com.iam360.dscvr.util.DBHelper;
import com.iam360.dscvr.util.MixpanelHelper;
import com.iam360.dscvr.util.NotificationSender;
import com.iam360.dscvr.views.UploaderJob;
import com.iam360.dscvr.views.dialogs.GenericOKDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Mariel on 4/13/2016.
 */
public class OptoImagePreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ITEM_WIDTH = Constants.getInstance().getDisplayMetrics().widthPixels;

    @Bind(R.id.statusbar) RelativeLayout statusbar;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.exit_button) Button exitButton;
    @Bind(R.id.retry_button) Button retryButton;
    @Bind(R.id.description_box) EditText descBox;
    @Bind(R.id.post_later_button) Button postLaterIcon;
    @Bind(R.id.post_later_group) RelativeLayout postLaterButton;
    @Bind(R.id.post_later_progress) ProgressBar postLaterProgress;
    @Bind(R.id.upload_progress) RelativeLayout uploadProgress;
    @Bind(R.id.black_circle) Button blackCircle;
    @Bind(R.id.upload_button) Button uploadButton;
    @Bind(R.id.preview_image) KenBurnsView previewImage;
    @Bind(R.id.navigation_buttons) RelativeLayout navigationButtons;
    @Bind(R.id.location_layout) LinearLayout locationLayout;
    @Bind(R.id.add_location_text) TextView addLocText;
    @Bind(R.id.add_location_icon) ImageButton addLocIcon;
    @Bind(R.id.location_progress) ProgressBar locationProgress;

    @Bind(R.id.fb_share) ImageButton fbShareButton;
    @Bind(R.id.fb_progress) ProgressBar fbShareProgress;
    @Bind(R.id.twitter_share) ImageButton twitterShareButton;
    @Bind(R.id.twitter_progress) ProgressBar twitterShareProgress;
    @Bind(R.id.insta_share) ImageButton instaShareButton;

    private Optograph optographGlobal;
    private String optographId;
    protected ApiConsumer apiConsumer;
    private CallbackManager callbackManager;

    private DBHelper mydb;
    private boolean doneUpload;
    private Cache cache;
    private String userToken = "";
    private String imagePath;

    private boolean isFBShare = false;
    private boolean isTwitterShare = false;
    private boolean isInstaShare = false;

    private boolean UPLOAD_IMAGE_MODE = false;

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    private GeocodeDetails chosenLocDetails;
    private GeocodeReverse chosenLoc;
    private List<GeocodeReverse> listOfLoc;
    private Context context;

    private double longitude;
    private double latitude;

    public final static int WEBVIEW_REQUEST_CODE = 100;
    public final static String optoType360 = "optograph";
    public final static String optoType360_1 = "optograph_1";
    public final static String optoType360_3 = "optograph_3";
    public final static String optoTypeTheta = "theta";

    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     */
    static String TWITTER_CONSUMER_KEY; // place your cosumer key here
    static String TWITTER_CONSUMER_SECRET; // place your consumer secret here
    static String CALLBACK_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optoimage_preview);

        // fb initialization
        FacebookSdk.sdkInitialize(this);
        callbackManager = CallbackManager.Factory.create();
        cache = Cache.open();
        context = this;

        // get bundles
        optographId = getIntent().getStringExtra("id");
        imagePath = getIntent().getStringExtra("path");//randomUUID

        // twitter
        TWITTER_CONSUMER_KEY = getString(R.string.twitter_consumer_key);
        TWITTER_CONSUMER_SECRET = getString(R.string.twitter_consumer_secret);
        CALLBACK_URL = getString(R.string.twitter_callback_url);

        mydb = new DBHelper(this);
        userToken = cache.getString(Cache.USER_TOKEN);
        apiConsumer = new ApiConsumer(userToken.equals("")? null:userToken);
        doneUpload = false;

        Optograph optograph = new Optograph(optographId);
        optographGlobal = optograph;

        ButterKnife.bind(this, this);

        isFBShare = cache.getBoolean(Cache.POST_OPTO_TO_FB, false);
        isTwitterShare = cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);
        Log.d("myTag"," share: postToFB? "+isFBShare+" postToTwitter? "+isTwitterShare+"" +
                " userFBLoggedIn? "+cache.getBoolean(Cache.POST_OPTO_TO_FB, false)+" userTwitterLoggedIn? "+
                cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false));
        optographGlobal.setPostFacebook(isFBShare);
        optographGlobal.setPostTwitter(isTwitterShare);
        mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, isFBShare);
        mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare);

//        initializeToolbar();
        initializeShareButtons();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Timber.v("kitkat");
            statusbar.setVisibility(View.VISIBLE);
        } else {
            statusbar.setVisibility(View.GONE);
        }

        uploadButton.setOnClickListener(this);
        postLaterButton.setOnClickListener(this);
        postLaterIcon.setOnClickListener(this);
        exitButton.setOnClickListener(this);
        retryButton.setOnClickListener(this);
        fbShareButton.setOnClickListener(this);
        twitterShareButton.setOnClickListener(this);
        instaShareButton.setOnClickListener(this);
        addLocText.setOnClickListener(this);

        postLaterButton.setVisibility(View.VISIBLE);
        if(imagePath != null) {
            UPLOAD_IMAGE_MODE = true;
            // force this true
            doneUpload = true;
//            Uri imageUri = Uri.parse(imagePath);
//            previewImage.setImageURI(imageUri);
            postLaterButton.setVisibility(View.GONE);

            Timber.d("Image path : " + imagePath);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage("file://" + imagePath, previewImage);

//            Picasso.with(previewImage.getContext())
//                    .load(new File(imagePath))
//                    .placeholder(R.drawable.placeholder)
//                    .resize(ITEM_WIDTH, 400)
//                    .centerInside()
////                    .centerCrop()
//                    .into(previewImage);

//            createDefaultOptograph(optographGlobal);
        }

        if (!UPLOAD_IMAGE_MODE) {
            optograph.setOptograph_type(cache.getInt(Cache.CAMERA_MODE) ==(Constants.ONE_RING_MODE)?optoType360_1:optoType360_3);
            MixpanelHelper.trackCreateOptographPost(this);
        } else {
            optograph.setOptograph_type(optoTypeTheta);
        }

    }

    private void instatiateLocation() {
        locationProgress.setVisibility(View.VISIBLE);
        addLocText.setVisibility(View.GONE);
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String provider = locationManager.getBestProvider(criteria, false);
//        Location location = locationManager.getLastKnownLocation(provider);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location==null) location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location!=null) {
            Log.d("myTag"," location: not null lat: "+location.getLatitude()+" long: "+location.getLongitude());
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            getNearbyLocations(location.getLatitude(), location.getLongitude());
        } else {
            addLocText.setVisibility(View.VISIBLE);
            locationProgress.setVisibility(View.GONE);
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,  final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void insertLocation(String optoId) {
        UUID id = UUID.randomUUID();
        mydb.insertLocation(String.valueOf(id),"","","",latitude,longitude,chosenLocDetails.getCountry(),
                chosenLocDetails.getName(),chosenLocDetails.getCountry_short(),chosenLocDetails.getPlace(),chosenLocDetails.getRegion(),
                chosenLocDetails.isPoi());
        Cursor res = mydb.getData(optoId, DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        if (res.getCount()==0) {
            res.close();
            return;
        }
        res.close();
        mydb.updateColumnOptograph(optoId,DBHelper.OPTOGRAPH_LOCATION_ID,String.valueOf(id));
        Cursor res1 = mydb.getData(String.valueOf(id), DBHelper.LOCATION_TABLE_NAME, DBHelper.LOCATION_ID);
        res1.moveToFirst();
        if (res1.getCount()==0) {
            res1.close();
            return;
        }
        res1.close();
    }

    private void updateOptograph(Optograph opto) {
        Log.d("myTag", " upload: updateOptograph isFBShare? " + opto.isPostFacebook());
        Timber.d("isFBShare? " + opto.isPostFacebook() + " isTwitShare? " + opto.isPostTwitter() + " optoId: " + opto.getId());
        LocationToUpdate loc = null;
//        JsonObject obj = new JsonObject();
//        obj.addProperty("text",opto.getText());
//        obj.addProperty("is_private",opto.is_private());
//        obj.addProperty("post_facebook",opto.isPostFacebook());
//        obj.addProperty("post_twitter",opto.isPostTwitter());
        if (chosenLocDetails!=null) {
//            JsonObject loc = new JsonObject();
//            loc.addProperty("latitude",latitude);
//            loc.addProperty("longitude",longitude);
//            loc.addProperty("text",chosenLocDetails.getName());
//            loc.addProperty("country",chosenLocDetails.getCountry());
//            loc.addProperty("country_short",chosenLocDetails.getCountry_short());
//            loc.addProperty("place",chosenLocDetails.getPlace());
//            loc.addProperty("region",chosenLocDetails.getRegion());
//            loc.addProperty("poi",chosenLocDetails.isPoi());
//            obj.add("location",loc);
//            Log.d("myTag"," location: obj: "+obj.toString());
            loc = new LocationToUpdate(latitude,longitude,
                    chosenLocDetails.getName(),chosenLocDetails.getCountry(),chosenLocDetails.getCountry_short(),
                    chosenLocDetails.getPlace(),chosenLocDetails.getRegion(),chosenLocDetails.isPoi());
            insertLocation(opto.getId());
            Log.d("myTag"," location: updateOptograph "+loc.toString());
        }

        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter(),loc);
        Log.d("myTag", " upload: "+opto.getId() + " " + data.toString());
        apiConsumer.updateOptoData(opto.getId(), data, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", " updateOptoData: onResponse isSuccess: " + response.isSuccess());
                Log.d("myTag", " updateOptoData: onResponse body: " + response.body());
                Log.d("myTag", " updateOptoData: onResponse message: " + response.message());
                Log.d("myTag", " updateOptoData: onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", " updateOptoData location: response errorBody: " + response.errorBody() + " message: " + response.message());
                    blackCircle.setVisibility(View.GONE);
                    uploadProgress.setVisibility(View.GONE);
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Log.d("myTag", " updateOptoData location: updateOptoData: uploadimagemode: " + UPLOAD_IMAGE_MODE);
                if (!UPLOAD_IMAGE_MODE)
                    DscvrApp.getInstance().getJobManager().addJobInBackground(new UploaderJob(UUID.fromString(opto.getId()))); // upload 12images in background process
                else {
                    Snackbar.make(uploadButton, getString(R.string.image_uploaded), Snackbar.LENGTH_SHORT).show();
                    finish(); //no need to upload cube faces for theta upload
                    blackCircle.setVisibility(View.GONE);
                    uploadProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
                Log.d("myTag", " upload: updateOptoData: onFailure " + t.getMessage());
                Snackbar.make(uploadButton, "No Internet Connection.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void locationListView() {
        locationLayout.removeAllViews();
        for(GeocodeReverse loc : listOfLoc) {
            addView(locationLayout, loc);
        }
    }

    /**
     *  Api call for nearby places. Will add the locations to the view.
     * @param latitude
     * @param longitude
     */
    private void getNearbyLocations(double latitude, double longitude) {

        apiConsumer.getNearbyPlaces(String.valueOf(latitude), String.valueOf(longitude), new Callback<List<GeocodeReverse>>() {
            @Override
            public void onResponse(Response<List<GeocodeReverse>> response, Retrofit retrofit) {

                listOfLoc = response.body();
                for (GeocodeReverse loc : listOfLoc) {
                    addView(locationLayout, loc);
                }
                locationProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d(t.getMessage());
                addLocText.setVisibility(View.VISIBLE);
                locationProgress.setVisibility(View.GONE);
            }
        });
    }

    private void locationButtonState(Button view,GeocodeReverse loc) {
        Log.d("myTag", "location chosenLoc null? " + (chosenLoc == null) + " chosen==loc? " + (loc.equals(chosenLoc)));
        if (chosenLoc!=null && loc.equals(chosenLoc)) {
            chosenLoc = null;
            chosenLocDetails = null;
        } else {
            chosenLoc = loc;
            getLocationDetails(chosenLoc.getPlace_id());
        }
        if(chosenLoc!=null)Log.d("myTag","location name: "+chosenLoc.getName());
        locationListView();
    }

    /**
     * Adds the new itemview to the given view. In this case, all location views will be added to the given Linearlayout
     * @param view
     * @param loc
     */
    private void addView(final LinearLayout view, final GeocodeReverse loc) {
        final View itemView = LayoutInflater.from(this).inflate(
                R.layout.item_location, view, false);

        Button mLoc = (Button) itemView.findViewById(R.id.contact);
        ImageView mDelete = (ImageView) itemView.findViewById(R.id.contact_del);

        if (chosenLoc!=null && chosenLoc.equals(loc)) mLoc.setBackgroundResource(R.drawable.location_button_selected);
        else mLoc.setBackgroundResource(R.drawable.location_button_unselected);

        mLoc.setText(loc.getName());
        view.addView(itemView);

        Log.d("myTag","location name: "+loc.getName());

        mLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationButtonState(mLoc,loc);
                mLoc.setPressed(true);
//                mLoc.isPressed() ? mLoc.
//                view.removeView(itemView);
            }
        });

//        mDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                view.removeView(itemView);
//            }
//        });
    }

    public void getLocationDetails(String placeId) {
        apiConsumer.getLocationDetails(placeId, new Callback<GeocodeDetails>() {
            @Override
            public void onResponse(Response<GeocodeDetails> response, Retrofit retrofit) {
                if (!response.isSuccess()) return;
                GeocodeDetails det = response.body();
                if (chosenLoc==null || det==null || !det.getName().equals(chosenLoc.getName())) return;
                chosenLocDetails = det;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag"," ERROR: "+t.getMessage());
            }
        });
    }

    private void loginFacebook() {
        final List<String> PUBLISH_PERMISSIONS = Arrays.asList("publish_actions");
        LoginManager.getInstance().logInWithPublishPermissions(this, PUBLISH_PERMISSIONS);
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("myTag", " share: success login on fb: " + loginResult.getAccessToken().getUserId());

                cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                cache.save(Cache.USER_FB_LOGGED_IN, true);
                isFBShare = true;
                cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                optographGlobal.setPostFacebook(isFBShare);
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, true);
                PersonManager.updatePerson();
                initializeShareButtons();
            }

            @Override
            public void onCancel() {
                Log.d("myTag", "oncancel login on fb.");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("myTag", "onError login on fb.");
            }
        });
    }

    private void loginTwitter() {
        twitterShareProgress.setVisibility(View.VISIBLE);
        twitterShareButton.setClickable(false);
        final ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

        final Configuration configuration = builder.build();
        final TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);

                    /**
                     *  Loading twitter login page on webview for authorization
                     *  Once authorized, results are received at onActivityResult
                     *  */
                    final Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                    startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
//                    new UpdatePersonSocialData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    PersonManager.updatePerson();
                    Log.d("myTag"," share: twitter try");
                } catch (TwitterException e) {
                    Log.d("myTag"," share: twitter catch message: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("myTag", " resultCode " + Activity.RESULT_OK + " = " + resultCode + "? requestCode: " + requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            String verifier = data.getExtras().getString("oauth_verifier");
            if (verifier != null) {
                new TwitterLoggedIn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);
            } else {
                cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                optographGlobal.setPostTwitter(isTwitterShare);
                initializeShareButtons();
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare);
                twitterShareProgress.setVisibility(View.GONE);
                twitterShareButton.setClickable(true);
                PersonManager.updatePerson();
            }
        } else if (requestCode == 100) {
            twitterShareProgress.setVisibility(View.GONE);
            twitterShareButton.setClickable(true);
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class TwitterLoggedIn extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String verifier : params) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    String username = user.getName();
                    cache.save(Cache.USER_TWITTER_TOKEN, accessToken.getToken());
                    cache.save(Cache.USER_TWITTER_SECRET, accessToken.getTokenSecret());
                    cache.save(Cache.USER_TWITTER_LOGGED_IN, true);

                    Log.d("myTag", " screenName: " + accessToken.getScreenName() + " userId: " + accessToken.getUserId() + " " + accessToken.getTokenSecret());

                    Log.d("myTag", "Hello " + username);
                } catch (Exception e) {
                    Log.e("Twitter Login Failed", " Error: " + e.toString());
                    Snackbar.make(twitterShareButton, "Twitter Login Failed.", Snackbar.LENGTH_SHORT).show();isTwitterShare = !cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);
                    cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                    optographGlobal.setPostTwitter(isTwitterShare);
                    initializeShareButtons();
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isTwitterShare = true;
            cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
            optographGlobal.setPostTwitter(isTwitterShare);
            mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, true);
            twitterShareProgress.setVisibility(View.GONE);
            twitterShareButton.setClickable(true);
            initializeShareButtons();
        }
    }

    private boolean createDefaultOptograph(Optograph opto) {
        Cursor res = mydb.getData(opto.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        res.moveToFirst();
        boolean ret = false;
        if (res.getCount() == 0) {
            ret = true;
            mydb.insertOptograph(opto.getId(), "", cache.getString(Cache.USER_ID), "", opto.getCreated_atRFC3339(),
                    opto.getDeleted_at(), false, 0, false, false, opto.getStitcher_version(), true, false, "", true, true, false, opto.isPostFacebook(), opto.isPostTwitter(), false,
                    false, false, "", opto.getOptograph_type(), "");
        }
        return ret;
    }

    private void uploadOptonautData(Optograph optograph) {
        String optographType;
        if(UPLOAD_IMAGE_MODE) optographType = optoTypeTheta; else optographType = optoType360 ;

        OptoData data = null;
        if(optographType.equals(optoType360) && cache.getInt(Cache.CAMERA_MODE) ==(Constants.ONE_RING_MODE)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optoType360_1,Constants.PLATFORM+" "+Build.VERSION.RELEASE,Build.MODEL,Build.MANUFACTURER);
        else if(optographType.equals(optoType360) && cache.getInt(Cache.CAMERA_MODE) == (Constants.THREE_RING_MODE)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optoType360_3,Constants.PLATFORM+" "+Build.VERSION.RELEASE,Build.MODEL,Build.MANUFACTURER);
        else if(optographType.equals(optoTypeTheta)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optographType,Constants.PLATFORM+" "+Build.VERSION.RELEASE,Build.MODEL,Build.MANUFACTURER);

        Timber.d("OPTOGRAPHTYPE " + data.toString());
        Log.d("myTag"," Preview upload: "+data.toString());

        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                Log.d("myTag"," Preview upload: uploadOptoData onResponse success? "+response.isSuccess()+" id: "+optograph.getId()+" errorBody: "+response.errorBody()+" message: "+response.message());
                if (!response.isSuccess()) {
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    blackCircle.setVisibility(View.GONE);
                    uploadProgress.setVisibility(View.GONE);
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    blackCircle.setVisibility(View.GONE);
                    uploadProgress.setVisibility(View.GONE);
                    return;
                }
                optographGlobal.setIs_data_uploaded(true);
                mydb.updateColumnOptograph(optographGlobal.getId(), DBHelper.OPTOGRAPH_IS_DATA_UPLOADED, true);
                Cursor res = mydb.getData(optographGlobal.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
                res.moveToFirst();
                Log.d("myTag"," Preview upload: success upload data id: "+optograph.getId()+" isDataUploaded? "+res.getInt(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_DATA_UPLOADED)));
                // do things for success
                optographGlobal.setIs_published(true);
                if(UPLOAD_IMAGE_MODE) uploadPlaceHolder(optographGlobal);
            }

            @Override
            public void onFailure(Throwable t) {
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.INVISIBLE);
                uploadButton.setVisibility(View.VISIBLE);
//                Toast.makeText(getActivity(), "No Internet Connection.", Toast.LENGTH_SHORT).show();
                Snackbar.make(uploadButton, "No Internet Connection.", Snackbar.LENGTH_SHORT).show();
            }
        });

        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID);
        if (res == null || res.getCount() == 0) return;
        res.moveToFirst();
    }

    private void uploadPlaceHolder(Optograph opto) {
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        String holder = "";
        if(UPLOAD_IMAGE_MODE) {
            dir = new File(imagePath);
            holder = dir.getAbsolutePath();
            Timber.d("Image path : UPLOAD_IMAGE_MODE");
        } else {
            if (dir.exists()) {// remove the not notation here
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    File file = files[i];
                    if (file.isDirectory() && file.getName().equals("preview")) {
                        for (String s : file.list()) {
                            Log.d("myTag", " placeholder path to upload.");
                            holder = file.getPath() + "/" + s;
                            break;
                        }
                    } else {
                        // ignore
                    }
                }
            }
        }

        new UploadPlaceHolder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_button:
                userToken = cache.getString(Cache.USER_TOKEN);
                if(UPLOAD_IMAGE_MODE) createDefaultOptograph(optographGlobal);
//                if ((userToken == null || userToken.equals(""))) {
//                    Snackbar.make(v,context.getString(R.string.profile_login_first),Snackbar.LENGTH_SHORT).show();
//                    //TODO login page
////                    ((MainActivityRedesign) getApplicationContext()).profileDialog();
//                } else if (doneUpload) {
//                    apiConsumer = new ApiConsumer(userToken);
                    blackCircle.setVisibility(View.VISIBLE);
                    uploadProgress.setVisibility(View.VISIBLE);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, true);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_LOCAL, true);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_PERSON_ID, cache.getString(Cache.USER_ID));
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_TEXT, descBox.getText().toString());
                    optographGlobal.setText(descBox.getText().toString());
////                    getLocalImage(optograph);
//
                Log.d("myTag"," location: uploadImageMode: "+UPLOAD_IMAGE_MODE+" userToken: "+userToken+
                        " isDataUploaded? "+optographGlobal.is_data_uploaded());
                    if(UPLOAD_IMAGE_MODE) {
                        if (userToken != null && !userToken.isEmpty()) {
                            if(!optographGlobal.is_data_uploaded()) uploadOptonautData(optographGlobal);
                            else uploadPlaceHolder(optographGlobal);
                        }
                    } else {
                        updateOptograph(optographGlobal);
                        finish();
                    }
//                }

                break;
            case R.id.post_later_button:
                userToken = cache.getString(Cache.USER_TOKEN);
                if (chosenLocDetails!=null) {
                    insertLocation(optographId);
                }
//                if ((userToken == null || userToken.equals("")) && doneUpload) {
//                    //TODO login page
////                    ((MainActivityRedesign) getApplicationContext()).profileDialog();
//                } else if (doneUpload) {
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, false);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_LOCAL, true);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_TEXT, descBox.getText().toString());
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, optographGlobal.isPostFacebook());
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, optographGlobal.isPostTwitter());
                    finish();
//                }
                break;
            case R.id.exit_button:
                exitDialog();
                break;
            case R.id.retry_button:
                //retryDialog();
                exitDialog();
                break;
            case R.id.fb_share:
                Set<String> permissions = null;
                if(com.facebook.AccessToken.getCurrentAccessToken() == null)
                    sharedNotLoginDialog();
                else {
                    permissions = com.facebook.AccessToken.getCurrentAccessToken().getPermissions();

                    if (permissions.contains("publish_actions")) {
                        isFBShare = !cache.getBoolean(Cache.POST_OPTO_TO_FB, false);
                        cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                        optographGlobal.setPostFacebook(isFBShare);
                        initializeShareButtons();
                        mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, isFBShare);
                        PersonManager.updatePerson();
                    } else {
                        loginFacebook();
                    }
                }
                break;
            case R.id.twitter_share:
                userToken = cache.getString(Cache.USER_TOKEN);
                if (userToken == null || userToken.equals("")) {
                    sharedNotLoginDialog();
                    return;
                } else if (cache.getBoolean(Cache.USER_TWITTER_LOGGED_IN, false)) {
                    isTwitterShare = !cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false);
                    cache.save(Cache.POST_OPTO_TO_TWITTER, isTwitterShare);
                    optographGlobal.setPostTwitter(isTwitterShare);
                    initializeShareButtons();
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare);
                    PersonManager.updatePerson();
                    return;
                }
                loginTwitter();
                break;
            case R.id.insta_share:
                userToken = cache.getString(Cache.USER_TOKEN);
                if (userToken == null || userToken.equals("")) {
                    sharedNotLoginDialog();
                    return;
                }
                Snackbar.make(instaShareButton, "Share to Instagram will soon be available.", Snackbar.LENGTH_SHORT).show();
                break;
            case R.id.add_location_icon:
            case R.id.add_location_text:
                buildAlertMessageNoGps();
                break;//uncomment when location is active
            default:
                break;

        }

    }

    class UploadPlaceHolder extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            for (String s : params) {
                String[] s3 = s.split("/");
                String face = s3[s3.length - 1];

                uploadImage(optographGlobal, s, face);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            postLaterProgress.setVisibility(View.GONE);
            uploadProgress.setVisibility(View.GONE);
            blackCircle.setVisibility(View.GONE);
            doneUpload = true;
        }
    }

    private int flag = 2;

    private boolean uploadImage(Optograph opto, String filePath, String fileName) {
        flag = 2;
//        String[] s2 = filePath.split("/");
//        String fileName = s2[s2.length - 1];

        Log.d("myTag","filePath: "+filePath+" fileName: "+fileName);

        Bitmap bm = null;

        try {
            bm = BitmapFactory.decodeFile(filePath);
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage());
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        byte[] data = bos.toByteArray();

        RequestBody fbody = RequestBody.create(MediaType.parse("image/jpeg"), data);
        RequestBody fbodyMain = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("asset", fileName, fbody)
                .addFormDataPart("key", "placeholder") //fileName.replace(".jpg", ""))
                .build();
        Log.d("myTag", "asset: " + fileName + " key: " + fileName.replace(".jpg", ""));

        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, (UPLOAD_IMAGE_MODE ? optoTypeTheta : optoType360), new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadPlaceHolderImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());

                flag = response.isSuccess() ? 1 : 0;
                optographGlobal.setIs_place_holder_uploaded(true);
                doneUpload = true;
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, true);

                // update texts of theta
                if (UPLOAD_IMAGE_MODE) {
                    updateOptograph(optographGlobal);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_ON_SERVER, true);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_LOCAL, false);
                }

                postLaterIcon.setVisibility(View.VISIBLE);
                postLaterProgress.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
                blackCircle.setVisibility(View.GONE);
                doneUpload = true;

            }

            @Override
            public void onFailure(Throwable t) {
                Snackbar.make(uploadButton, "Failed to upload. Check internet connection.", Snackbar.LENGTH_SHORT).show();
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage() + " ");
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
                t.printStackTrace();
                flag = 0;
            }
        });
        while (flag == 2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        return (flag == 1);
    }

    private void initializeToolbar() {
        float scale = Constants.getInstance().getDisplayMetrics().density;
        int marginTop = (int) (25 * scale + 0.5f);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setMargins(0, marginTop, 0, 0); // left, top, right, bottom
        toolbar.setLayoutParams(lp);
//        FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) previewImage.getLayoutParams();
//        lp1.setMargins(0,marginTop+,0,0);
//        previewImage.setLayoutParams(lp1);
    }

    private void initializeShareButtons() {
        Log.d("myTag", "initializeShare: fb: " + cache.getBoolean(Cache.POST_OPTO_TO_FB, false) + " twitter: " + cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false));
        if (cache.getBoolean(Cache.POST_OPTO_TO_FB, false)) {
//            fbShareButton.setBackgroundColor(getResources().getColor(R.color.debugView1));
            fbShareButton.setBackgroundResource(R.drawable.facebook_share_active);
        } else /*fbShareButton.setBackgroundColor(getResources().getColor(R.color.timeAgoFontColor));*/ fbShareButton.setBackgroundResource(R.drawable.facebook_share_inactive);
        if (cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false)) {
//            twitterShareButton.setBackgroundColor(getResources().getColor(R.color.debugView1));
            twitterShareButton.setBackgroundResource(R.drawable.twitter_share_active);
        } else
//            twitterShareButton.setBackgroundColor(getResources().getColor(R.color.timeAgoFontColor));
        twitterShareButton.setBackgroundResource(R.drawable.twitter_share_inactive);
    }

    class UpdatePersonSocialData extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            PersonManager.updatePerson();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private void deleteOptographFromDB() {
        mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME_FEEDS, DBHelper.OPTOGRAPH_ID, optographGlobal.getId());
        mydb.deleteEntry(DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID, optographGlobal.getId());
    }

    private void deleteOptographFromPhone(String id) {
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + id);
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + id);

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    for (File file1 : file.listFiles()) {
                        boolean result = file1.delete();
                        Log.d("myTag", "getName: " + file1.getName() + " getPath: " + file1.getPath() + " delete: " + result);
                    }
                    boolean result = file.delete();
                    Log.d("myTag", "getName: " + file.getName() + " getPath: " + file.getPath() + " delete: " + result);
                    /*for (String s : file.list()) {
//                        Log.d("myTag", "list of file: " + s);
                        (new File(file.getPath()+"/"+s)).delete();
                    }*/
//                    optographs.add(new Optograph(file.getName()));
                } else {
                    // ignore
                }
            }
            boolean result = dir.delete();
            Log.d("myTag", "getName: " + dir.getName() + " getPath: " + dir.getPath() + " delete: " + result);
        }
    }

    private void exitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_exit_from_preview)
                .setPositiveButton(getResources().getString(R.string.dialog_discard), (dialog, which) -> {
                    //how can i call an Activity here???
//                    deleteOptograph();// error occurred with this line because the fragment was unattached before the execution finished.
                    if (doneUpload) {
                        deleteOptographFromPhone(optographId);
                        finish();
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void retryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_retry_recording)
                .setPositiveButton(getResources().getString(R.string.dialog_retry), (dialog, which) -> {
                    // how can i call the RecordingFragment here???
//                    MainActivityRedesign activity = (MainActivityRedesign) context;
//                    activity.retryRecording();
                }).setNegativeButton(getResources().getString(R.string.dialog_keep), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void sharedNotLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_login_needed)
                .setMessage(R.string.dialog_share_not_login)
                .setNegativeButton(getResources().getString(R.string.dialog_continue), (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    public void onBackPressed() {
        exitDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!GlobalState.isAnyJobRunning) {
            postLaterIcon.setVisibility(View.VISIBLE);
            postLaterProgress.setVisibility(View.GONE);
            uploadProgress.setVisibility(View.GONE);
            blackCircle.setVisibility(View.GONE);
            doneUpload = true;
        }

        // Register for preview image generation event
        BusProvider.getInstance().register(this);

        // get current location
        instatiateLocation();//uncomment for location
//        locationProgress.setVisibility(View.GONE);//remove when location is active
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister for preview image generation event
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void receivePreviewImage(RecordFinishedPreviewEvent recordFinishedPreviewEvent) {
        Timber.d("receivePreviewImage");

        //https://github.com/flavioarfaria/KenBurnsView
        previewImage.setImageBitmap(recordFinishedPreviewEvent.getPreviewImage());

        createDefaultOptograph(optographGlobal);
        if (userToken != null && !userToken.isEmpty()) {
            uploadOptonautData(optographGlobal);
        } else {
            Log.d("myTag", " must login to upload data");
        }

    }

    @Subscribe
    public void receiveFinishedImage(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("receiveFinishedImage");
        postLaterIcon.setVisibility(View.VISIBLE);
        postLaterProgress.setVisibility(View.GONE);
        uploadProgress.setVisibility(View.GONE);
        blackCircle.setVisibility(View.GONE);
        doneUpload = true;
    }

}
