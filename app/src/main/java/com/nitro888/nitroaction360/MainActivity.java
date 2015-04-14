package com.nitro888.nitroaction360;

import android.os.Bundle;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.nitro888.nitroaction360.nitroaction.NAGUIRelativeLayout;
import com.nitro888.nitroaction360.nitroaction.NAMediaPlayer;
import com.nitro888.nitroaction360.nitroaction.NAScreenGLRenderer;
import com.nitro888.nitroaction360.nitroaction.NAViewsToGLRenderer;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity /*implements YouTubePlayer.OnInitializedListener*/ {

    private NACardboardOverlayView      mNACardboardOverlayView;
    private CardboardView               mCardboardView;

    private NAViewsToGLRenderer         mNAViewsToGLRenderer;
    private NAScreenGLRenderer          mNAScreenGLRenderer;
    private NAGUIRelativeLayout         mNAGUIRelativeLayout;
    private NAMediaPlayer               mNAMediaPlayer;

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


        // Cardboard
        mCardboardView          = (CardboardView) findViewById(R.id.cardboard_view);
        mCardboardView.setRenderer((CardboardView.StereoRenderer) mNAScreenGLRenderer);

        mNACardboardOverlayView = (NACardboardOverlayView) findViewById(R.id.overlay);
        mNACardboardOverlayView.show3DToast("NitroAction 360 Start");

        // youtube
        //onCreateYouTube();

        setCardboardView(mCardboardView);
    }

    public void onGUIButtonClick(View view) {
    }


    @Override
    public void onCardboardTrigger() {
        mNAGUIRelativeLayout.menuOpen(R.id.Setting);
    }
/*
    // for youtube test
    public static final String VIDEO_ID = "zKtAuflyc5w";

    private YouTubePlayer           youTubePlayer;
    private YouTubePlayerFragment   youTubePlayerFragment;

    private static final int        RQS_ErrorDialog = 1;

    private MyPlayerStateChangeListener myPlayerStateChangeListener;
    private MyPlaybackEventListener     myPlaybackEventListener;


    private void onCreateYouTube() {
        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);
        youTubePlayerFragment.initialize(YouTubeKey.API_KEY, this);
        //youTubePlayerFragment.getView();

        myPlayerStateChangeListener = new MyPlayerStateChangeListener();
        myPlaybackEventListener     = new MyPlaybackEventListener();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {

        if (result.isUserRecoverableError()) {
            result.getErrorDialog(this, RQS_ErrorDialog).show();
        } else {
            Toast.makeText(this,
                    "YouTubePlayer.onInitializationFailure(): " + result.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        youTubePlayer = player;

        Toast.makeText(getApplicationContext(),
                "YouTubePlayer.onInitializationSuccess()",
                Toast.LENGTH_LONG).show();

        youTubePlayer.setPlayerStateChangeListener(myPlayerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(myPlaybackEventListener);
        youTubePlayer.play();

        if (!wasRestored) {
            player.cueVideo(VIDEO_ID);
        }
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(com.google.android.youtube.player.YouTubePlayer.ErrorReason arg0) {
        }
        @Override
        public void onLoaded(String arg0) {
        }
        @Override
        public void onLoading() {
        }
        @Override
        public void onVideoEnded() {
        }
        @Override
        public void onVideoStarted() {
        }
    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {
        @Override
        public void onBuffering(boolean arg0) {
        }
        @Override
        public void onPaused() {
        }
        @Override
        public void onPlaying() {
        }
        @Override
        public void onSeekTo(int arg0) {
        }
        @Override
        public void onStopped() {
        }
    }
    */
}