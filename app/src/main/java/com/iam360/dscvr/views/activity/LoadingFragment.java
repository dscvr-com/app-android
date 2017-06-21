package com.iam360.dscvr.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.iam360.dscvr.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoadingFragment extends Fragment {

    private ProgressBar progressBar;

    public LoadingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_loading, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        return view;
    }


}
