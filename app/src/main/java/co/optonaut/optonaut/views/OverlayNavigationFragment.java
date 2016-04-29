package co.optonaut.optonaut.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.record.GlobalState;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.views.MainActivityRedesign;
import co.optonaut.optonaut.views.dialogs.CancelRecordingDialog;
import co.optonaut.optonaut.views.dialogs.SignInDialog;
import co.optonaut.optonaut.views.dialogs.ChooseRecordingModeDialog;
import co.optonaut.optonaut.views.dialogs.VRModeExplanationDialog;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-25
 */

// TODO: create OverlayRecordFragment!
public class OverlayNavigationFragment extends Fragment implements View.OnClickListener{
    public static final int GONE = -1;
    public static final int FEED = 0;
    public static final int PREVIEW_RECORD = 1;
    public static final int RECORDING = 2;
    public static final int PROFILE = 3;
    public static final int IMAGE_PREVIEW = 4;

    private int RECORDING_MODE = Constants.MODE_CENTER;
    private int currentMode;
    private int screenWidth = Constants.getInstance().getDisplayMetrics().widthPixels;

    @Bind(R.id.statusbar) RelativeLayout statusbar;

    // Toolbar
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.header_overlay) RelativeLayout headerOverlay;
    @Bind(R.id.search_button) Button searchButton;
    @Bind(R.id.header) TextView header;
    @Bind(R.id.notification_button) Button notificationButton;
    @Bind(R.id.settings_label) Button settingsButton;
    @Bind(R.id.vrmode_button) TextView vrmodeButton;

    // Navigation bar
    @Bind(R.id.navigation_buttons) RelativeLayout navigationButtons;
    @Bind(R.id.home_group) RelativeLayout homeGroup;
    @Bind(R.id.home_label) TextView homeLabel;
    @Bind(R.id.home_button) Button homeButton;
    @Bind(R.id.home_button_indicator) View homeIndicator;

    @Bind(R.id.cancel_group) RelativeLayout cancelGroup;
    @Bind(R.id.cancel_label) TextView cancelLabel;
    @Bind(R.id.cancel_button) Button cancelButton;

    @Bind(R.id.record_button) Button recordButton;
    @Bind(R.id.record_progress) ProgressBar recordProgress;

    @Bind(R.id.profile_group) RelativeLayout profileGroup;
    @Bind(R.id.profile_label) TextView profileLabel;
    @Bind(R.id.profile_button) Button profileButton;
    @Bind(R.id.profile_button_indicator) View profileIndicator;

    @Bind(R.id.camera_overlay) FrameLayout cameraOverlay;
    @Bind(R.id.instruction) TextView instruction;
    @Bind(R.id.crosshair) View crosshair;
    @Bind(R.id.arrow) View arrow;
    @Bind(R.id.line) View line;
    @Bind(R.id.angle) View angle;
    @Bind(R.id.progress_point) View progressPoint;
    @Bind(R.id.arc) LinearLayout arc;

    @Bind(R.id.one_ring) RelativeLayout oneRingOpt;
    @Bind(R.id.three_ring) RelativeLayout threeRingOpt;

    private CancelRecordingDialog cancelRecordingDialog;
    private SignInDialog signInDialog;
    private ChooseRecordingModeDialog chooseRecordingModeDialog;

    private VRModeExplanationDialog vrModeExplanationDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.overlay_nagivation_fragment, container, false);
        ButterKnife.bind(this, view);

        initializeToolbar(view);
        initializeNavigationButtons(view);

        currentMode = FEED;

//        tiltView = new TiltView(getActivity());
//        arc.addView(tiltView);
        statusbar = (RelativeLayout) view.findViewById(R.id.statusbar);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Timber.v("kitkat");
            statusbar.setVisibility(View.VISIBLE);
        } else {
            statusbar.setVisibility(View.GONE);
        }

        cancelRecordingDialog = new CancelRecordingDialog();
        cancelRecordingDialog.setTargetFragment(this, 0);

        signInDialog = new SignInDialog();
        signInDialog.setTargetFragment(this, 0);

        chooseRecordingModeDialog = new ChooseRecordingModeDialog();
        chooseRecordingModeDialog.setTargetFragment(this, 0);

        oneRingOpt.setBackground(getResources().getDrawable(R.drawable.ring_selector_red));
        oneRingOpt.setOnClickListener(this);
        threeRingOpt.setOnClickListener(this);

        return view;
    }

    private void initializeToolbar(View view) {
        searchButton.setTypeface(Constants.getInstance().getIconTypeface());
        searchButton.setText(String.valueOf((char) 0xe91f));
        searchButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });

