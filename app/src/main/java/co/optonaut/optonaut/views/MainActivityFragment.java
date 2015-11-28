package co.optonaut.optonaut.views;

import android.databinding.DataBindingUtil;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.optonaut.optonaut.FeedBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;
import co.optonaut.optonaut.viewmodels.FeedViewModel;

public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    final Optograph optograph = new Optograph();
    FeedBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_main, container, false);
    binding.setFeed(new FeedViewModel(optograph));
    View view = binding.getRoot();


    return view;
    }
}
