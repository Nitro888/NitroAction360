package com.nitro888.nitroaction360;

import android.os.Bundle;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.nitro888.nitroaction360.nitroaction.NAGUIRelativeLayout;
import com.nitro888.nitroaction360.nitroaction.NAMediaPlayer;
import com.nitro888.nitroaction360.nitroaction.NAScreenGLRenderer;
import com.nitro888.nitroaction360.nitroaction.NAViewsToGLRenderer;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity {

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

        setCardboardView(mCardboardView);
    }

    // for browser controller
    public void openMovie(String fileName) {
        mNAMediaPlayer.openMovieFile(fileName);
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
}