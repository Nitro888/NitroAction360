package com.nitro888.nitroaction360.nitroaction;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.akoscz.youtube.PlaylistItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nitro888.nitroaction360.MainActivity;
import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.nitro888.nitroaction360.utils.FileExplorer;
import com.nitro888.nitroaction360.utils.ScreenTypeHelper;
import com.nitro888.nitroaction360.utils.YouTubePlayListHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitro888 on 15. 4. 13..
 */
public class NAGUIRelativeLayout extends RelativeLayout {
    private static final String TAG                         = NAGUIRelativeLayout.class.getSimpleName();
    private final Context       mContext;

    private NAViewsToGLRenderer mNAViewsToGLRenderer        = null;

    private static final int    GUI_AD_CTRL                 = R.id.GUI_AD;

    private static final int    GUI_PLAYER_CTRL             = R.id.GUI_Player;
    private static final int    GUI_PLAYER_BTN_CTRL         = R.id.GUI_Player_Btns;
    private static final int    GUI_PLAYER_TITLE_CTRL       = R.id.GUI_Player_Title;
    private static final int    GUI_PLAYER_TIME_CTRL        = R.id.GUI_Player_Time;
    private static final int    GUI_PLAYER_PROGRESS_CTRL    = R.id.GUI_Player_ProgressBar;

    private static final int    GUI_BROWSER_CTRL            = R.id.GUI_Browser;
    private static final int    GUI_SETTING_CTRL            = R.id.GUI_Setting;
    private static final int    GUI_YOUTUBE_CATEGORY_CTRL   = R.id.GUI_YouTube_Category;
    private static final int    GUI_YOUTUBE_PLAYLIST_CTRL   = R.id.GUI_YouTube_Playlist;

    public static  final int    MENU_PER_PAGE               = 3;
    public static  final int    ITEMS_PER_PAGE              = 6;

    private RelativeLayout      mAdController               = null;
    private TableLayout         mPlayController             = null;
    private GridLayout          mPlayBtnController          = null;
    private TextView            mPlayTextTitleController    = null;
    private TextView            mPlayTextTimeController     = null;
    private SeekBar             mPlayerProgress             = null;
    private GridLayout          mBrowserController          = null;
    private GridLayout          mSettingController          = null;
    private GridLayout          mYoutubeCategoryController  = null;
    private GridLayout          mYoutubePlaylistController  = null;
    private boolean             mFinishInit                 = false;

    private Vibrator            mVibrator;

    private int                 mLookAtBtnIndex             = -1;
    private int                 mLookAtBtnResourceID        = -1;
    private boolean             isActivateGUI               = false;
    private int                 mActivateGUILayerID         = GUI_AD_CTRL;

    // for adView
    private AdView              mAdView                     = null;
    private AdRequest           mADRequest;

