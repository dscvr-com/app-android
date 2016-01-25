package co.optonaut.optonaut.views.redesign;

import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

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

        TextView homeLabel = (TextView) view.findViewById(R.id.home_label);
        homeLabel.setText("HOME");
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
        profileLabel.setText("PROFILE");

        Button profileButton = (Button) view.findViewById(R.id.profile_button);
        profileButton.setTypeface(Constants.getInstance().getDefaultTypeface());
        profileButton.setText(String.valueOf((char) 0xe910));
        profileButton.setOnClickListener(v -> {
            Snackbar.make(v, getResources().getString(R.string.feature_next_version), Snackbar.LENGTH_LONG).show();
        });

        return view;
    }
}
