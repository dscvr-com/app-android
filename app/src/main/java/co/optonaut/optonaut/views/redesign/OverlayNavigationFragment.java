package co.optonaut.optonaut.views.redesign;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.nativecode.TestUtil;
import co.optonaut.optonaut.util.Constants;
import co.optonaut.optonaut.util.MixpanelHelper;
import co.optonaut.optonaut.views.dialogs.VRModeExplanationDialog;
import timber.log.Timber;

/**
 * @author Nilan Marktanner
 * @date 2016-01-25
 */
public class OverlayNavigationFragment extends Fragment {
    private RelativeLayout statusbar;
    private Toolbar toolbar;

    VRModeExplanationDialog vrModeExplanationDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.overlay_nagivation_fragment, container, false);

        initializeToolbar(view);
        initializeNavigationButtons(view);

        statusbar = (RelativeLayout) view.findViewById(R.id.statusbar);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Timber.v("kitkat");
            statusbar.setVisibility(View.VISIBLE);
        } else {
            statusbar.setVisibility(View.GONE);
        }

        return view;
    }

    private void initializeNavigationButtons(View view) {
        TextView homeLabel = (TextView) view.findViewById(R.id.home_label);
        homeLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());
        homeLabel.setText(getResources().getString(R.string.home_label));
        Button homeButton = (Button) view.findViewById(R.id.home_button);
        View home_indicator = view.findViewById(R.id.home_button_indicator);
        Timber.d("height: %s", home_indicator.getHeight());
        Timber.d("measured height: %s", home_indicator.getMeasuredHeight());

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, Constants.getInstance().getDisplayMetrics());
        home_indicator.setTranslationY(px);
        home_indicator.setVisibility(View.VISIBLE);
        homeButton.setTypeface(Constants.getInstance().getIconTypeface());
        homeButton.setText(String.valueOf((char) 0xe90e));
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: switch to home screen
                home_indicator.setVisibility(View.VISIBLE);
            }
        });

        Button recordButton = (Button) view.findViewById(R.id.record_button);
        View profile_button_indicator = (View) view.findViewById(R.id.profile_button_indicator);
        recordButton.setTypeface(Constants.getInstance().getIconTypeface());
        recordButton.setText(String.valueOf((char) 0xe902));
        recordButton.setOnClickListener(v -> {
            TestUtil t = new TestUtil();
            t.logNative();
            Snackbar.make(v, getResources().getString(R.string.feature_record_soon), Snackbar.LENGTH_LONG).show();
        });

        TextView profileLabel = (TextView) view.findViewById(R.id.profile_label);
        profileLabel.setTypeface(Constants.getInstance().getDefaultLightTypeFace());
        profileLabel.setText(getResources().getString(R.string.profile_label));

        Button profileButton = (Button) view.findViewById(R.id.profile_button);
        profileButton.setTypeface(Constants.getInstance().getIconTypeface());
        profileButton.setText(String.valueOf((char) 0xe910));
        profileButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_profiles_soon), Snackbar.LENGTH_SHORT).show();
        });
    }

    private void initializeToolbar(View view) {
        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setTypeface(Constants.getInstance().getIconTypeface());
        searchButton.setText(String.valueOf((char) 0xe91f));
        searchButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });

        TextView header = (TextView) view.findViewById(R.id.header);
        header.setTypeface(Constants.getInstance().getIconTypeface());
        header.setText(String.valueOf((char) 0xe91c));

        Button notificationButton = (Button) view.findViewById(R.id.notification_button);
        notificationButton.setTypeface(Constants.getInstance().getIconTypeface());
        notificationButton.setText(String.valueOf((char) 0xe90f));
        notificationButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });


        Button settingsButton = (Button) view.findViewById(R.id.settings_label);
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
        TextView vrmode_button = (TextView) view.findViewById(R.id.vrmode_button);
        vrmode_button.setTypeface(Constants.getInstance().getIconTypeface());
        vrmode_button.setText(String.valueOf((char) 0xe920));
        vrmode_button.setOnClickListener(v -> {
            MixpanelHelper.trackActionViewer2DVRButton(getActivity());
            vrModeExplanationDialog.show(getChildFragmentManager(), null);
        });


        toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        float scale = Constants.getInstance().getDisplayMetrics().density;
        int marginTop = (int) (25 * scale + 0.5f);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setMargins(0, marginTop, 0, 0);
        toolbar.setLayoutParams(lp);
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
}
