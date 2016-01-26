package co.optonaut.optonaut.views.redesign;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import co.optonaut.optonaut.R;
import co.optonaut.optonaut.util.Constants;

/**
 * @author Nilan Marktanner
 * @date 2016-01-25
 */
public class OverlayNavigationFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.overlay_nagivation_fragment, container, false);

        initializeToolbar(view);

        initializeNavigationButtons(view);

        return view;
    }

    private void initializeNavigationButtons(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.my_toolbar);

        float scale = Constants.getInstance().getDisplayMetrics().density;
        int marginTop = (int) (25 * scale + 0.5f);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        lp.setMargins(0, marginTop, 0, 0);


        TextView homeLabel = (TextView) view.findViewById(R.id.home_label);
        homeLabel.setText(getResources().getString(R.string.home_label));
        Button homeButton = (Button) view.findViewById(R.id.home_button);
        homeButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        homeButton.setText(String.valueOf((char) 0xe90e));

        Button recordButton = (Button) view.findViewById(R.id.record_button);
        recordButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        recordButton.setText(String.valueOf((char) 0xe902));
        recordButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_LONG).show();
        });

        TextView profileLabel = (TextView) view.findViewById(R.id.profile_label);
        profileLabel.setText(getResources().getString(R.string.profile_label));

        Button profileButton = (Button) view.findViewById(R.id.profile_button);
        profileButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        profileButton.setText(String.valueOf((char) 0xe910));
        profileButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_LONG).show();
        });
    }

    private void initializeToolbar(View view) {
        Button searchButton = (Button) view.findViewById(R.id.search_button);
        searchButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        searchButton.setText(String.valueOf((char) 0xe91f));
        searchButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });

        TextView header = (TextView) view.findViewById(R.id.header);
        header.setTypeface(Constants.getInstance().getDefaultTypeface());
        header.setText(String.valueOf((char) 0xe91c));

        Button notificationButton = (Button) view.findViewById(R.id.notification_button);
        notificationButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        notificationButton.setText(String.valueOf((char) 0xe90f));
        notificationButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_SHORT).show();
        });

        Button settingsButton = (Button) view.findViewById(R.id.settings_button);
        settingsButton.setTypeface(Constants.getInstance().getDefaultTypeface());
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
    }

    public void toggleTotalVisibility(int visibility) {
        if (visibility != View.INVISIBLE && visibility != View.VISIBLE) {
            throw new RuntimeException("Can only toggle between visible and invisible!");
        }

        if (getView() != null) {
            getView().setVisibility(visibility);
        } else {
            Log.w(Constants.DEBUG_TAG, "Setting visibility of null-View!");
        }
    }
}
