package com.nitro888.nitroaction360;

import android.os.Bundle;
import android.view.View;

import com.akoscz.youtube.PlaylistItem;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.nitro888.nitroaction360.nitroaction.NAGUIRelativeLayout;
import com.nitro888.nitroaction360.nitroaction.NAMediaPlayer;
import com.nitro888.nitroaction360.nitroaction.NAScreenGLRenderer;
import com.nitro888.nitroaction360.nitroaction.NAViewsToGLRenderer;
import com.nitro888.nitroaction360.utils.YouTubeDownloadHelper;
import com.nitro888.nitroaction360.utils.YouTubePlayListHelper;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity {
    private static final String         TAG                     = MainActivity.class.getSimpleName();

    private NACardboardOverlayView      mNACardboardOverlayView;
    private CardboardView               mCardboardView;

    private NAViewsToGLRenderer         mNAViewsToGLRenderer;
    private NAScreenGLRenderer          mNAScreenGLRenderer;
    private NAGUIRelativeLayout         mNAGUIRelativeLayout;
    private NAMediaPlayer               mNAMediaPlayer;
    private YouTubePlayListHelper       mYoutubePlayListHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        // ViewsToGLRenderer
        mNAViewsToGLRenderer    = new NAViewsToGLRenderer(this);

        // GUI
        mNAGUIRelativeLayout    = (NAGUIRelativeLayout) findViewById(R.id.GUI);
        mNAGUIRelativeLayout.setViewToGLRenderer(mNAViewsToGLRenderer);

        // MediaPlayer
        mNAMediaPlayer          = new NAMediaPlayer(this);
        mNAMediaPlayer.setViewToGLRenderer(mNAViewsToGLRenderer);

        // Screen
        mNAScreenGLRenderer     = new NAScreenGLRenderer(this);
        mNAScreenGLRenderer.setViewToGLRenderer(mNAViewsToGLRenderer);

        // Load Youtube
        /*
        ConnectivityManager cm =
        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork.isConnectedOrConnecting();
        boolean isWiFi      = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
         */
        //mYoutubePlayListHelper  = new YouTubePlayListHelper(this);

        // Cardboard
        mCardboardView          = (CardboardView) findViewById(R.id.cardboard_view);
        mCardboardView.setRenderer((CardboardView.StereoRenderer) mNAScreenGLRenderer);

        mNACardboardOverlayView = (NACardboardOverlayView) findViewById(R.id.overlay);
        //mNACardboardOverlayView.show3DToast("NitroAction 360 Start");

        setCardboardView(mCardboardView);

        //YouTubeDownloadHelper.main();
    }

    // for browser controller
    public void openMovieFile(String fileName) {
        mNAMediaPlayer.openMovieFile(fileName);
    }

    // for youtube
    public int getYoutubeCount(int category) {
        return mYoutubePlayListHelper.getCount(category);
    }

    public PlaylistItem getYoutubeItem(int category, int position) {
        return mYoutubePlayListHelper.getItem(category,position);
    }

    public void openMovieStream(String url) {
        mNAMediaPlayer.openMovieStream(url);
    }

    // for play controller
    public void fastRewind() {
        mNAMediaPlayer.fastRewind();
    }
    public void fastForward() {
        mNAMediaPlayer.fastForward();
    }
    public void playOrPause() {
        mNAMediaPlayer.playOrPause();
    }
    public int  getPlayState(){
        return mNAMediaPlayer.getPlayState();
    }
    public void skipPrevious() {
        mNAMediaPlayer.skipPrevious();
    }
    public void skipNext() {
        mNAMediaPlayer.skipNext();
    }
    public int getBufferingPercent() {
        return mNAMediaPlayer.getBufferingPercent();
    }
    public int getDuration() {
        return mNAMediaPlayer.getDuration();
    }
    public int getCurrentPosition() {
        return mNAMediaPlayer.getCurrentPosition();
    }


    // for setting controller
    public void setScreenShapeType(int screenID) {
        mNAScreenGLRenderer.setScreenShapeType(screenID);
    }
    public void setScreenRenderType(int renderType) {
        mNAScreenGLRenderer.setScreenRenderType(renderType);
    }
    public void setScreenTiltPosition(float step) {
        mNAScreenGLRenderer.setScreenTiltPosition(step);
    }
    public void setScreenScale(float step) {
        mNAScreenGLRenderer.setScreenScale(step);
    }

    @Override
    public void onCardboardTrigger() {
        mNAGUIRelativeLayout.onCardboardTrigger();
    }
    public void onGUIButtonClick(View view)
    {
        mNAGUIRelativeLayout.onGUIButtonClick(view.getId());
    }

    @Override
    public void onResume() {
        super.onResume();
        mNAMediaPlayer.resume();
    }
    public void onSurfaceChanged() {
        mNAMediaPlayer.onSurfaceChanged();
    }
    @Override
    public void onPause() {
        super.onPause();
        mNAMediaPlayer.pause();
    }
    @Override
    public void onStop() {
        super.onStop();
        mNAMediaPlayer.stop();
    }
}