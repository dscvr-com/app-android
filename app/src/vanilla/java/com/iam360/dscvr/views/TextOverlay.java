package com.iam360.dscvr.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iam360.dscvr.R;

import timber.log.Timber;

/**
 * Created by Joven on 11/14/2016.
 */
///https://github.com/cardbookvr/launcherlobby
public class TextOverlay extends LinearLayout {
    private final OverlayEye leftEye;
    private final OverlayEye rightEye;

    public TextOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        params.setMargins(0, 0, 0, 0);



        leftEye = new OverlayEye(context, attrs);
        leftEye.setLayoutParams(params);
//        leftEye.setBackgroundDrawable(myBubble);
        addView(leftEye);

        rightEye = new OverlayEye(context, attrs);
        rightEye.setLayoutParams(params);
//        rightEye.setBackgroundDrawable(myBubble);
        addView(rightEye);

        setDeptFactor(0.01f);
        setColor(Color.rgb(150,255, 180));
        addContent("");
        setVisibility(VISIBLE);
    }

    public void setDeptFactor(float deptFactor) {
        this.leftEye.setDepthFactor(deptFactor);
        this.rightEye.setDepthFactor(-deptFactor);
    }

    private void setColor(int color){
        leftEye.setColor(color);
        rightEye.setColor(color);
    }

    public void addContent(String text){
        leftEye.addContent(text,"L");
        rightEye.addContent(text,"R");
    }

    private class OverlayEye extends ViewGroup{
        private Context context;
        private AttributeSet attrs;
        private TextView textView;
        private int textColor;
        private int viewWidth;
        private int deptOffset;
//        private LinearLayout mainParent;
        private LinearLayout mainParentChld1;
        private LinearLayout mainParentChld1Child;
        private LinearLayout mainParentChld2;

        public OverlayEye(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
            this.attrs = attrs;
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final int width = r - l;
            final int height = b - t;
            viewWidth = width;
            int layoutBot = b - (height/2);

            final float verticalTextPos = 0.52f;

            float topMargin = height * verticalTextPos;
            Timber.d("width="+width+"   height="+height+"  topMargin="+topMargin+"  b="+b);
            Timber.d("l="+l+"  t="+t+"  r="+r+"  b="+b+"  layoutBot="+layoutBot);
            textView.layout(0, (int) topMargin, width, b);
//            textView.layout(0, (int) topMargin,width,0);
        }

        private void setDepthFactor(float factor){
            this.deptOffset = (int) (factor * viewWidth);
        }

        public void setColor(int color){
            this.textColor = color;
        }

        public void addContent(String text, String type){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(0, 0, 0, 0);

            textView = new TextView(context, attrs);
            if(type.equals("L")){
                textView.setId(R.id.vr_mode_textview_L);
            }else{
                textView.setId(R.id.vr_mode_textview_R);
            }
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(textColor);
            textView.setText(text);
            textView.setX(deptOffset);
            textView.setLayoutParams(params);

//            LinearLayout mainParent = new LinearLayout(context);
//            mainParent.setGravity(Gravity.CENTER);
//            mainParent.setLayoutParams(params);
//            mainParent.addView(textView);

            addView(textView);

////            mainParent = new LinearLayout(context);
//            mainParentChld1 = new LinearLayout(context);
//            mainParentChld1Child = new LinearLayout(context);
//            mainParentChld2 = new LinearLayout(context);
//
//            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
//            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
//            LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
//
//            params2.setMargins(10,0,10,10);
//            mainParentChld1Child.setLayoutParams(params2);
//            mainParentChld1Child.setPadding(10,0,10,0);
//
//            mainParentChld1.setLayoutParams(params3);
//            mainParentChld1.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
//
//            mainParentChld2.setLayoutParams(params3);
//
////            mainParent.setLayoutParams(params1);
////            mainParent.setOrientation(VERTICAL);
//
//            BubbleDrawable myBubble = new BubbleDrawable(BubbleDrawable.CENTER);
//            myBubble.setCornerRadius(20);
//            myBubble.setPadding(25, 25, 25, 25);
//            textView.setBackgroundDrawable(myBubble);
//            mainParentChld1Child.addView(textView);
//            mainParentChld1.addView(mainParentChld1Child);
//            addView(mainParentChld1);
//            addView(mainParentChld2);

//            addView(mainParent);
        }
    }
}