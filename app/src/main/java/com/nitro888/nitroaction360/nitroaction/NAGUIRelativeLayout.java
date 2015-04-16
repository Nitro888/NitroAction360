package com.nitro888.nitroaction360.nitroaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nitro888.nitroaction360.MainActivity;
import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.nitro888.nitroaction360.utils.FileExplorer;
import com.nitro888.nitroaction360.utils.ScreenTypeHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nitro888 on 15. 4. 13..
 */
public class NAGUIRelativeLayout extends RelativeLayout {
    private static final String TAG                         = NAGUIRelativeLayout.class.getSimpleName();
    private final Context       mContext;

    private NAViewsToGLRenderer mNAViewsToGLRenderer        = null;

    private static final int    GUI_PLAYER_CTRL             = R.id.Player;
    private static final int    GUI_BROWSER_CTRL            = R.id.Browser;
    private static final int    GUI_SETTING_CTRL            = R.id.Setting;
    public static  final int    ITEMS_PER_PAGE              = 6;

    private GridLayout          mPlayController             = null;
    private GridLayout          mBrowserController          = null;
    private GridLayout          mSettingController          = null;
    private boolean             mFinishInit                 = false;

    private Vibrator            mVibrator;

    private int                 mLookAtBtnIndex             = -1;
    private int                 mLookAtBtnResourceID        = -1;
    private boolean             isActivateGUI               = false;
    private int                 mActivateGUILayerID         = GUI_BROWSER_CTRL;

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
        mPlayController     = (GridLayout) findViewById(GUI_PLAYER_CTRL);
        mBrowserController  = (GridLayout) findViewById(GUI_BROWSER_CTRL);
        mSettingController  = (GridLayout) findViewById(GUI_SETTING_CTRL);
        menuOpen(-1);
        mFinishInit         = true;
    }

    public void onCardboardTrigger() {
        mVibrator.vibrate(50);

        if(!isActivateGUI) {
            menuOpen(mActivateGUILayerID);
            ((MainActivity) mContext).pause();
        } else {
            if(mLookAtBtnIndex!=-1) {
                onGUIButtonClick(mLookAtBtnResourceID);
            } else {
                menuOpen(-1);
            }
        }
    }

    private void menuOpen(int showGUI) {
        switch (showGUI) {
            case GUI_PLAYER_CTRL:   // play controller
                mActivateGUILayerID = showGUI;
                mPlayController.setVisibility(View.VISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case GUI_BROWSER_CTRL:  // browser controller
                mActivateGUILayerID = showGUI;
                browserSelectDir(mFolder);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.VISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
            case GUI_SETTING_CTRL:  // setting controller
                mActivateGUILayerID = showGUI;
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.VISIBLE);
                break;
            default:
                mActivateGUILayerID = GUI_PLAYER_CTRL;
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                break;
        }

        if(showGUI==-1)     isActivateGUI   = false;
        else                isActivateGUI   = true;
    }

    public void onGUIButtonClick(int btnID) {
        mLookAtBtnResourceID    = btnID;

        final ImageButton btn = ((ImageButton)findViewById(mLookAtBtnResourceID));

        //btn.performClick();
        /*
        btn.setPressed(true);
        btn.invalidate();

        btn.postDelayed(new Runnable() {
            public void run() {
                btn.setPressed(false);
                btn.invalidate();
            }
        }, 100);
        */
        processBtn();
    }

    /*
        UI Control  - browser
    */
    private String                      mFolder             = "";
    private int                         mFolderPage         = 0;
    private List<String>[]              mFolderFiles;
    private final List<String>          mFolderThumbnails   = new ArrayList<String>();

    private void browserSelectDir(String folder){
        if(folder.equals(""))
            mFolder = FileExplorer.getRoot();
        else
            mFolder = folder;

        mFolderFiles    = FileExplorer.getDir(mFolder);
        mFolderPage     = 0;
        mFolderThumbnails.clear();

        for(int i=0 ; i < mFolderFiles[0].size() ; i++)
            if(FileExplorer.selectItem(mFolderFiles[0].get(i))==0)
                mFolderThumbnails.add("");
            else if(FileExplorer.selectItem(mFolderFiles[0].get(i))==1)
                mFolderThumbnails.add(mFolderFiles[0].get(i));

        updateBrowserController();
    }

    private void browserNextPage(){
        mFolderPage++;
        int maxPage = (mFolderThumbnails.size()%ITEMS_PER_PAGE)>0?(mFolderThumbnails.size()/ITEMS_PER_PAGE)+1:mFolderThumbnails.size()/ITEMS_PER_PAGE;
        if(mFolderPage>=maxPage) mFolderPage=maxPage-1;
        updateBrowserController();
    }

    private void browserPreviousPage(){
        mFolderPage--;
        if(mFolderPage<0)       mFolderPage=0;
        updateBrowserController();
    }

    private void updateBrowserController() {
        int listStart   = mFolderPage*ITEMS_PER_PAGE;
        int listEnd     = mFolderThumbnails.size()>(listStart+ITEMS_PER_PAGE)?listStart+ITEMS_PER_PAGE:mFolderThumbnails.size();

        final BitmapDrawable[] thumbnails   = new BitmapDrawable[listEnd-listStart];
        for(int i=0 ; i < thumbnails.length ; i++ ) {
            if(mFolderThumbnails.get(i+listStart).equals(""))
                thumbnails[i]   = null;
            else
                thumbnails[i]   = new BitmapDrawable(getResources(),
                        ThumbnailUtils.createVideoThumbnail(
                                mFolderThumbnails.get(i + listStart), MediaStore.Video.Thumbnails.MICRO_KIND));
        }

        updateBrowserController(thumbnails);
    }

    private void browserSelectItem(int btnIndex){
        int type = FileExplorer.selectItem(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));

        switch (type) {
            case 0:
                browserSelectDir(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                break;
            case 1:
                menuOpen(-1);
                ((MainActivity) mContext).openMovie(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                break;
        }
    }

    private void updateBrowserController(BitmapDrawable[] thumbnails) {
        for(int i = 3 ; i < mBrowserController.getChildCount() ; i++)
            mBrowserController.getChildAt(i).setVisibility(View.INVISIBLE);

        Drawable    backImg = mBrowserController.getChildAt(0).getBackground();

        int width   = 0;
        int height  = 0;

        for(int i = 0 ; i < thumbnails.length ; i++) {
            width   = mBrowserController.getChildAt(i+3).getWidth();
            height   = mBrowserController.getChildAt(i+3).getHeight();

            mBrowserController.getChildAt(i+3).setVisibility(View.VISIBLE);
            ((ImageButton)mBrowserController.getChildAt(i+3)).setImageResource(
                    thumbnails[i] == null ? R.drawable.ic_folder_white_48dp : R.drawable.ic_play_circle_outline_white_48dp
            );

            mBrowserController.getChildAt(i+3).setBackground(thumbnails[i]==null?backImg:thumbnails[i]);

            ((ImageButton)mBrowserController.getChildAt(i+3)).setMaxWidth(width);
            ((ImageButton)mBrowserController.getChildAt(i+3)).setMaxHeight(height);

        }
    }
    /*
        UI Control  - browser
    */

    private void processBtn() {
        switch (mLookAtBtnResourceID) {
            case R.id.btn_close:
            case R.id.btn_back:
                menuOpen(-1);
                break;
            case R.id.btn_left:
                browserPreviousPage();
                break;
            case R.id.btn_right:
                browserNextPage();
                break;
            case R.id.btn_file01:
                browserSelectItem(0);
                break;
            case R.id.btn_file02:
                browserSelectItem(1);
                break;
            case R.id.btn_file03:
                browserSelectItem(2);
                break;
            case R.id.btn_file04:
                browserSelectItem(3);
                break;
            case R.id.btn_file05:
                browserSelectItem(4);
                break;
            case R.id.btn_file06:
                browserSelectItem(5);
                break;

            // player
            case R.id.btn_folder:
                menuOpen(GUI_BROWSER_CTRL);
                break;
            case R.id.btn_screen_up:
                ((MainActivity) mContext).setScreenTiltPosition(1.0f);
                break;
            case R.id.btn_setting:
                menuOpen(GUI_SETTING_CTRL);
                break;
            case R.id.btn_fast_rewind:
                ((MainActivity) mContext).fastRewind();
                break;
            case R.id.btn_play:
                ((MainActivity) mContext).play();
                break;
            case R.id.btn_fast_forward:
                ((MainActivity) mContext).fastForward();
                break;
            case R.id.btn_skip_previous:
                ((MainActivity) mContext).skipPrevious();
                break;
            case R.id.btn_screen_down:
                ((MainActivity) mContext).setScreenTiltPosition(-1.0f);
                break;
            case R.id.btn_skip_next:
                ((MainActivity) mContext).skipNext();
                break;

            // setting
            case R.id.btn_sbs_3d:
                ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_3D_SBS);
                break;
            case R.id.btn_dome:
                ((MainActivity) mContext).setScreenShapeType(ScreenTypeHelper.SCREEN_SHAPE_DOME);
                break;
            case R.id.btn_screen_sizeup:
                ((MainActivity) mContext).setScreenScale(1.0f);
                break;
            case R.id.btn_2d:
                ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_2D);
                break;
            case R.id.btn_screen_sizeReset:
                ((MainActivity) mContext).setScreenScale(0.0f);
                break;
            case R.id.btn_tb_3d:
                ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_3D_TLBR);
                break;
            case R.id.btn_panorama:
                ((MainActivity) mContext).setScreenShapeType(ScreenTypeHelper.SCREEN_SHAPE_CURVE);
                break;
            case R.id.btn_screen_sizedown:
                ((MainActivity) mContext).setScreenScale(-1.0f);
                break;
        }
    }

    private void update3DToast() {
        String  msg = "";

        if((mLookAtBtnResourceID!=-1)&&(findViewById(mLookAtBtnResourceID).getVisibility()==View.VISIBLE)) {
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
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+0);
                    break;
                case R.id.btn_file02:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+1);
                    break;
                case R.id.btn_file03:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+2);
                    break;
                case R.id.btn_file04:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+3);
                    break;
                case R.id.btn_file05:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+4);
                    break;
                case R.id.btn_file06:
                    msg = mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+5);
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
        }

        ((NACardboardOverlayView)((Activity) mContext).findViewById(R.id.overlay)).show3DToast(msg);
    }

    public void lookAtBtn(int indexBtn) {
        mLookAtBtnIndex = indexBtn;
        if(mLookAtBtnIndex==-1)
            mLookAtBtnResourceID = mLookAtBtnIndex;
    }

    private void updateBtnColorA() {
        GridLayout views    = null;

        if(mPlayController.getVisibility()==View.VISIBLE)           views   = mPlayController;
        else if(mBrowserController.getVisibility()==View.VISIBLE)   views   = mBrowserController;
        else if(mSettingController.getVisibility()==View.VISIBLE)   views   = mSettingController;

        if(views!=null) {
            for(int i = 0 ; i < views.getChildCount() ; i++) {
                if(mLookAtBtnIndex==i)  {
                    ((ImageButton)views.getChildAt(i)).setColorFilter(Color.rgb(255, 255, 255));
                    views.getChildAt(i).setAlpha(1.0f);
                    mLookAtBtnResourceID    = views.getChildAt(i).getId();
                }
                else {
                    ((ImageButton)views.getChildAt(i)).setColorFilter(Color.rgb(128, 128, 128));
                    views.getChildAt(i).setAlpha(0.6f);
                }
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

        updateBtnColorA();
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