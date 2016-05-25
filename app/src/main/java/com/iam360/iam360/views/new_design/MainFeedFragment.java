package com.iam360.iam360.views.new_design;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.UUID;

import com.iam360.iam360.R;
import com.iam360.iam360.bus.BusProvider;
import com.iam360.iam360.bus.RecordFinishedEvent;
import com.iam360.iam360.record.GlobalState;
import com.iam360.iam360.util.Cache;
import com.iam360.iam360.util.Constants;
import com.iam360.iam360.util.GeneralUtils;
import com.iam360.iam360.util.MixpanelHelper;
import com.iam360.iam360.viewmodels.LocalOptographManager;
import com.iam360.iam360.views.dialogs.NetworkProblemDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements View.OnClickListener, Updateable {
    public static final String TAG = MainFeedFragment.class.getSimpleName();
    private static final int MILLISECONDS_THRESHOLD_FOR_SWITCH = 250;

    NetworkProblemDialog networkProblemDialog;

    private SensorManager sensorManager;
    private boolean inVRMode;

    private DateTime inVRPositionSince;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkProblemDialog = new NetworkProblemDialog();
        inVRMode = false;
        inVRPositionSince = null;
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.profileBtn.setOnClickListener(this);
        binding.cameraBtn.setOnClickListener(this);
        binding.settingsBtn.setOnClickListener(this);
        binding.thetaBtn.setOnClickListener(this);
        binding.headerLogo.setOnClickListener(this);

//        Settings start
        initializeButtons();

        binding.a3dButton.setOnClickListener(this);
        binding.gyroButton.setOnClickListener(this);
        binding.littlePlanetButton.setOnClickListener(this);
        binding.oneRingButton.setOnClickListener(this);
        binding.threeRingButton.setOnClickListener(this);
        binding.manualButton.setOnClickListener(this);
        binding.motorButton.setOnClickListener(this);
//        Settings end

        binding.slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                binding.profileBtn.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        binding.pullUpButton.setVisibility(View.VISIBLE);
                        if (previousState == SlidingUpPanelLayout.PanelState.DRAGGING) binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.settings_bg));
                        binding.profileBtn.setVisibility(View.GONE);
                        break;
                    case COLLAPSED:
                    case ANCHORED:
                    case HIDDEN:
                        binding.pullUpButton.setVisibility(View.GONE);
                        binding.profileBtn.setVisibility(View.VISIBLE);
                        binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.transparentOverlay));
                        break;
                    case DRAGGING:
                        if (previousState== SlidingUpPanelLayout.PanelState.EXPANDED) {
                            binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.transparentOverlay));
                            binding.pullUpButton.setVisibility(View.GONE);
                        } else if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            binding.barTransparent.setBackgroundColor(getResources().getColor(R.color.settings_bg));
                            binding.pullUpButton.setVisibility(View.VISIBLE);
                        }
                        binding.profileBtn.setVisibility(View.GONE);
                        break;
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        inVRMode = false;
        BusProvider.getInstance().register(this);
        if (GlobalState.shouldHardRefreshFeed) {
            initializeFeed();
        }

    }

    @Override
    protected void initializeFeed() {
        LocalOptographManager.getOptographs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(optographFeedAdapter::addItem);

        apiConsumer.getOptographs(5)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> MixpanelHelper.trackViewViewer2D(getActivity()))
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

        GlobalState.shouldHardRefreshFeed = false;
    }

    @Override
    protected void loadMore() {
        apiConsumer.getOptographs(50, optographFeedAdapter.getOldest().getCreated_at())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> {
                    networkProblemDialog.show(getFragmentManager(), "networkProblemDialog");
                    return null;
                })
                .subscribe(optographFeedAdapter::addItem);

        // TODO: prefetch textures
    }

    @Override
    protected void refresh() {
        // TODO: actually refresh data
    }

    @Subscribe
    public void recordFinished(RecordFinishedEvent event) {
        initializeFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_btn:
                ((MainActivity) getActivity()).setPage(MainActivity.PROFILE_MODE);
                break;
            case R.id.camera_btn:
                if (cache.getString(Cache.USER_TOKEN).isEmpty()) {
                    Snackbar.make(v,getActivity().getString(R.string.profile_login_first),Snackbar.LENGTH_SHORT).show();
                    return;
                } else if (GlobalState.isAnyJobRunning) {
                    Snackbar.make(binding.cameraBtn, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), RecorderActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_btn:
//                ((MainActivity) getActivity()).startSettings();
                binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.header_logo:
                binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.theta_btn:
                Intent intent1 = new Intent();
                // Show only images, no videos or anything else
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_IMAGE_REQUEST);
                break;
            case R.id.a3d_button:
                cache.save(Cache.VR_3D_ENABLE,!cache.getBoolean(Cache.VR_3D_ENABLE,false));
                binding.a3dButton.setBackgroundResource(cache.getBoolean(Cache.VR_3D_ENABLE, false) ? R.drawable.a3dvr_active_icn : R.drawable.a3dvr_inactive_icn);
                break;
            case R.id.gyro_button:
                gyroValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.little_planet_button:
                littlePlanetValidation();
                instatiateFeedDisplayButton();
                break;
            case R.id.one_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.ONE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.ONE_RING_MODE);
                    activeOneRing();
                }
                break;
            case R.id.three_ring_button:
                if (cache.getInt(Cache.CAMERA_MODE) != Constants.THREE_RING_MODE) {
                    cache.save(Cache.CAMERA_MODE, Constants.THREE_RING_MODE);
                    activeThreeRing();
                }
                break;
            case R.id.manual_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)!=Constants.MANUAL_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MANUAL_MODE);
                    activeManualType();
                }
                break;
            case R.id.motor_button:
                if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE) != Constants.MOTOR_MODE) {
                    cache.save(Cache.CAMERA_CAPTURE_TYPE, Constants.MOTOR_MODE);
                    activeMotorType();
                }
                break;
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

                MainActivity activity = (MainActivity) getActivity();
                activity.startImagePreview(UUID.randomUUID(), new GeneralUtils().getRealPathFromURI(getActivity(), uri));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void update() {

    }

    //    Settings start
    private void initializeButtons() {
        binding.a3dButton.setBackgroundResource(cache.getBoolean(Cache.VR_3D_ENABLE, false) ? R.drawable.a3dvr_active_icn : R.drawable.a3dvr_inactive_icn);

        instatiateFeedDisplayButton();

        if (cache.getInt(Cache.CAMERA_MODE) == Constants.THREE_RING_MODE)
            activeThreeRing();
        else activeOneRing();

        if (cache.getInt(Cache.CAMERA_CAPTURE_TYPE)==Constants.MANUAL_MODE)
            activeManualType();
        else activeMotorType();
    }

    private void gyroValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!gyro && lilPlanet) cache.save(Cache.LITTLE_PLANET_ENABLE,false);
        cache.save(Cache.GYRO_ENABLE,!gyro);
    }

    private void littlePlanetValidation() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);
        if (!lilPlanet && gyro) cache.save(Cache.GYRO_ENABLE,false);
        cache.save(Cache.LITTLE_PLANET_ENABLE, !lilPlanet);
    }

    private void instatiateFeedDisplayButton() {
        boolean gyro = cache.getBoolean(Cache.GYRO_ENABLE,false);
        boolean lilPlanet = cache.getBoolean(Cache.LITTLE_PLANET_ENABLE,false);

        binding.gyroButton.setBackgroundResource(gyro?R.drawable.gyro_big_active_icn:R.drawable.gyro_big_inactive_icn);
        binding.settingsGyro.setTextColor(gyro?getResources().getColor(R.color.text_active):getResources().getColor(R.color.text_inactive));
        binding.littlePlanetButton.setBackgroundResource(lilPlanet?R.drawable.little_planet_big_active_icn:R.drawable.little_planet_big_inactive_icn);
        binding.settingsLittlePlanet.setTextColor(lilPlanet?getResources().getColor(R.color.text_active):getResources().getColor(R.color.text_inactive));
    }

    private void activeOneRing() {
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_active_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_inactive_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeThreeRing() {
        binding.threeRingButton.setBackgroundResource(R.drawable.three_ring_active_icn);
        binding.settingsThreeRing.setTextColor(getResources().getColor(R.color.text_active));
        binding.oneRingButton.setBackgroundResource(R.drawable.one_ring_inactive_icn);
        binding.settingsOneRing.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeManualType() {
        binding.manualButton.setBackgroundResource(R.drawable.manual_active_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_active));
        binding.motorButton.setBackgroundResource(R.drawable.motor_inactive_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_inactive));
    }

    private void activeMotorType() {
        binding.motorButton.setBackgroundResource(R.drawable.motor_active_icn);
        binding.settingsMotor.setTextColor(getResources().getColor(R.color.text_active));
        binding.manualButton.setBackgroundResource(R.drawable.manual_inactive_icn);
        binding.settingsManual.setTextColor(getResources().getColor(R.color.text_inactive));
    }
    // Settings end
}
