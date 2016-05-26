package com.iam360.iam360.views.new_design;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.RecordFinishedEvent;
import com.iam360.iam360.bus.RecordFinishedPreviewEvent;
import com.iam360.iam360.model.GeocodeReverse;
import com.iam360.iam360.model.LogInReturn;
import com.iam360.iam360.model.OptoData;
import com.iam360.iam360.model.OptoDataUpdate;
import com.iam360.iam360.model.Optograph;
import com.iam360.iam360.network.ApiConsumer;
import com.iam360.iam360.network.PersonManager;
import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.CameraUtils;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.DBHelper;
import com.iam360.iam360.views.WebViewActivity;
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

    @Bind(R.id.statusbar) RelativeLayout statusbar;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.exit_button) Button exitButton;
    @Bind(R.id.retry_button) Button retryButton;
    @Bind(R.id.description_box) EditText descBox;
    @Bind(R.id.post_later_group) RelativeLayout postLaterButton;
    @Bind(R.id.post_later_progress) ProgressBar postLaterProgress;
    @Bind(R.id.upload_progress) RelativeLayout uploadProgress;
    @Bind(R.id.black_circle) Button blackCircle;
    @Bind(R.id.upload_button) Button uploadButton;
    @Bind(R.id.preview_image) KenBurnsView previewImage;
    @Bind(R.id.navigation_buttons) RelativeLayout navigationButtons;
    @Bind(R.id.location_layout) LinearLayout locationLayout;

    @Bind(R.id.fb_share) ImageButton fbShareButton;
    @Bind(R.id.twitter_share) ImageButton twitterShareButton;
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

    private GeocodeReverse chosenLoc;
    private Context context;

    public static final int WEBVIEW_REQUEST_CODE = 100;
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
        optographGlobal.setPostFacebook(isFBShare);
        optographGlobal.setPostTwitter(isTwitterShare);
        mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, isFBShare ? 1 : 0);
        mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare ? 1 : 0);

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
        exitButton.setOnClickListener(this);
        retryButton.setOnClickListener(this);
        fbShareButton.setOnClickListener(this);
        twitterShareButton.setOnClickListener(this);
        instaShareButton.setOnClickListener(this);

        if(imagePath != null) {
            UPLOAD_IMAGE_MODE = true;
            // force this true
            doneUpload = true;
            Uri imageUri = Uri.parse(imagePath);
            previewImage.setImageURI(imageUri);
        }

        // get current location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);

