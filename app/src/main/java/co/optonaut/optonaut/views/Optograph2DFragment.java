package co.optonaut.optonaut.views;

import android.app.ActionBar;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.w3c.dom.Text;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.Optograph2DBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-12-02
 */
public class Optograph2DFragment extends Fragment {
    private static final String DEBUG_TAG = "Optonaut";
    private Optograph2DBinding binding;
    private Optograph optograph;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        optograph = args.getParcelable("optograph");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // hide Action Bar to simulate "Fullscreen Mode"
        ((MainActivity) getActivity()).getSupportActionBar().hide();

        binding = DataBindingUtil.inflate(inflater, R.layout.optograph_2d_view, container, false);
        binding.setVariable(BR.optograph, optograph);
        binding.setVariable(BR.person, optograph.getPerson());
        Log.d(DEBUG_TAG, "Person asset: " + optograph.getPerson().getAvatar_asset_id());

        binding.executePendingBindings();
        return binding.getRoot();
    }
}