    public NAGUIRelativeLayout(Context context) {
        super(context);
        //setWillNotDraw(false);
        mContext            = context;
        // init Vibrator
        mVibrator           = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public NAGUIRelativeLayout (Context context, AttributeSet attrs) {
        super(context, attrs);
        //setWillNotDraw(false);
        mContext            = context;
        // init Vibrator
        mVibrator           = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initLayout() {
        mAdController               = (RelativeLayout)  findViewById(GUI_AD_CTRL);
        mPlayController             = (TableLayout)     findViewById(GUI_PLAYER_CTRL);

        mPlayBtnController          = (GridLayout)      findViewById(GUI_PLAYER_BTN_CTRL);
        mPlayTextTitleController    = (TextView)        findViewById(GUI_PLAYER_TITLE_CTRL);
        mPlayTextTimeController     = (TextView)        findViewById(GUI_PLAYER_TIME_CTRL);
        mPlayerProgress             = (SeekBar)         findViewById(GUI_PLAYER_PROGRESS_CTRL);

        mBrowserController          = (GridLayout)      findViewById(GUI_BROWSER_CTRL);
        mSettingController          = (GridLayout)      findViewById(GUI_SETTING_CTRL);

        mYoutubeCategoryController  = (GridLayout)      findViewById(GUI_YOUTUBE_CATEGORY_CTRL);
        mYoutubePlaylistController  = (GridLayout)      findViewById(GUI_YOUTUBE_PLAYLIST_CTRL);

        mAdView                     = (AdView)          findViewById(R.id.adView);
        mADRequest                  = new AdRequest.Builder().build();
        mAdView.loadAd(mADRequest);

        updateGUI(true);
        updateTitle("");

        menuOpen(mActivateGUILayerID);
        mFinishInit         = true;

        findViewById(R.id.text_pages).setBackground(findViewById(R.id.btn_left).getBackground());
    }

    public void onCardboardTrigger() {
        mVibrator.vibrate(50);

        if(!isActivateGUI) {
            menuOpen(mActivateGUILayerID);
        } else {
            if(mActivateGUILayerID==GUI_AD_CTRL){
                menuOpen(GUI_PLAYER_CTRL);
            }
            else if(mLookAtBtnIndex!=-1) {
                onGUIButtonClick(mLookAtBtnResourceID);
            } else {
                menuOpen(-1);
            }
        }
    }

    private void menuOpen(int showGUI) {
        mActivateGUILayerID = showGUI;

        switch (mActivateGUILayerID) {
            case GUI_AD_CTRL:       // Ads controller
                mAdController.setVisibility(View.VISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
            case GUI_PLAYER_CTRL:   // play controller
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.VISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
            case GUI_BROWSER_CTRL:  // browser controller
                browserSelectDir(mFolder);
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.VISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
            case GUI_SETTING_CTRL:  // setting controller
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.VISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
            case GUI_YOUTUBE_CATEGORY_CTRL:
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.VISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
            case GUI_YOUTUBE_PLAYLIST_CTRL:
                selectYoutubePlaylist();
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.VISIBLE);
                break;
            default:
                mActivateGUILayerID = GUI_PLAYER_CTRL;
                mAdController.setVisibility(View.INVISIBLE);
                mPlayController.setVisibility(View.INVISIBLE);
                mBrowserController.setVisibility(View.INVISIBLE);
                mSettingController.setVisibility(View.INVISIBLE);
                mYoutubeCategoryController.setVisibility(View.INVISIBLE);
                mYoutubePlaylistController.setVisibility(View.INVISIBLE);
                break;
        }

        if(showGUI==-1)     isActivateGUI   = false;
        else                isActivateGUI   = true;
    }

    public void onGUIButtonClick(int btnID) {
        if( findViewById(mLookAtBtnResourceID) instanceof ImageButton) {
            mLookAtBtnResourceID    = btnID;
            /*
            final ImageButton btn = ((ImageButton)findViewById(mLookAtBtnResourceID));
            btn.performClick();

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
        } else if (findViewById(mLookAtBtnResourceID) instanceof TextView) {
            mLookAtBtnResourceID    = btnID;
            processBtn();
        }
    }

    /*
        UI Control  - browser
    */
    private String                      mFolder             = "";
    private int                         mFolderPage         = 0;
    private int                         mFolderPageMax      = 0;
    private List<String>[]              mFolderFiles;
    private final List<String>          mFolderThumbnails   = new ArrayList<String>();

    private void browserSelectDir(String folder){
        if(folder.equals(""))
            mFolder = FileExplorer.getRoot();
        else
            mFolder = folder;

        mFolderFiles    = FileExplorer.getDir(mFolder);
        mFolderThumbnails.clear();

        for(int i=0 ; i < mFolderFiles[0].size() ; i++)
            if(FileExplorer.selectItem(mFolderFiles[0].get(i))==0)
                mFolderThumbnails.add("");
            else if(FileExplorer.selectItem(mFolderFiles[0].get(i))==1)
                mFolderThumbnails.add(mFolderFiles[0].get(i));

        mFolderPage     = 0;
        mFolderPageMax  = (mFolderThumbnails.size()%ITEMS_PER_PAGE)>0?(mFolderThumbnails.size()/ITEMS_PER_PAGE)+1:mFolderThumbnails.size()/ITEMS_PER_PAGE;

        updateBrowserController();
    }

    private void browserNextPage(){
        int temp = mFolderPage;
        mFolderPage++;
        if(mFolderPage>=mFolderPageMax) mFolderPage=mFolderPageMax-1;
        if(temp!=mFolderPage)   updateBrowserController();
    }

    private void browserPreviousPage(){
        int temp = mFolderPage;
        mFolderPage--;
        if(mFolderPage<0)       mFolderPage=mFolderPageMax-1;
        if(temp!=mFolderPage)   updateBrowserController();
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

    private void updateBrowserController(BitmapDrawable[] thumbnails) {
        for(int i = MENU_PER_PAGE ; i < mBrowserController.getChildCount() ; i++)
            mBrowserController.getChildAt(i).setVisibility(View.INVISIBLE);

        Drawable    backImg = mBrowserController.getChildAt(0).getBackground();

        for(int i = 0 ; i < thumbnails.length ; i++) {
            mBrowserController.getChildAt(i+MENU_PER_PAGE).setVisibility(View.VISIBLE);
            ((ImageButton)mBrowserController.getChildAt(i+MENU_PER_PAGE)).setImageResource(
                    thumbnails[i] == null ? R.drawable.ic_folder_white_48dp : R.drawable.ic_play_circle_outline_white_48dp
            );
            mBrowserController.getChildAt(i+MENU_PER_PAGE).setBackground(thumbnails[i]==null?backImg:thumbnails[i]);
        }
    }

    private void browserSelectItem(int btnIndex){
        int type = FileExplorer.selectItem(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));

        switch (type) {
            case 0:
                browserSelectDir(mFolderFiles[0].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                break;
            case 1:
                menuOpen(-1);
                updateTitle(mFolderFiles[1].get(mFolderPage*ITEMS_PER_PAGE+btnIndex));
                ((MainActivity) mContext).openMovieFile(mFolderFiles[0].get(mFolderPage * ITEMS_PER_PAGE + btnIndex));
                break;
        }
    }
    /*
        UI Control  - browser
    */


    /*
        UI Control  - youtube
    */
    private int     mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_EARTH_PLAYLIST_ID;
    private int     mYoutubePage        = 0;
    private int     mYoutubePageMax     = 0;

    public int getPlayYoutubeCategory(){
        return mYoutubeCategory;
    }

    private void selectYoutubePlaylist() {
        mFolderThumbnails.clear();

        if(!((MainActivity) mContext).isConnected())    return;

        int itemCnt = ((MainActivity) mContext).getYoutubeCount(mYoutubeCategory);

        for(int i=0 ; i < itemCnt ; i++) {
            PlaylistItem p = ((MainActivity) mContext).getYoutubeItem(mYoutubeCategory, i);
            if(p!=null)
                mFolderThumbnails.add(p.thumbnailUrl);
        }

        mYoutubePage     = 0;
        mYoutubePageMax  = (mFolderThumbnails.size()%ITEMS_PER_PAGE)>0?(mFolderThumbnails.size()/ITEMS_PER_PAGE)+1:mFolderThumbnails.size()/ITEMS_PER_PAGE;

        updateYoutubePlaylist();
    }

    private void youtubeNextPage(){
        int temp = mYoutubePage;
        mYoutubePage++;
        if(mYoutubePage>=mYoutubePageMax) mYoutubePage=mYoutubePageMax-1;
        if(temp!=mYoutubePage)   updateYoutubePlaylist();
    }

    private void youtubePreviousPage(){
        int temp = mYoutubePage;
        mYoutubePage--;
        if(mYoutubePage<0)       mYoutubePage=mYoutubePageMax-1;
        if(temp!=mYoutubePage)   updateYoutubePlaylist();
    }

    private void updateYoutubePlaylist() {
        if(!((MainActivity) mContext).isConnected())    return;

        int listStart   = mYoutubePage*ITEMS_PER_PAGE;
        int listEnd     = mFolderThumbnails.size()>(listStart+ITEMS_PER_PAGE)?listStart+ITEMS_PER_PAGE:mFolderThumbnails.size();

        for(int i = MENU_PER_PAGE ; i < mYoutubePlaylistController.getChildCount() ; i++)
            mYoutubePlaylistController.getChildAt(i).setVisibility(View.INVISIBLE);

        for(int i = 0 ; i < (listEnd-listStart) ; i++)
            new DownloadImageTask(mYoutubePlaylistController.getChildAt(i+MENU_PER_PAGE)).execute(mFolderThumbnails.get(i+listStart));
    }

    private class DownloadImageTask extends AsyncTask<String, Void, BitmapDrawable> {
        private View mView;
        public DownloadImageTask(View view) {
            mView   = view;
        }
        protected BitmapDrawable doInBackground(String... urls) {
            return loadDrawableImageFromURL(urls[0]);
        }

        protected void onPostExecute(BitmapDrawable result) {
            mView.setVisibility(View.VISIBLE);
            mView.setBackground(result);
        }
        private BitmapDrawable loadDrawableImageFromURL(String url) {
            try {
                return new BitmapDrawable(getResources(), new URL(url).openStream());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                return null;
            }
        }
    }

    private void youtubeSelectItem(int btnIndex){
        if(((MainActivity) mContext).isConnected()) {
            menuOpen(-1);
            if(((MainActivity) mContext).getYoutubeItem(mYoutubeCategory,mYoutubePage*ITEMS_PER_PAGE+btnIndex)!=null)
                ((MainActivity) mContext).getMoveStreamUrl(((MainActivity) mContext).getYoutubeItem(mYoutubeCategory,mYoutubePage*ITEMS_PER_PAGE+btnIndex).videoId);
        }
    }

    private String getYoutubeItemTitle(int btnIndex) {
        if(((MainActivity) mContext).getYoutubeItem(mYoutubeCategory, mYoutubePage*ITEMS_PER_PAGE+btnIndex)!=null)
            return ((MainActivity) mContext).getYoutubeItem(mYoutubeCategory, mYoutubePage*ITEMS_PER_PAGE+btnIndex).title;

        return "";
    }
    /*
        UI Control  - youtube
    */

    private void processBtn() {
        if((mLookAtBtnResourceID!=-1)&&(findViewById(mLookAtBtnResourceID).getVisibility()==View.VISIBLE))
        {
            switch (mLookAtBtnResourceID) {
                // browser
                case R.id.btn_left:
                    browserPreviousPage();
                    break;
                case R.id.btn_right:
                    browserNextPage();
                    break;
                case R.id.text_pages:
                    menuOpen(GUI_PLAYER_CTRL);
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
                case R.id.btn_youtube:
                    menuOpen(GUI_YOUTUBE_CATEGORY_CTRL);
                    break;
                case R.id.btn_setting:
                    menuOpen(GUI_SETTING_CTRL);
                    break;
                case R.id.btn_fast_rewind:
                    ((MainActivity) mContext).fastRewind();
                    break;
                case R.id.btn_stop_pause_play:
                    ((MainActivity) mContext).playOrPause();
                    break;
                case R.id.btn_fast_forward:
                    ((MainActivity) mContext).fastForward();
                    break;

                // setting
                case R.id.btn_sbs_3d:
                    ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_3D_SBS);
                    break;
                case R.id.btn_screen_up:
                    ((MainActivity) mContext).setScreenTiltPosition(1.0f);
                    break;
                case R.id.btn_panorama:
                    ((MainActivity) mContext).setScreenShapeType(ScreenTypeHelper.SCREEN_SHAPE_CURVE);
                    break;

                case R.id.btn_screen_sizedown:
                    ((MainActivity) mContext).setScreenScale(-1.0f);
                    break;
                case R.id.btn_2d:
                    ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_2D);
                    break;
                case R.id.btn_screen_sizeup:
                    ((MainActivity) mContext).setScreenScale(1.0f);
                    break;
                case R.id.btn_tb_3d:
                    ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_3D_TLBR);
                    break;
                case R.id.btn_screen_down:
                    ((MainActivity) mContext).setScreenTiltPosition(-1.0f);
                    break;
                case R.id.btn_dome:
                    ((MainActivity) mContext).setScreenShapeType(ScreenTypeHelper.SCREEN_SHAPE_DOME);
                    break;

                // youtube category
                case R.id.btn_youtube_earth:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_EARTH_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_space:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_SPACE_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_game:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_GAME_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_mv:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_MV_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_movie:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_MOVIE_TRAILER_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_animation:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_ANIMATION_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_demo:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_3D_DEMO_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_sp360:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_SP360_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;
                case R.id.btn_youtube_yt360:
                    mYoutubeCategory    = YouTubePlayListHelper.YOUTUBE_CH_YT360_PLAYLIST_ID;
                    menuOpen(GUI_YOUTUBE_PLAYLIST_CTRL);
                    break;

                // youtube playlist
                case R.id.btn_youtube_left:
                    youtubePreviousPage();
                    break;
                case R.id.btn_youtube_right:
                    youtubeNextPage();
                    break;
                case R.id.youtube_pages:
                    menuOpen(GUI_YOUTUBE_CATEGORY_CTRL);
                    break;
                case R.id.btn_youtube01:
                    youtubeSelectItem(0);
                    break;
                case R.id.btn_youtube02:
                    youtubeSelectItem(1);
                    break;
                case R.id.btn_youtube03:
                    youtubeSelectItem(2);
                    break;
                case R.id.btn_youtube04:
                    youtubeSelectItem(3);
                    break;
                case R.id.btn_youtube05:
                    youtubeSelectItem(4);
                    break;
                case R.id.btn_youtube06:
                    youtubeSelectItem(5);
                    break;
            }
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
                case R.id.btn_right:
                    msg = "Next Page";
                    break;
                case R.id.text_pages:
                    msg = "Home";
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
                    msg = "Storage";
                    break;
                case R.id.btn_youtube:
                    msg = "YouTube";
                    break;
                case R.id.btn_setting:
                    msg = "Setting";
                    break;
                case R.id.btn_fast_rewind:
                    msg = "Fast Rewind";
                    break;
                case R.id.btn_stop_pause_play:
                    if(((MainActivity) mContext).getPlayState()==NAMediaPlayer.PLAYER_STOP) {
                        msg = "stop";
                        ((ImageButton) findViewById(R.id.btn_stop_pause_play)).setImageResource(R.drawable.ic_stop_white_48dp);
                    } else if(((MainActivity) mContext).getPlayState()==NAMediaPlayer.PLAYER_PAUSE) {
                        msg = "play";
                        ((ImageButton) findViewById(R.id.btn_stop_pause_play)).setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    } else if(((MainActivity) mContext).getPlayState()==NAMediaPlayer.PLAYER_PLAY) {
                        msg = "pause";
                        ((ImageButton) findViewById(R.id.btn_stop_pause_play)).setImageResource(R.drawable.ic_pause_white_48dp);
                    }
                    break;
                case R.id.btn_fast_forward:
                    msg = "Fast Forward";
                    break;

                // setting
                case R.id.btn_sbs_3d:
                    msg = "Side By Side 3D Mode";
                    break;
                case R.id.btn_screen_up:
                    msg = "Screen Up";
                    break;
                case R.id.btn_panorama:
                    msg = "Panorama Screen";
                    break;
                case R.id.btn_screen_sizedown:
                    msg = "Screen Size Down";
                    break;
                case R.id.btn_2d:
                    msg = "2D Mode";
                    break;
                case R.id.btn_screen_sizeup:
                    msg = "Screen Size Up";
                    break;
                case R.id.btn_tb_3d:
                    msg = "Top And Bottom 3D Mode";
                    break;
                case R.id.btn_screen_down:
                    msg = "Screen Down";
                    break;
                case R.id.btn_dome:
                    msg = "Dome Screen";
                    break;

                // youtube category
                case R.id.btn_youtube_earth:
                    msg = "Earth";
                    break;
                case R.id.btn_youtube_space:
                    msg = "Space";
                    break;
                case R.id.btn_youtube_game:
                    msg = "Game";
                    break;
                case R.id.btn_youtube_mv:
                    msg = "Music Video";
                    break;
                case R.id.btn_youtube_movie:
                    msg = "Movie Trailer";
                    break;
                case R.id.btn_youtube_animation:
                    msg = "Animation Movie Trailer";
                    break;
                case R.id.btn_youtube_demo:
                    msg = "Demo";
                    break;
                case R.id.btn_youtube_sp360:
                    msg = "SP360";
                    break;
                case R.id.btn_youtube_yt360:
                    msg = "YouTube 360";
                    break;

                // youtube playlist
                case R.id.btn_youtube_left:
                    msg = "Previous Page";
                    break;
                case R.id.btn_youtube_right:
                    msg = "Next Page";
                    break;
                case R.id.youtube_pages:
                    msg = "Youtube Home";
                    break;
                case R.id.btn_youtube01:
                    msg = getYoutubeItemTitle(0);
                    break;
                case R.id.btn_youtube02:
                    msg = getYoutubeItemTitle(1);
                    break;
                case R.id.btn_youtube03:
                    msg = getYoutubeItemTitle(2);
                    break;
                case R.id.btn_youtube04:
                    msg = getYoutubeItemTitle(3);
                    break;
                case R.id.btn_youtube05:
                    msg = getYoutubeItemTitle(4);
                    break;
                case R.id.btn_youtube06:
                    msg = getYoutubeItemTitle(5);
                    break;
            }
        }

        ((NACardboardOverlayView)((Activity) mContext).findViewById(R.id.overlay)).show3DToast(msg);
    }

    public void updateLookAtBtn(int indexBtn) {
        mLookAtBtnIndex     = indexBtn;
        GridLayout views    = getActivateViews();

        if(views!=null) {
            if((mLookAtBtnIndex==-1)||(indexBtn>=views.getChildCount())) {
                mLookAtBtnResourceID    = -1;
                mLookAtBtnIndex         = -1;
            }
        } else {
            mLookAtBtnResourceID    = -1;
            mLookAtBtnIndex         = -1;
        }
    }

    private void updateGUI(boolean isReset) {
        if(isReset) {
            ((TextView)findViewById(R.id.text_pages)).setText("");
            mPlayTextTimeController.setText("");
            mPlayerProgress.setProgress(0);
        } else {
            ((TextView)findViewById(R.id.text_pages)).setText((mFolderPage+1)+"/"+mFolderPageMax);
            ((TextView)findViewById(R.id.youtube_pages)).setText((mYoutubePage+1)+"/"+mYoutubePageMax);
            if(((MainActivity) mContext).getPlayState()==NAMediaPlayer.PLAYER_PLAY) {
                int current     = ((MainActivity) mContext).getCurrentPosition();
                int duration    = ((MainActivity) mContext).getDuration();

                String Time     = String.format("%02d:%02d:%02d / %02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(current),
                        TimeUnit.MILLISECONDS.toMinutes(current) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(current)),
                        TimeUnit.MILLISECONDS.toSeconds(current) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current)),
                        TimeUnit.MILLISECONDS.toHours(duration),
                        TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));

