package com.iam360.dscvr.views.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
        View view = inflater.inflate(R.layout.fragment_loading, container, false);

        WebView wv = (WebView) view.findViewById(R.id.webview_component);
        wv.getSettings().setJavaScriptEnabled(true);

        final String mimeType = "text/html";
        final String encoding = "utf-8";
        final String html = "<html><body  bgcolor=\"#fafafa\" class=\"centered-wrapper\">"+
        "<div class=\"centered-content\">"+
                "<svg width=\"200px\"  height=\"200px\"  xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" preserveAspectRatio=\"xMidYMid\" class=\"lds-blank\">\n" +
                "    <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r3}}\" ng-attr-stroke=\"{{config.c3}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"46\" stroke=\"#ff7200\" stroke-width=\"5\">\n" +
                "      <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 144.51326206513048 0 144.51326206513048;0 0 144.51326206513048 0 0 144.51326206513048;0 0 144.51326206513048 0 0 144.51326206513048;0 144.51326206513048 0 144.51326206513048 0 144.51326206513048;0 144.51326206513048 0 144.51326206513048 0 144.51326206513048\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-5s\" repeatCount=\"indefinite\"></animate>\n" +
                "    </circle>\n" +
                "    <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r2}}\" ng-attr-stroke=\"{{config.c2}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"40\" stroke=\"#fcb711\" stroke-width=\"5\">\n" +
                "      <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 125.66370614359172 0 125.66370614359172;0 0 125.66370614359172 0 0 125.66370614359172;0 0 125.66370614359172 0 0 125.66370614359172;0 125.66370614359172 0 125.66370614359172 0 125.66370614359172;0 125.66370614359172 0 125.66370614359172 0 125.66370614359172\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-4.6s\" repeatCount=\"indefinite\"></animate>\n" +
                "    </circle>\n" +
                "    <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r1}}\" ng-attr-stroke=\"{{config.c1}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"34\" stroke=\"#E0641B\" stroke-width=\"5\">\n" +
                "      <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 106.81415022205297 0 106.81415022205297;0 0 106.81415022205297 0 0 106.81415022205297;0 0 106.81415022205297 0 0 106.81415022205297;0 106.81415022205297 0 106.81415022205297 0 106.81415022205297;0 106.81415022205297 0 106.81415022205297 0 106.81415022205297\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-4.2s\" repeatCount=\"indefinite\"></animate>\n" +
                "    </circle>\n" +
                "    <g transform=\"rotate(180 50 50)\">\n" +
                "      <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r3}}\" ng-attr-stroke=\"{{config.c3}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"46\" stroke=\"#ff7200\" stroke-width=\"5\">\n" +
                "        <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 144.51326206513048 0 144.51326206513048;0 0 144.51326206513048 0 0 144.51326206513048;0 0 144.51326206513048 0 0 144.51326206513048;0 144.51326206513048 0 144.51326206513048 0 144.51326206513048;0 144.51326206513048 0 144.51326206513048 0 144.51326206513048\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-2.1999999999999997s\" repeatCount=\"indefinite\"></animate>\n" +
                "      </circle>\n" +
                "      <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r2}}\" ng-attr-stroke=\"{{config.c2}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"40\" stroke=\"#fcb711\" stroke-width=\"5\">\n" +
                "        <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 125.66370614359172 0 125.66370614359172;0 0 125.66370614359172 0 0 125.66370614359172;0 0 125.66370614359172 0 0 125.66370614359172;0 125.66370614359172 0 125.66370614359172 0 125.66370614359172;0 125.66370614359172 0 125.66370614359172 0 125.66370614359172\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-2.6s\" repeatCount=\"indefinite\"></animate>\n" +
                "      </circle>\n" +
                "      <circle cx=\"50\" cy=\"50\" fill=\"none\" ng-attr-r=\"{{config.r1}}\" ng-attr-stroke=\"{{config.c1}}\" ng-attr-stroke-width=\"{{config.width}}\" r=\"34\" stroke=\"#E0641B\" stroke-width=\"5\">\n" +
                "        <animate attributeName=\"stroke-dasharray\" calcMode=\"linear\" values=\"0 0 0 106.81415022205297 0 106.81415022205297;0 0 106.81415022205297 0 0 106.81415022205297;0 0 106.81415022205297 0 0 106.81415022205297;0 106.81415022205297 0 106.81415022205297 0 106.81415022205297;0 106.81415022205297 0 106.81415022205297 0 106.81415022205297\" keyTimes=\"0;0.2;0.4;0.6;1\" dur=\"5\" begin=\"-3.2s\" repeatCount=\"indefinite\"></animate>\n" +
                "      </circle>\n" +
                "    </g>\n" +
                "  </svg>" +
                "</div></body></html>";

        wv.loadData(html, mimeType, encoding );
        wv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });
        wv.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        return view;
    }


}
