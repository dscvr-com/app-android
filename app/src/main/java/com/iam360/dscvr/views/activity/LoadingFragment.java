package com.iam360.dscvr.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.iam360.dscvr.R;

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

        WebView wv = (WebView) view.findViewById(R.id.webview_component);
        wv.getSettings().setJavaScriptEnabled(true);

        final String mimeType = "text/html";
        final String encoding = "utf-8";
        final String html = "<p><object type=\"image/svg+xml\" data=\"file:///azure.svg\" height=\""+wv.getHeight()+"px\" width=\""+wv.getWidth()+"px /></p>";

        wv.loadDataWithBaseURL("fake://not/needed", html, mimeType, encoding, "");
        return view;
    }


}
