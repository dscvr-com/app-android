package com.iam360.dscvr.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iam360.dscvr.R;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoadingFragment extends Fragment {


    public LoadingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_loading, container, false);
        GifImageView textview = (GifImageView) view.findViewById(R.id.gif);
        try {
            GifDrawable gifFromResource = new GifDrawable( getResources(), R.drawable.azure );

            textview.setImageDrawable(gifFromResource);
            gifFromResource.start();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(),"error while reading ressource");
        }
        return view;
    }


}
