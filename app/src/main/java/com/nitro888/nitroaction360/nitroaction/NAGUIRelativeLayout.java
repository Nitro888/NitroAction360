package com.nitro888.nitroaction360.nitroaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;

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

    private Vibrator            mVibrator;

    private int                 mLookAtBtnIndex             = -1;
    private int                 mLookAtBtnResourceID        = -1;
    private boolean             isActivateGUI               = false;

    public NAGUIRelativeLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        mContext            = context;
        // init Vibrator
        mVibrator           = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public NAGUIRelativeLayout (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mContext            = context;
        // init Vibrator
        mVibrator           = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initLayout() {
        mPlayController     = (GridLayout) findViewById(mPlayControllerLayoutID);
        mBrowserController  = (GridLayout) findViewById(mBrowserControllerLayoutID);
        mSettingController  = (GridLayout) findViewById(mSettingControllerLayoutID);
        menuOpen(-1);
        mFinishInit         = true;
    }

    public void onCardboardTrigger() {
        mVibrator.vibrate(50);

        if(!isActivateGUI) {
            isActivateGUI   = true;
            menuOpen(mPlayControllerLayoutID);
        } else {
            if(mLookAtBtnIndex!=-1) {
                onGUIButtonClick();
            } else {
                isActivateGUI   = false;
                menuOpen(-1);
            }
        }
    }

    private void menuOpen(int showGUI) {
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

    private void onGUIButtonClick() {
        final ImageButton btn = ((ImageButton)findViewById(mLookAtBtnResourceID));

        btn.performClick();
        btn.setPressed(true);
        btn.invalidate();

        btn.postDelayed(new Runnable() {
            public void run() {
                btn.setPressed(false);
                btn.invalidate();
            }
        }, 40);

        processBtn();
    }

    private void processBtn() {
        switch (mLookAtBtnResourceID) {
            case R.id.btn_close:
            case R.id.btn_back:
                break;
            case R.id.btn_left:
                //browserPreviousPage();
                break;
            case R.id.btn_right:
                //browserNextPage();
                break;
            case R.id.btn_file01:
                //browserSelectItem(0);
                break;
            case R.id.btn_file02:
                //browserSelectItem(1);
                break;
            case R.id.btn_file03:
                //browserSelectItem(2);
                break;
            case R.id.btn_file04:
                //browserSelectItem(3);
                break;
            case R.id.btn_file05:
                //browserSelectItem(4);
                break;
            case R.id.btn_file06:
                //browserSelectItem(5);
                break;
        }
    }

    private void update3DToast() {
        String  msg = "";

        switch (mLookAtBtnResourceID) {
            // browser
            case R.id.btn_left:
                msg = "Previous Page";
                break;
            case R.id.btn_close:
                msg = "Close";
                break;
            case R.id.btn_right:
                msg = "Next Page";
                break;
            case R.id.btn_file01:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+0);
                break;
            case R.id.btn_file02:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+1);
                break;
            case R.id.btn_file03:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+2);
                break;
            case R.id.btn_file04:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+3);
                break;
            case R.id.btn_file05:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+4);
                break;
            case R.id.btn_file06:
                //msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+5);
                break;

            // player
            case R.id.btn_folder:
                msg = "Folder Menu";
                break;
            case R.id.btn_screen_up:
                msg = "Screen Up";
                break;
            case R.id.btn_setting:
                msg = "Setting Menu";
                break;
            case R.id.btn_fast_rewind:
                msg = "Fast Rewind";
                break;
            case R.id.btn_play:
                msg = "Play";
                break;
            case R.id.btn_fast_forward:
                msg = "Fast Forward";
                break;
            case R.id.btn_skip_previous:
                msg = "Skip Previous";
                break;
            case R.id.btn_screen_down:
                msg = "Screen Down";
                break;
            case R.id.btn_skip_next:
                msg = "Skip Next";
                break;

            // setting
            case R.id.btn_sbs_3d:
                msg = "Side By Side 3D Mode";
                break;
            case R.id.btn_dome:
                msg = "Dome Screen";
                break;
            case R.id.btn_screen_sizeup:
                msg = "Screen Size Up";
                break;
            case R.id.btn_2d:
                msg = "2D Mode";
                break;
            case R.id.btn_back:
                msg = "Close";
                break;
            case R.id.btn_screen_sizeReset:
                msg = "Screen Size Reset";
                break;
            case R.id.btn_tb_3d:
                msg = "Top And Bottom 3D Mode";
                break;
            case R.id.btn_panorama:
                msg = "Panorama Screen";
                break;
            case R.id.btn_screen_sizedown:
                msg = "Screen Size Down";
                break;
        }

        ((NACardboardOverlayView)((Activity) mContext).findViewById(R.id.overlay)).show3DToast(msg);
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
        if(mLookAtBtnIndex==-1)
            mLookAtBtnResourceID = mLookAtBtnIndex;
    }

    private void updateBtnAlpha() {
        GridLayout views    = null;

        if(mPlayController.getVisibility()==View.VISIBLE)           views   = mPlayController;
        else if(mBrowserController.getVisibility()==View.VISIBLE)   views   = mBrowserController;
        else if(mSettingController.getVisibility()==View.VISIBLE)   views   = mSettingController;

        if(views!=null) {
            for(int i = 0 ; i < views.getChildCount() ; i++) {
                if(mLookAtBtnIndex==i)  {
                    views.getChildAt(i).setAlpha(1.0f);
                    mLookAtBtnResourceID    = views.getChildAt(i).getId();
                }
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
        update3DToast();

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