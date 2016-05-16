package co.optonaut.optonaut.views.new_design;

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

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.bus.BusProvider;
import co.optonaut.optonaut.bus.RecordFinishedEvent;
import co.optonaut.optonaut.record.GlobalState;
import co.optonaut.optonaut.util.GeneralUtils;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.viewmodels.LocalOptographManager;
import co.optonaut.optonaut.views.dialogs.NetworkProblemDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author Nilan Marktanner
 * @date 2015-12-15
 */
public class MainFeedFragment extends OptographListFragment implements View.OnClickListener {
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

        profileButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        thetaButton.setOnClickListener(this);
        headerLogoButton.setOnClickListener(this);

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                profileButton.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                switch (newState) {
                    case EXPANDED:
                        profileButton.setVisibility(View.GONE);
                        break;
                    case COLLAPSED:
                        profileButton.setVisibility(View.VISIBLE);
                        break;
                    case ANCHORED:
                        profileButton.setVisibility(View.VISIBLE);
                        break;
                    case HIDDEN:
                        profileButton.setVisibility(View.VISIBLE);
                        break;
                    case DRAGGING:
                        profileButton.setVisibility(View.GONE);
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
                if (GlobalState.isAnyJobRunning) {
                    Snackbar.make(cameraButton, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), RecorderActivity.class);
                startActivity(intent);
                break;
            case R.id.settings_btn:
                ((MainActivity) getActivity()).startSettings();
                break;
            case R.id.header_logo:
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                break;
            case R.id.theta_btn:
                Intent intent1 = new Intent();
                // Show only images, no videos or anything else
                intent1.setType("image/*");
                intent1.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent1, "Select Picture"), PICK_IMAGE_REQUEST);
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
}