//        getNearbyLocations(location.getLatitude(), location.getLongitude());
    }

    private void updateOptograph(Optograph opto) {
        Log.d("myTag", "update optograph");
        Timber.d("isFBShare? "+opto.isPostFacebook()+" isTwitShare? "+opto.isPostTwitter()+" optoId: "+opto.getId());
        OptoDataUpdate data = new OptoDataUpdate(opto.getText(),opto.is_private(),opto.is_published(),opto.isPostFacebook(),opto.isPostTwitter());

        Log.d("myTag", opto.getId() + " " + data.toString());
        apiConsumer.updateOptoData(opto.getId(), data, new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", " onResponse isSuccess: " + response.isSuccess());
                Log.d("myTag", " onResponse body: " + response.body());
                Log.d("myTag", " onResponse message: " + response.message());
                Log.d("myTag", " onResponse raw: " + response.raw().toString());
                if (!response.isSuccess()) {
                    Log.d("myTag", "response errorBody: " + response.errorBody());
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(!UPLOAD_IMAGE_MODE) getLocalImage(opto);
                else {
//                    finish(); //no need to upload cube faces for theta upload
                    blackCircle.setVisibility(View.GONE);
                    uploadProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
                Log.d("myTag", t.getMessage());
                Snackbar.make(uploadButton, "No Internet Connection.", Snackbar.LENGTH_SHORT).show();
            }
        });
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

                List<GeocodeReverse> locations = response.body();
                for(GeocodeReverse loc : locations) {
                    addView(locationLayout, loc);
                }

            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d(t.getMessage());
            }
        });
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

        mLoc.setText(loc.getName());
        view.addView(itemView);

        mLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenLoc = loc;
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

    private void loginFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("myTag", "success login on fb: " + loginResult.getAccessToken().getUserId());

                cache.save(Cache.USER_FB_ID, loginResult.getAccessToken().getUserId());
                cache.save(Cache.USER_FB_TOKEN, loginResult.getAccessToken().getToken());
                cache.save(Cache.USER_FB_LOGGED_IN, true);
                isFBShare = true;
                cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                optographGlobal.setPostFacebook(isFBShare);
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, 1);
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
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d("myTag","resultCode "+Activity.RESULT_OK+" = "+resultCode+"? requestCode: "+requestCode);
        if (resultCode == Activity.RESULT_OK && requestCode==100) {
            String verifier = data.getExtras().getString("oauth_verifier");
            new TwitterLoggedIn().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);
        } else {
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
                    Snackbar.make(twitterShareButton, "Twitter Login Failed.", Snackbar.LENGTH_SHORT).show();
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
            mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, 1);
            initializeShareButtons();
        }
    }

    private boolean createDefaultOptograph(Optograph opto) {
        return mydb.insertOptograph(opto.getId(), "", cache.getString(Cache.USER_ID), "", opto.getCreated_atRFC3339(),
                opto.getDeleted_at(), 0, 0, 0, 0, opto.getStitcher_version(), 1, 0, "", 1, 0, 0, 0, 0,0);
    }

    private void uploadOptonautData(Optograph optograph) {
        String optographType;
        if(UPLOAD_IMAGE_MODE) optographType = optoTypeTheta; else optographType = optoType360 ;

        Log.d("mytag", "TIME : " + optograph.getCreated_atRFC3339());

        OptoData data = null;
        if(optographType.equals(optoType360) && cache.getString(Cache.CAMERA_MODE).equals(Constants.ONE_RING_MODE)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optoType360_1);
        else if(optographType.equals(optoType360) && cache.getString(Cache.CAMERA_MODE).equals(Constants.THREE_RING_MODE)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optoType360_3);
        else if(optographType.equals(optoTypeTheta)) data  = new OptoData(optograph.getId(), "0.7.0", optograph.getCreated_atRFC3339(), optographType);

        Timber.d("OPTOGRAPHTYPE " + data.toString());

        apiConsumer.uploadOptoData(data, new Callback<Optograph>() {
            @Override
            public void onResponse(Response<Optograph> response, Retrofit retrofit) {
                if (!response.isSuccess()) {
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                Optograph opto = response.body();
                if (opto == null) {
                    Snackbar.make(uploadButton, "Failed to upload.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                optographGlobal.setIs_data_uploaded(true);
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_DATA_UPLOADED, 1);
                Log.d("myTag", " success: id: " + opto.getId() + " personName: " + opto.getPerson().getUser_name());
                // do things for success
                optographGlobal.setIs_published(true);
                uploadPlaceHolder(optographGlobal);
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

        Cursor res = mydb.getData(optograph.getId(), DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID);
        if (res == null || res.getCount() == 0) return;
        res.moveToFirst();
        String stringRes = "" + DBHelper.OPTOGRAPH_ID + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_ID)) +
                "\n" + DBHelper.OPTOGRAPH_IS_PUBLISHED + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_PUBLISHED)) +
                "\n" + DBHelper.OPTOGRAPH_CREATED_AT + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_CREATED_AT)) +
                "\n" + DBHelper.OPTOGRAPH_IS_ON_SERVER + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_ON_SERVER)) +
                "\n" + DBHelper.OPTOGRAPH_TEXT + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_TEXT)) +
                "\n" + DBHelper.OPTOGRAPH_IS_STITCHER_VERSION + " " + res.getString(res.getColumnIndex(DBHelper.OPTOGRAPH_IS_STITCHER_VERSION));