                mPlayTextTimeController.setText(Time);

                mPlayerProgress.setMax(duration);
                mPlayerProgress.setProgress(current);
                mPlayerProgress.setSecondaryProgress(((MainActivity) mContext).getBufferingPercent()*duration/100);
            }
        }
    }

    private void updateTitle(String title) {
        mPlayTextTitleController.setText(title);
    }

    private void updateBtnColorA() {
        GridLayout views    = getActivateViews();

        if(views!=null) {
            for(int i = 0 ; i < views.getChildCount() ; i++) {
                if(mLookAtBtnIndex==i)  {
                    //((ImageButton)views.getChildAt(i)).setColorFilter(Color.rgb(255, 255, 255));
                    views.getChildAt(i).setAlpha(1.0f);
                    mLookAtBtnResourceID    = views.getChildAt(i).getId();
                }
                else {
                    //((ImageButton)views.getChildAt(i)).setColorFilter(Color.rgb(128, 128, 128));
                    views.getChildAt(i).setAlpha(0.6f);
                }
            }
        }
    }

    private GridLayout getActivateViews() {
        GridLayout views    = null;

        if(mPlayController.getVisibility()==View.VISIBLE)                   views   = mPlayBtnController;
        else if(mBrowserController.getVisibility()==View.VISIBLE)           views   = mBrowserController;
        else if(mSettingController.getVisibility()==View.VISIBLE)           views   = mSettingController;
        else if(mYoutubeCategoryController.getVisibility()==View.VISIBLE)   views   = mYoutubeCategoryController;
        else if(mYoutubePlaylistController.getVisibility()==View.VISIBLE)   views   = mYoutubePlaylistController;

        return views;
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
        updateGUI(false);
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