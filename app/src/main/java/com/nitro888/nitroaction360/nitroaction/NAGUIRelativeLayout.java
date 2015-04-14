package com.nitro888.nitroaction360.nitroaction;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nitro888.nitroaction360.R;

/**
 * Created by nitro888 on 15. 4. 13..
 */
public class NAGUIRelativeLayout extends RelativeLayout {
    private static final String TAG                         = NAGUIRelativeLayout.class.getSimpleName();
    private final Context       mContext;

    private NAViewsToGLRenderer mNAViewsToGLRenderer        = null;

    private static final int    mPlayControllerLayoutID     = R.id.Player;
    private static final int    mBrowserControllerLayoutID  = R.id.Browser;
    private static final int    mSettingControllerLayoutID  = R.id.Setting;

    private GridLayout          mPlayController             = null;
    private GridLayout          mBrowserController          = null;
    private GridLayout          mSettingController          = null;
    private boolean             mFinishInit                 = false;
    private int                 mLookAtBtnIndex             = -1;

    public NAGUIRelativeLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        mContext            = context;
    }

    public NAGUIRelativeLayout (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mContext            = context;
    }

    private void initLayout() {
        mPlayController     = (GridLayout) findViewById(mPlayControllerLayoutID);
        mBrowserController  = (GridLayout) findViewById(mBrowserControllerLayoutID);
        mSettingController  = (GridLayout) findViewById(mSettingControllerLayoutID);
        menuOpen(-1);
        mFinishInit         = true;
    }

    public void menuOpen(int showGUI) {
        switch (showGUI) {
            case R.id.Player: // play controller
                mPlayController.setVisibility(View.VISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case R.id.Browser: // browser controller
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.VISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case R.id.Setting: // setting controller
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.VISIBLE);
                break;
            default:
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
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

    public void lookAtBtn(int indexBtn) {
        mLookAtBtnIndex = indexBtn;
    }

    private void updateBtnAlpha() {
        GridLayout views    = null;

        if(mPlayController.getVisibility()==View.VISIBLE)           views   = mPlayController;
        else if(mBrowserController.getVisibility()==View.VISIBLE)   views   = mBrowserController;
        else if(mSettingController.getVisibility()==View.VISIBLE)   views   = mSettingController;

        if(views!=null) {
            for(int i = 0 ; i < views.getChildCount() ; i++) {
                if(mLookAtBtnIndex==i)  views.getChildAt(i).setAlpha(1.0f);
                else                    views.getChildAt(i).setAlpha(0.5f);
            }
        }
    }

    //-------------------------------------------------------------------
    // from ViewToGLRenderer
    // https://github.com/ArtemBogush/AndroidViewToGLRendering
    //-------------------------------------------------------------------
    // draw magic
    @Override
    protected void dispatchDraw( Canvas canvas ) {
        if(mNAViewsToGLRenderer==null)  return;
        if(!mFinishInit)                initLayout();

        updateBtnAlpha();

        //returns canvas attached to gl texture to draw on
        Canvas glAttachedCanvas = mNAViewsToGLRenderer.onDrawViewBegin(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI);
        if(glAttachedCanvas != null) {
            //translate canvas to reflect view scrolling
            glAttachedCanvas.translate(-getScrollX(), -getScrollY());
            //draw the view to provided canvas
            super.dispatchDraw(glAttachedCanvas);
        }
        // notify the canvas is updated
        mNAViewsToGLRenderer.onDrawViewEnd(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI);
    }
    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer = viewTOGLRenderer;
    }
}