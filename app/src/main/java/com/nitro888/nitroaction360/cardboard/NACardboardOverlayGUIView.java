package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by nitro888 on 15. 4. 10..
 */
public class NACardboardOverlayGUIView extends LinearLayout implements SensorEventListener {
    private final CardboardOverlayEyeView   leftView;
    private final CardboardOverlayEyeView   rightView;
    private AlphaAnimation                  textFadeAnimation;

    public NACardboardOverlayGUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        leftView = new CardboardOverlayEyeView(context, attrs);
        leftView.setLayoutParams(params);
        addView(leftView);

        rightView = new CardboardOverlayEyeView(context, attrs);
        rightView.setLayoutParams(params);
        addView(rightView);

        // Set some reasonable defaults.
        setDepthOffset(0.016f);
        setColor(Color.rgb(150, 255, 180));
        setVisibility(View.VISIBLE);

        textFadeAnimation = new AlphaAnimation(1.0f, 0.0f);
        textFadeAnimation.setDuration(5000);

        // init sensor
        initSensor();
    }

    public void show3DToast(String message) {
        setText(message);
        setTextAlpha(1f);
        textFadeAnimation.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setTextAlpha(0f);
            }
        });
        startAnimation(textFadeAnimation);
    }

    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override public void onAnimationRepeat(Animation animation) {}
        @Override public void onAnimationStart(Animation animation) {}
    }

    private void setDepthOffset(float offset) {
        leftView.setOffset(offset);
        rightView.setOffset(-offset);
    }

    private void setText(String text) {
        leftView.setText(text);
        rightView.setText(text);
    }

    private void setTextAlpha(float alpha) {
        leftView.setTextViewAlpha(alpha);
        rightView.setTextViewAlpha(alpha);
    }

    private void setColor(int color) {
        leftView.setColor(color);
        rightView.setColor(color);
    }

    /*
        for head tracking
        https://github.com/pscholl/glass_snippets/blob/master/lib/src/main/java/de/tud/ess/HeadListView.java
    */
    private static final float  INVALID_X = 10;
    private static final float  INVALID_Z = 10;
    private static final String TAG = NACardboardOverlayGUIView.class.getSimpleName();
    private Sensor              mSensor;
    private SensorManager       mSensorManager;
    private int                 mLastAccuracyXZ;
    private float               mStartX = INVALID_X;
    private float               mStartZ = INVALID_Z;
    private int                 mLastPositionX = -1;
    private int                 mLastPositionZ = -1;
    private static final int    SENSOR_RATE_uS = 400000;
    private static final float  VELOCITY_X = (float) (Math.PI / 180 * 2); // scroll one item per 2°
    private static final float  VELOCITY_Z = (float) (Math.PI / 180 * 2); // scroll one item per 2°

    public void initSensor() {
        mSensorManager  = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor         = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        //activate();
    }

    public void activate() {
        if (mSensor == null)
            return;

        mStartX = INVALID_X;
        mStartZ = INVALID_Z;

        mLastPositionX = -1;
        mLastPositionZ = -1;

        mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
        Log.d(TAG, "Automatic scrolling enabled");
    }

    public void deactivate() {
        mSensorManager.unregisterListener(this);

        mStartX = INVALID_X;
        mStartZ = INVALID_Z;

        mLastPositionX = -1;
        mLastPositionZ = -1;

        Log.d(TAG, "Automatic scrolling disabled");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        mLastAccuracyXZ = accuracy;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mat = new float[9],
                orientation = new float[3];

        if (mLastAccuracyXZ == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        SensorManager.getRotationMatrixFromVector(mat, event.values);
        SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
        SensorManager.getOrientation(mat, orientation);

        float   z = orientation[0], // see https://developers.google.com/glass/develop/gdk/location-sensors/index
                x = orientation[1],
                y = orientation[2];

        if (mStartX == INVALID_X)   mStartX = x;
        if (mStartZ == INVALID_Z)   mStartZ = z;

        int positionX = (int) ((mStartX - x) * -1 / VELOCITY_X);
        int positionZ = (int) ((mStartZ - z) * -1 / VELOCITY_Z);

        Log.d(TAG, "Look At ( "+positionX+" , " + + positionZ + " )");


    }

    /**
     * A simple view group containing some horizontally centered text underneath a horizontally
     * centered image.
     *
     * <p>This is a helper class for CardboardOverlayView.
     */
    private class CardboardOverlayEyeView extends ViewGroup {
        private final ImageView imageView;
        private final TextView textView;
        private float offset;

        public CardboardOverlayEyeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            imageView = new ImageView(context, attrs);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);  // Preserve aspect ratio.
            addView(imageView);

            textView = new TextView(context, attrs);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
            textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setShadowLayer(3.0f, 0.0f, 0.0f, Color.DKGRAY);
            addView(textView);
        }

        public void setColor(int color) {
            imageView.setColorFilter(color);
            textView.setTextColor(color);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        public void setTextViewAlpha(float alpha) {
            textView.setAlpha(alpha);
        }

        public void setOffset(float offset) {
            this.offset = offset;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            // Width and height of this ViewGroup.
            final int width = right - left;
            final int height = bottom - top;

            // The size of the image, given as a fraction of the dimension as a ViewGroup.
            // We multiply both width and heading with this number to compute the image's bounding
            // box. Inside the box, the image is the horizontally and vertically centered.
            final float imageSize = 0.12f;

            // The fraction of this ViewGroup's height by which we shift the image off the
            // ViewGroup's center. Positive values shift downwards, negative values shift upwards.
            final float verticalImageOffset = -0.07f;

            // Vertical position of the text, specified in fractions of this ViewGroup's height.
            final float verticalTextPos = 0.52f;

            // Layout ImageView
            float imageMargin = (1.0f - imageSize) / 2.0f;
            float leftMargin = (int) (width * (imageMargin + offset));
            float topMargin = (int) (height * (imageMargin + verticalImageOffset));
            imageView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width * imageSize), (int) (topMargin + height * imageSize));

            // Layout TextView
            leftMargin = offset * width;
            topMargin = height * verticalTextPos;
            textView.layout(
                    (int) leftMargin, (int) topMargin,
                    (int) (leftMargin + width), (int) (topMargin + height * (1.0f - verticalTextPos)));
        }
    }
}
