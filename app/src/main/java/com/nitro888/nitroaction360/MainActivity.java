package com.nitro888.nitroaction360;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.nitro888.nitroaction360.cardboard.CardboardOverlayView;
import com.nitro888.nitroaction360.cardboard.NACardboardView;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.vrtoolkit.cardboard.CardboardActivity;
/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity implements YouTubePlayer.OnInitializedListener {

    private CardboardOverlayView    mOverlayView;
    private NACardboardView         mNACardboardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);

        mNACardboardView = (NACardboardView) findViewById(R.id.cardboard_view);
        setCardboardView(mNACardboardView);

        mOverlayView    = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlayView.show3DToast("NitroAction 360 Start");

        mNACardboardView.initRenderer(this, R.raw.plane_sq, R.mipmap.test, R.raw.side_by_side);

        onCreateYouTube();
    }



    // for youtube test
    public static final String VIDEO_ID = "zKtAuflyc5w";

    private YouTubePlayer           youTubePlayer;
    private YouTubePlayerFragment   youTubePlayerFragment;

    private static final int RQS_ErrorDialog = 1;

    private MyPlayerStateChangeListener myPlayerStateChangeListener;
    private MyPlaybackEventListener     myPlaybackEventListener;


    private void onCreateYouTube() {
        /*
        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);
        youTubePlayerFragment.initialize(YouTubeKey.API_KEY, this);
        //youTubePlayerFragment.getView();

        myPlayerStateChangeListener = new MyPlayerStateChangeListener();
        myPlaybackEventListener     = new MyPlaybackEventListener();
        */
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
}