//        descBox.setText(stringRes);
        Log.d("myTag", "" + stringRes);
    }

    private void uploadPlaceHolder(Optograph opto) {
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        String holder = "";
        if(UPLOAD_IMAGE_MODE) {
            dir = new File(imagePath);
            holder = dir.getAbsolutePath();
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
            Log.d("myTag", "before: ");
            int ctr = 0;
            for (boolean i : opto.getLeftFace().getStatus()) {
                Log.d("myTag", "left " + ctr + ": " + i);
                ctr += 1;
            }
            int ctr2 = 0;
            for (boolean i : opto.getRightFace().getStatus()) {
                Log.d("myTag", "right " + ctr2 + ": " + i);
                ctr2 += 1;
            }
        }

        new UploadPlaceHolder().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, holder);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_button:
                userToken = cache.getString(Cache.USER_TOKEN);
                if ((userToken == null || userToken.equals(""))) {
                    Snackbar.make(v,context.getString(R.string.profile_login_first),Snackbar.LENGTH_SHORT).show();
                    //TODO login page
//                    ((MainActivityRedesign) getApplicationContext()).profileDialog();
                } else if (doneUpload) {
                    apiConsumer = new ApiConsumer(userToken);
                    blackCircle.setVisibility(View.VISIBLE);
                    uploadProgress.setVisibility(View.VISIBLE);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, 0);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_PERSON_ID, cache.getString(Cache.USER_ID));
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_TEXT, descBox.getText().toString());
                    optographGlobal.setText(descBox.getText().toString());
