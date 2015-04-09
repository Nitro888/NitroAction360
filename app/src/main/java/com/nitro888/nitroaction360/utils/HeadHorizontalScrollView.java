package com.nitro888.nitroaction360.utils;

/**
 * Created by nitro888 on 15. 4. 9..
 */
import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.HorizontalScrollView;

/*
https://github.com/pscholl/glass_snippets/blob/master/lib/src/main/java/de/tud/ess/HeadScrollView.java
*/
public class HeadHorizontalScrollView extends HorizontalScrollView implements SensorEventListener {

    private static final String TAG = HeadHorizontalScrollView.class.getSimpleName();

    private ListAdapter adapter;
    private DataSetObserver dataSetObserver;
    private LinearLayout layout;

    private class AdapterObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            layout.removeAllViews();
            fillFromAdapter();
        }

        @Override
        public void onInvalidated() {
            layout.removeAllViews();
        }
    }

    public HeadHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public HeadHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeadHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
    }

    private Sensor mSensor;
    private int mLastAccuracy;
    private SensorManager mSensorManager;
    private float mStartR = 10;
    private static final int SENSOR_RATE_uS = 200000;
    private static final float VELOCITY = -1000; // from rad to pixels

    public void activate() {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }

        mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
        mStartR = 10;
    }

    public void deactivate() {
        mStartR = 10;

        if (mSensorManager == null)
            return;

        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && needsScrolling()) activate();
        else deactivate();
    }


//	@Override //Not called in CardScrollView
//	protected void onDisplayHint(int hint) {
//		super.onDisplayHint(hint);
//
//		if (hint == VISIBLE && needsScrolling()) activate();
//		else deactivate();
//	}

    private boolean needsScrolling() {
        //return getPaddingTop() < getChildAt(0).getTop() || getChildAt(getChildCount()-1).getBottom() > getBottom() - getPaddingBottom();
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        mLastAccuracy = accuracy;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mat = new float[9],
                orientation = new float[3];

        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        SensorManager.getRotationMatrixFromVector(mat, event.values);
        SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
        SensorManager.getOrientation(mat, orientation);

        float rot = orientation[0];   // azimuth

        if (mStartR == 10)
            mStartR = rot;

        float mEndY = mStartR - (getChildAt(0).getWidth() - getWidth() * 0.5F) / VELOCITY;

        int prior = getScrollX(),
                pos = (int) ((mStartR - rot) * VELOCITY);

        if (rot < mStartR) mStartR = rot;
        else if (rot > mEndY) mStartR += rot - mEndY;

        smoothScrollTo(pos, 0);
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter == this.adapter)
            return;

        if (dataSetObserver == null)
            dataSetObserver = new AdapterObserver();

        if (layout == null) {
            layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            this.addView(layout);
        }

        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
            layout.removeAllViews();
        }

        if (adapter != null) {

            this.adapter = adapter;
            this.adapter.registerDataSetObserver(dataSetObserver);

            fillFromAdapter();
        }
    }

    private void fillFromAdapter() {
        int last = adapter.getCount()-1;

        for (int i = 0; i < last; i++) {
            View v = adapter.getView(i, null, this);
            layout.addView(v);
            View divider = new View(getContext());
            divider.setBackgroundColor(0xFF333333);
            layout.addView(divider, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 1));
        }

        View v = adapter.getView(last, null, this);
        layout.addView(v);
    }
}