//        header.setTypeface(Constants.getInstance().getIconTypeface());
//        header.setText(String.valueOf((char) 0xe91c));

        notificationButton.setTypeface(Constants.getInstance().getIconTypeface());
        notificationButton.setText(String.valueOf((char) 0xe90f));
        notificationButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });


        settingsButton.setTypeface(Constants.getInstance().getIconTypeface());
        settingsButton.setText(String.valueOf((char) 0xe904));
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.profile_menu);

                //registering popup with OnMenuItemClickListener
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Snackbar.make(
                                view,
                                "You Clicked: " + item.getTitle(),
                                Snackbar.LENGTH_SHORT
                        ).show();
                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        vrModeExplanationDialog = new VRModeExplanationDialog();
        vrmodeButton.setTypeface(Constants.getInstance().getIconTypeface());
        vrmodeButton.setText(String.valueOf((char) 0xe920));
        vrmodeButton.setOnClickListener(v -> {
            MixpanelHelper.trackActionViewer2DVRButton(getActivity());
            vrModeExplanationDialog.show(getChildFragmentManager(), null);
        });

        float scale = Constants.getInstance().getDisplayMetrics().density;
        int marginTop = (int) (25 * scale + 0.5f);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setMargins(0, marginTop, 0, 0);
        toolbar.setLayoutParams(lp);
    }

    private void initializeNavigationButtons(View view) {
        homeLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());
        homeLabel.setText(getResources().getString(R.string.home_label));

        cancelLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());
        cancelLabel.setText(getResources().getString(R.string.cancel_label));

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, Constants.getInstance().getDisplayMetrics());
        homeIndicator.setTranslationY(px);
        homeIndicator.setVisibility(View.VISIBLE);
        homeButton.setTypeface(Constants.getInstance().getIconTypeface());
        homeButton.setText(String.valueOf((char) 0xe90e));
        homeGroup.setOnClickListener(v -> {
            Timber.v("Switching to home");
            changeMode(FEED, true);
            homeIndicator.setVisibility(View.VISIBLE);
            profileIndicator.setVisibility(View.INVISIBLE);
        });

        cancelButton.setTypeface(Constants.getInstance().getIconTypeface());
        cancelButton.setText(String.valueOf((char) 0xe909));
        cancelGroup.setOnClickListener(v -> {
            if (currentMode == PREVIEW_RECORD) {
                cancel();
            } else if (currentMode == RECORDING) {
                askToCancel();
            }
        });

        recordButton.setTypeface(Constants.getInstance().getIconTypeface());
        recordButton.setText(String.valueOf((char) 0xe902));

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalState.isAnyJobRunning) {
                    Snackbar.make(view, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
                    return;
                }
                if (currentMode == FEED) {
                    changeMode(PREVIEW_RECORD, false);
                } else if (currentMode == PREVIEW_RECORD) {
                    changeMode(RECORDING, false);
                }
            }
        });

