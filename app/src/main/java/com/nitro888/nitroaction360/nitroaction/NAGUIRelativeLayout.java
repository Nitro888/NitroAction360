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

    public NAGUIRelativeLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        mContext                = context;
        initLayout();
    }

    public NAGUIRelativeLayout (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mContext                = context;
        initLayout();
    }

    private void initLayout() {
        mPlayController     = (GridLayout) findViewById(mPlayControllerLayoutID);
        mBrowserController  = (GridLayout) findViewById(mBrowserControllerLayoutID);
        mSettingController  = (GridLayout) findViewById(mSettingControllerLayoutID);
    }

    public void layoutReset(int showGUI) {
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

    //-------------------------------------------------------------------
    // from ViewToGLRenderer
    // https://github.com/ArtemBogush/AndroidViewToGLRendering
    //-------------------------------------------------------------------
    // draw magic
    @Override
    protected void dispatchDraw( Canvas canvas ) {
        if(mNAViewsToGLRenderer==null) return;

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