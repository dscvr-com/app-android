package co.optonaut.optonaut.views;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.optonaut.optonaut.BR;
import co.optonaut.optonaut.FeedItemBinding;
import co.optonaut.optonaut.R;
import co.optonaut.optonaut.model.Optograph;

/**
 * @author Nilan Marktanner
 * @date 2015-12-02
 */
public class Optograph2DFragment extends Fragment {
    private static final String DEBUG_TAG = "Optonaut";
    private FeedItemBinding binding;
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
        return inflater.inflate(R.layout.feed_item, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        binding = DataBindingUtil.bind(view);
        binding.setVariable(BR.optograph, optograph);
        binding.executePendingBindings();
    }
}