//                    getLocalImage(optograph);

                    if(UPLOAD_IMAGE_MODE) {
                        createDefaultOptograph(optographGlobal);
                        if (userToken != null && !userToken.isEmpty()) {
                            uploadOptonautData(optographGlobal);
                            updateOptograph(optographGlobal);
                        }
                    } else
                        updateOptograph(optographGlobal);
                }

                break;
            case R.id.post_later_group:
                userToken = cache.getString(Cache.USER_TOKEN);
                if ((userToken == null || userToken.equals("")) && doneUpload) {
                    //TODO login page
//                    ((MainActivityRedesign) getApplicationContext()).profileDialog();
                } else if (doneUpload) {
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, 0);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_TEXT, descBox.getText().toString());
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, optographGlobal.isPostFacebook() ? 1 : 0);
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, optographGlobal.isPostTwitter() ? 1 : 0);
                    finish();
                }
                break;
            case R.id.exit_button:
                exitDialog();
                break;
            case R.id.retry_button:
                //retryDialog();
                exitDialog();
                break;
            case R.id.fb_share:
                userToken = cache.getString(Cache.USER_TOKEN);
                if (userToken == null || userToken.equals("")) {
                    sharedNotLoginDialog();
                    return;
                } else if (cache.getBoolean(Cache.USER_FB_LOGGED_IN, false)) {
                    isFBShare = !cache.getBoolean(Cache.POST_OPTO_TO_FB, false);
                    cache.save(Cache.POST_OPTO_TO_FB, isFBShare);
                    optographGlobal.setPostFacebook(isFBShare);
                    initializeShareButtons();
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_FACEBOOK, isFBShare ? 1 : 0);
                    PersonManager.updatePerson();
                    return;
                }
                loginFacebook();
                Log.d("myTag", "fbShareClicked.");
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
                    mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_POST_TWITTER, isTwitterShare ? 1 : 0);
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
                Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                String face = s3[s3.length - 1];
                Log.d("myTag", " face: " + face);

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
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_PLACEHOLDER_UPLOADED, 1);

            }

            @Override
            public void onFailure(Throwable t) {
                Snackbar.make(uploadButton, "Failed to upload. Check internet connection.",Snackbar.LENGTH_SHORT).show();
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage() + " ");
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
        Log.d("myTag", "initializeShare fb: " + cache.getBoolean(Cache.POST_OPTO_TO_FB, false) + " twitter: " + cache.getBoolean(Cache.POST_OPTO_TO_TWITTER, false));
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
        mydb.deleteEntry(DBHelper.OPTO_TABLE_NAME, DBHelper.OPTOGRAPH_ID, optographGlobal.getId());
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

    private void getLocalImage(Optograph opto) {
        cache.save(Cache.UPLOAD_ON_GOING, true);
        Log.d("myTag", "Path: " + CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());
        File dir = new File(CameraUtils.PERSISTENT_STORAGE_PATH + opto.getId());

        List<String> filePathList = new ArrayList<>();

        if (dir.exists()) {// remove the not notation here
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory() && !file.getName().contains("preview")) {
                    Log.d("myTag", "getName: " + file.getName() + " getPath: " + file.getPath());
                    for (String s : file.list()) {
                        filePathList.add(file.getPath() + "/" + s);
                    }
                } else {
                    // ignore
                }
            }
        }
        Log.d("myTag", "before: ");
        int ctr = 0;
        for (boolean i : opto.getLeftFace().getStatus()) {
            Log.d("myTag", "left " + ctr + ": " + i);
            ctr += 1;
        }
        int ctr2 = 0;
        for (boolean i : opto.getRightFace().getStatus()) {
            Log.d("myTag", "right " + ctr2 + ": " + i);
            ctr2 += 1;
        }

        new UploadCubeImages().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePathList);
    }

    // try using AbstractQueuedSynchronizer
    class UploadCubeImages extends AsyncTask<List<String>, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(List<String>... params) {
            for (List<String> sL : params) {
                for (String s : sL) {
                    String[] s3 = s.split("/");
                    Log.d("myTag", "onNext s: " + s + " s3 length: " + s3.length + " (s2[s2.length - 1]): " + (s3[s3.length - 1]));
                    Log.d("myTag", " split: " + (s3[s3.length - 1].split("\\."))[0]);
                    int side = Integer.valueOf((s3[s3.length - 1].split("\\."))[0]);
                    String face = s.contains("right") ? "r" : "l";
                    Log.d("myTag", " face: " + face);

                    uploadFaceImage(optographGlobal, s, face, side);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Cursor res = mydb.getData(optographGlobal.getId(), DBHelper.FACES_TABLE_NAME, DBHelper.FACES_ID);
            res.moveToFirst();
            if (res.getCount() == 0) return;
            String stringRes = "" + DBHelper.FACES_LEFT_ZERO + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_ZERO)) +
                    "\n" + DBHelper.FACES_LEFT_ONE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_ONE)) +
                    "\n" + DBHelper.FACES_LEFT_TWO + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_TWO)) +
                    "\n" + DBHelper.FACES_LEFT_THREE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_THREE)) +
                    "\n" + DBHelper.FACES_LEFT_FOUR + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_FOUR)) +
                    "\n" + DBHelper.FACES_LEFT_FIVE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_LEFT_FIVE)) +
                    "\n" + DBHelper.FACES_RIGHT_ZERO + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_ZERO)) +
                    "\n" + DBHelper.FACES_RIGHT_ONE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_ONE)) +
                    "\n" + DBHelper.FACES_RIGHT_TWO + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_TWO)) +
                    "\n" + DBHelper.FACES_RIGHT_THREE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_THREE)) +
                    "\n" + DBHelper.FACES_RIGHT_FOUR + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_FOUR)) +
                    "\n" + DBHelper.FACES_RIGHT_FIVE + " " + res.getString(res.getColumnIndex(DBHelper.FACES_RIGHT_FIVE));
            Log.d("myTag", "" + stringRes);
            cache.save(Cache.UPLOAD_ON_GOING, false);
            if (mydb.checkIfAllImagesUploaded(optographId)) {
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_IS_ON_SERVER, 1);
                finish();
            } else {
                mydb.updateColumnOptograph(optographId, DBHelper.OPTOGRAPH_SHOULD_BE_PUBLISHED, 0);
                Log.d("myTag", "Not uploaded");
                Snackbar.make(uploadButton,"Failed to upload. Check internet connection.",Snackbar.LENGTH_SHORT).show();
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
            }
        }
    }

    private boolean uploadFaceImage(Optograph opto, String filePath, String face, int side) {
        flag = 2;
        String[] s2 = filePath.split("/");
        String fileName = s2[s2.length - 1];

        if (face.equals("l") && opto.getLeftFace().getStatus()[side]) {
            Log.d("myTag"," already uploaded: "+face+side);
            return true;
        }
        else if (opto.getRightFace().getStatus()[side]) {
            Log.d("myTag"," already uploaded: "+face+side);
            return true;
        }

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
                .addFormDataPart("asset", face + fileName, fbody)
                .addFormDataPart("key", face + side)
                .build();
        Log.d("myTag", "asset: " + face + fileName + " key: " + face + fileName.replace(".jpg", ""));
        apiConsumer.uploadOptoImage(opto.getId(), fbodyMain, (UPLOAD_IMAGE_MODE ? optoTypeTheta : optoType360), new Callback<LogInReturn.EmptyResponse>() {
            @Override
            public void onResponse(Response<LogInReturn.EmptyResponse> response, Retrofit retrofit) {
                Log.d("myTag", "onResponse uploadImage isSuccess? " + response.isSuccess());
                Log.d("myTag", "onResponse message: " + response.message());
                Log.d("myTag", "onResponse body: " + response.body());
                Log.d("myTag", "onResponse raw: " + response.raw());
                if (face.equals("l"))
                    opto.getLeftFace().setStatusByIndex(side, response.isSuccess());
                else opto.getRightFace().setStatusByIndex(side, response.isSuccess());
                updateFace(opto, face, side, response.isSuccess() ? 1 : 0);

                Log.d("myTag", "after: ");
                int ctr = 0;
                for (boolean i : opto.getLeftFace().getStatus()) {
                    Log.d("myTag", "left " + ctr + ": " + i);
                    ctr += 1;
                }
                int ctr2 = 0;
                for (boolean i : opto.getRightFace().getStatus()) {
                    Log.d("myTag", "right " + ctr2 + ": " + i);
                    ctr2 += 1;
                }
                flag = response.isSuccess() ? 1 : 0;
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("myTag", "onFailure uploadImage: " + t.getMessage());
                blackCircle.setVisibility(View.GONE);
                uploadProgress.setVisibility(View.GONE);
                if (face.equals("l")) opto.getLeftFace().setStatusByIndex(side, false);
                else opto.getRightFace().setStatusByIndex(side, false);
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

    private void updateFace(Optograph opto, String face, int side, int value) {
        String column = "faces_";
        if (face.equals("l")) column += "left_";
        else column += "right_";

        if (side == 0) column += "zero";
        else if (side == 1) column += "one";
        else if (side == 2) column += "two";
        else if (side == 3) column += "three";
        else if (side == 4) column += "four";
        else if (side == 5) column += "five";

        mydb.updateFace(opto.getId(), column, value);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!GlobalState.isAnyJobRunning) {
            postLaterProgress.setVisibility(View.GONE);
            uploadProgress.setVisibility(View.GONE);
            blackCircle.setVisibility(View.GONE);
            doneUpload = true;
        }

        // Register for preview image generation event
        BusProvider.getInstance().register(this);
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
        postLaterProgress.setVisibility(View.GONE);
        uploadProgress.setVisibility(View.GONE);
        blackCircle.setVisibility(View.GONE);
        doneUpload = true;
    }

    @Subscribe
    public void receiveFinishEvent(RecordFinishedEvent recordFinishedEvent) {
        Timber.d("recordFinishedEvent");
    }

}