//        recordButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (GlobalState.isAnyJobRunning) {
//                    Snackbar.make(view, R.string.dialog_wait_on_record_finish, Snackbar.LENGTH_LONG).show();
//                    return false;
//                }
//
//                chooseRecordingModeDialog.show(getFragmentManager(), "chooseRecordingModeDialog");
//
//                return false;
//            }
//        });

        profileLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());
        profileLabel.setText(getResources().getString(R.string.profile_label));

        profileButton.setTypeface(Constants.getInstance().getIconTypeface());
        profileButton.setText(String.valueOf((char) 0xe910));
        profileGroup.setOnClickListener(v -> {
            homeIndicator.setVisibility(View.INVISIBLE);
            profileIndicator.setVisibility(View.VISIBLE);
            changeMode(PROFILE, false);
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                //observer.removeOnGlobalLayoutListener(this);
                // get width and height of the view
                initializeSharedPreferences(view);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();

        getView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (currentMode == RECORDING || currentMode == PREVIEW_RECORD) {
                        return cancelGroup.callOnClick();
                    }
                }
            }
            return false;
        });
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void initializeSharedPreferences(View view) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (!sharedPref.contains(getActivity().getResources().getString(R.string.preference_lowerboundary))) {
            int lowerBoundary = getLowerBoundary(view);
            editor.putInt(getString(R.string.preference_lowerboundary), lowerBoundary);
        }
        if (!sharedPref.contains(getActivity().getResources().getString(R.string.preference_upperboundary))) {
            int upperBoundary = getUpperBoundary(view);
            editor.putInt(getString(R.string.preference_upperboundary), upperBoundary);
        }
        editor.commit();
    }

    public void changeMode(final int mode, boolean cancel) {
        switch (mode) {
            case GONE:
                // TODO: set invisible
                break;
            case FEED:
                switchToFeedMode(cancel);
                break;
            case PREVIEW_RECORD:
                switchToPreviewRecordMode(RECORDING_MODE);
                break;
            case RECORDING:
                switchToRecordingMode();
                break;
            case PROFILE:
                switchToProfileMode();
                break;
            default:
                Timber.e("Unknown mode in OverlayNavigationFragment");

        }
    }

    public void hideRecordButton() {
        recordButton.setVisibility(View.INVISIBLE);
    }

    public void showRecordButton() {
        recordButton.setVisibility(View.VISIBLE);
    }

    /**
     *
     * @param visibility View.INVISIBLE / VISIBLE / GONE
     */
    public void setToolbarVisibility(int visibility) {
        toolbar.setVisibility(visibility);
    }

    public void switchToFeedMode(boolean cancelRecording) {
        Timber.v("switching to feed mode");
        currentMode = FEED;

        if (cancelRecording) {
            MainActivityRedesign activity = (MainActivityRedesign) getActivity();
            activity.cancelRecording();
        }

        toolbar.setVisibility(View.VISIBLE);
        homeGroup.setVisibility(View.VISIBLE);
        cancelGroup.setVisibility(View.INVISIBLE);
        profileGroup.setVisibility(View.VISIBLE);
        recordButton.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.INVISIBLE);
        oneRingOpt.setVisibility(View.VISIBLE);
        threeRingOpt.setVisibility(View.VISIBLE);

        cameraOverlay.setVisibility(View.INVISIBLE);
    }

    public void switchToFeedModeFromPreview() {
        Timber.v("switching to feed mode");
        currentMode = FEED;
    }

    public void switchToImagePreview(UUID id) {
        Timber.v("switching to image_preview mode");
        currentMode = IMAGE_PREVIEW;

        MainActivityRedesign activity = (MainActivityRedesign) getActivity();
        activity.startImagePreview(id);
    }

    public void switchToPreviewRecordMode(int ringMode) {
        Timber.v("switching to preview record mode");
        currentMode = PREVIEW_RECORD;

        MainActivityRedesign activity = (MainActivityRedesign) getActivity();
        activity.prepareRecording(ringMode);

        toolbar.setVisibility(View.INVISIBLE);
        homeGroup.setVisibility(View.INVISIBLE);
        cancelGroup.setVisibility(View.VISIBLE);
        profileGroup.setVisibility(View.INVISIBLE);
        oneRingOpt.setVisibility(View.INVISIBLE);
        threeRingOpt.setVisibility(View.INVISIBLE);
        recordButton.setVisibility(View.VISIBLE);
        instruction.setVisibility(View.VISIBLE);
        instruction.setText(getActivity().getResources().getText(R.string.record_instruction_press));

    }

    private void switchToRecordingMode() {
        Timber.v("switching to recording mode");
        currentMode = RECORDING;

        recordButton.setVisibility(View.INVISIBLE);
        cameraOverlay.setVisibility(View.VISIBLE);

        instruction.setText(getActivity().getResources().getText(R.string.record_instruction_follow));

        ((MainActivityRedesign) getActivity()).startRecording();
    }

    private void switchToProfileMode() {
        Timber.v("switching to profile mode");
        currentMode = PROFILE;

        toolbar.setVisibility(View.INVISIBLE);
        homeGroup.setVisibility(View.VISIBLE);
        cancelGroup.setVisibility(View.INVISIBLE);
        profileGroup.setVisibility(View.VISIBLE);
        recordButton.setVisibility(View.VISIBLE);

        crosshair.setVisibility(View.INVISIBLE);
        arrow.setVisibility(View.INVISIBLE);
        line.setVisibility(View.INVISIBLE);
        angle.setVisibility(View.INVISIBLE);

        ((MainActivityRedesign) getActivity()).prepareProfile(false);
    }

    public void setTotalVisibility(int visibility) {
        if (visibility != View.INVISIBLE && visibility != View.VISIBLE) {
            throw new RuntimeException("Can only toggle between visible and invisible!");
        }

        if (getView() != null) {
            getView().setVisibility(visibility);
        } else {
            Timber.w("setting visibility of null-View!");
        }
    }

    public void hideDialog() {
        if (vrModeExplanationDialog != null) {
            if (vrModeExplanationDialog.getDialog() != null) {
                if (vrModeExplanationDialog.getDialog().isShowing()) {
                    vrModeExplanationDialog.dismiss();
                }
            }
        }

        if (cancelRecordingDialog != null) {
            if (cancelRecordingDialog.getDialog() != null) {
                if (cancelRecordingDialog.getDialog().isShowing()) {
                    cancelRecordingDialog.dismiss();
                }
            }
        }
    }

    private void askToCancel() {
        cancelRecordingDialog.show(getFragmentManager(), "cancelRecordingDialog");
    }

    public void cancel() {
        changeMode(FEED, true);
        getActivity().onBackPressed();
    }

    public boolean toggleTotalVisibility() {
        View view = getView();
        boolean toggled = false;
        if (view != null) {
            if (view.getVisibility() == View.INVISIBLE) {
                setTotalVisibility(View.VISIBLE);
                toggled = true;
            } else if (view.getVisibility() == View.VISIBLE) {
                setTotalVisibility(View.INVISIBLE);
                toggled = true;
            } else {
                Timber.w("visibility of overlay was neither invisible nor visible");
            }
        }
        return toggled;
    }

    private int getLowerBoundary(View view) {
        int lowerBoundary = 0;
        if (view != null && view.findViewById(R.id.navigation_buttons) != null) {
            int[] location = new int[2];
            view.findViewById(R.id.navigation_buttons).getLocationOnScreen(location);
            lowerBoundary = location[1];
            Timber.d("lower boundary: %s", lowerBoundary);
        } else {
            Timber.w("lower boundary not set yet");
        }
        return lowerBoundary;
    }

    private int getUpperBoundary(View view) {
        int upperBoundary = 0;
        if (view != null && toolbar != null) {
            int[] location = new int[2];
            toolbar.getLocationOnScreen(location);
            upperBoundary = location[1] + toolbar.getHeight();
            Timber.d("upper boundary: %s", upperBoundary);
        } else {
            Timber.w("upper boundary not set yet");
        }
        return upperBoundary;
    }

    public void setAngleRotation(float rotation) {

        line.setRotation((float)Math.toDegrees(rotation));
    }

    public void setArrowRotation(float rotation) {
        arrow.setRotation((float)Math.toDegrees(rotation));
    }

    public void setArrowVisible(boolean visible) {
        if (visible) {
            arrow.setVisibility(View.VISIBLE);
            crosshair.setBackground(getActivity().getResources().getDrawable(R.drawable.crosshair));
        } else {
            arrow.setVisibility(View.INVISIBLE);
            crosshair.setBackground(getActivity().getResources().getDrawable(R.drawable.crosshair_red));
        }
    }

    public void setGuideLinesVisible(boolean visible) {
        if(visible) {
            line.setVisibility(View.VISIBLE);
            angle.setVisibility(View.VISIBLE);
        } else {
            line.setVisibility(View.INVISIBLE);
            angle.setVisibility(View.INVISIBLE);
        }
    }

    public void setProgress(float progress) {
        progressPoint.setX((screenWidth - 100) * progress + 50);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.one_ring:
                RECORDING_MODE = Constants.MODE_CENTER;
                oneRingOpt.setBackground(getResources().getDrawable(R.drawable.ring_selector_red));
                threeRingOpt.setBackground(getResources().getDrawable(R.drawable.ring_selector_gray));
                break;
            case R.id.three_ring:
                RECORDING_MODE = Constants.MODE_TRUNCATED;
                oneRingOpt.setBackground(getResources().getDrawable(R.drawable.ring_selector_gray));
                threeRingOpt.setBackground(getResources().getDrawable(R.drawable.ring_selector_red));
                break;
        }

    }

    public class TiltView extends View {

        public TiltView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);

            Paint p = new Paint();
            RectF rectF = new RectF(0, 0, 200, 200);
            p.setColor(Color.RED);
            canvas.drawArc(rectF, 90, 35, true, p);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GlobalState.isAnyJobRunning) {
            recordProgress.setVisibility(View.VISIBLE);
        }
    }
}
