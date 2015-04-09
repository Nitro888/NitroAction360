package com.nitro888.nitroaction360.utils;

/**
 * Created by nitro888 on 15. 4. 9..
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/*
https://github.com/pscholl/glass_snippets/blob/master/lib/src/main/java/de/tud/ess/HeadListView.java
*/
public class HeadListView extends ListView implements SensorEventListener {

    private static final float INVALID_X = 10;
    private static final String TAG = HeadListView.class.getSimpleName();
    private Sensor mSensor;
    private int mLastAccuracy;
    private SensorManager mSensorManager;
    private float mStartX = INVALID_X;
    private static final int SENSOR_RATE_uS = 400000;
    private static final float VELOCITY = (float) (Math.PI / 180 * 2); // scroll one item per 2°
    /**
     * our content is larger than our view, and thus there is something to scroll
     */
    private boolean canScroll;


    public HeadListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public HeadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeadListView(Context context) {
        super(context);
        init();
    }

    public void init() {
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        if (isInEditMode())
            return;

        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void activate() {
        if (mSensor == null)
            return;

        mStartX = INVALID_X;
        lastPosition = -1;
        mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
        Log.d(TAG, "Automatic scrolling enabled");
    }

    public void deactivate() {
        mSensorManager.unregisterListener(this);
        mStartX = INVALID_X;
        lastPosition = -1;
        Log.d(TAG, "Automatic scrolling disabled");
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);

        if (isSensorRequired()) {
            activate();
        } else {
            deactivate();
        }
    }

    private boolean isSensorRequired() {
        ListAdapter adapter = getAdapter();
        return adapter != null && adapter.getCount() > 1 && getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        canScroll = getAdapter() != null && (getChildCount() < getAdapter().getCount());
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isSensorRequired())
            activate();
        else {
            deactivate();
        }
    }

    @Deprecated
    private boolean isContentBiggerThanView() {
        View a = getChildAt(0),
                b = getChildAt(getChildCount() - 1);

        return (a != null && b != null) && (getListPaddingTop() < a.getTop() ||	b.getBottom() > getBottom() - getListPaddingBottom());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        mLastAccuracy = accuracy;
    }

    protected int lastPosition = -1;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mat = new float[9],
                orientation = new float[3];

        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        SensorManager.getRotationMatrixFromVector(mat, event.values);
        SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
        SensorManager.getOrientation(mat, orientation);

        float z = orientation[0], // see https://developers.google.com/glass/develop/gdk/location-sensors/index
                x = orientation[1],
                y = orientation[2];

        if (mStartX == INVALID_X)
            mStartX = x;

        int position = (int) ((mStartX - x) * -1 / VELOCITY);

        if (position < 0) {
            mStartX = x;
            position = 0;
        } else if (position >= getCount()) {
            float endX = (getCount() * VELOCITY) + mStartX;
            mStartX += x - endX;
            position = getCount()-1;
        }

        if (lastPosition != position) {
//			Log.d(TAG, "Selecting pos. "+ position);
            if (canScroll) {
                smoothScrollToPosition(position);
            }
            setSelection(position);
            lastPosition = position;
        }
    }

}