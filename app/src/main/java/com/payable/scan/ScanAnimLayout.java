package com.payable.scan;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class ScanAnimLayout {

    public static void slideToAbove(RelativeLayout midLine) {

        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -110);
        slide.setDuration(2000);

        midLine.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                midLine.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(midLine.getWidth(), midLine.getHeight());
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                midLine.setLayoutParams(lp);

                slideToDown(midLine);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public static void slideToDown(RelativeLayout midLine) {

        Animation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 110);
        slide.setDuration(2000);

        midLine.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                midLine.clearAnimation();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(midLine.getWidth(), midLine.getHeight());
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                midLine.setLayoutParams(lp);

                slideToAbove(midLine);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
