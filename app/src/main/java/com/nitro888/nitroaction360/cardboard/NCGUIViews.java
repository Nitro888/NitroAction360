package com.nitro888.nitroaction360.cardboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.nitro888.nitroaction360.R;

/**
 * Created by nitro888 on 15. 4. 12..
 */
public class NCGUIViews extends ViewGroup {
    private static final String TAG = NCGUIViews.class.getSimpleName();
    private final Context   mContext;

    private final int       mPlayControllerLayoutID;
    private GridLayout      mPlayController     = null;
    private final int       mBrowserControllerLayoutID;
    private GridLayout      mBrowserController  = null;
    private final int       mSettingControllerLayoutID;
    private GridLayout      mSettingController  = null;

    private int             mSelectImgBtnID     = -1;

    public NCGUIViews(Context context, AttributeSet attrs, int playerID, int browserID, int settingID) {
        super(context, attrs);

        mContext                    = context;
        mPlayControllerLayoutID     = playerID;
        mBrowserControllerLayoutID  = browserID;
        mSettingControllerLayoutID  = settingID;
    }

    public void layoutReset(int showGUI) {

        int[] layout;

        layout  = getCenter(mPlayController);
        mPlayController.layout(layout[0],layout[1],
                mPlayController.getWidth()+layout[0],mPlayController.getHeight()+layout[1]);

        layout  = getCenter(mBrowserController);
        mBrowserController.layout(layout[0],layout[1],
                mBrowserController.getWidth()+layout[0],mBrowserController.getHeight()+layout[1]);

        layout  = getCenter(mSettingController);
        mSettingController.layout(layout[0],layout[1],
                mSettingController.getWidth()+layout[0],mSettingController.getHeight()+layout[1]);

        switch (showGUI) {
            case 0: // play controller
                mPlayController.setVisibility(View.VISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case 1: // browser controller
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.VISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case 2: // setting controller
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void moveTo(int x, int z) {
        if(mPlayController.getVisibility()==View.VISIBLE) {
            moveTo(mPlayController,x,z);
            lookAtBtn(mPlayController);
        }
        if(mBrowserController.getVisibility()==View.VISIBLE) {
            moveTo(mBrowserController,x,z);
            lookAtBtn(mBrowserController);
        }
        if(mSettingController.getVisibility()==View.VISIBLE) {
            moveTo(mSettingController,x,z);
            lookAtBtn(mSettingController);
        }
    }

    public void updateBrowserController(BitmapDrawable[] thumbnails) {
        for(int i = 3 ; i < mBrowserController.getChildCount() ; i++)
            mBrowserController.getChildAt(i).setVisibility(View.INVISIBLE);

        for(int i = 0 ; i < thumbnails.length ; i++) {
            if(thumbnails[i]==null)
                ((ImageButton)mBrowserController.getChildAt(i+3)).setImageResource(R.drawable.ic_folder_black_48dp);
            else {
                ((ImageButton)mBrowserController.getChildAt(i+3)).setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                ((ImageButton)mBrowserController.getChildAt(i+3)).setBackground(thumbnails[i]);
            }

            mBrowserController.getChildAt(i+3).setVisibility(View.VISIBLE);
        }
    }

    private void moveTo(GridLayout views, int x, int y) {
        int[] layout= getCenter(views);

        int l   = layout[0]-x;
        int t   = layout[1]-y;

        views.layout(l,t,l+views.getWidth(),t+views.getHeight());
    }

    private void lookAtBtn(GridLayout views) {
        float cX        = getWidth()*0.5f;
        float cY        = getHeight()*0.5f;
        float left      = views.getLeft();
        float top       = views.getTop();

        mSelectImgBtnID = -1;

        for(int i = 0 ; i < views.getChildCount() ; i++) {
            float l = left+views.getChildAt(i).getLeft();
            float t = top+views.getChildAt(i).getTop();
            float r = l + views.getChildAt(i).getWidth();
            float b = t + views.getChildAt(i).getHeight();

            if((l<cX)&(cX<r)&(t<cY)&(cY<b)) {
                if(views.getChildAt(i).getVisibility()==View.VISIBLE) {
                    views.getChildAt(i).setAlpha(1.0f);
                    mSelectImgBtnID   = views.getChildAt(i).getId();
                }
            }
            else {
                views.getChildAt(i).setAlpha(0.2f);
            }
        }
    }

    public int getLookAtBtnID() {
        return mSelectImgBtnID;
    }

    private void changeParent(){
        mPlayController     = (GridLayout) ((Activity) mContext).findViewById(mPlayControllerLayoutID);
        mBrowserController  = (GridLayout) ((Activity) mContext).findViewById(mBrowserControllerLayoutID);
        mSettingController  = (GridLayout) ((Activity) mContext).findViewById(mSettingControllerLayoutID);

        ViewGroup parent= (ViewGroup) mPlayController.getParent();
        parent.removeViewInLayout(mPlayController);
        addView(mPlayController);

        parent= (ViewGroup) mBrowserController.getParent();
        parent.removeViewInLayout(mBrowserController);
        addView(mBrowserController);

        parent= (ViewGroup) mSettingController.getParent();
        parent.removeViewInLayout(mSettingController);
        addView(mSettingController);

        layoutReset(0);
    }

    private int[] getCenter(View view) {
        float fX= 0.5f;
        float fY= 0.52f;
        int hW  = (int)(getWidth()*fX) - view.getWidth()/2;
        int hH  = (int)(getHeight()*fY) - view.getHeight()/2;

        int[] layout    = new int[2];

        layout[0]   = hW;
        layout[1]   = hH;

        return layout;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout (" + left + "," + top + "," + right + "," + bottom + ")");

        if(mPlayController==null) changeParent();
        //Log.d(TAG,"onLayout ("+mPlayController.getX()+","+mPlayController.getY()+","+mPlayController.getWidth()+","+mPlayController.getHeight()+")");
    }
}