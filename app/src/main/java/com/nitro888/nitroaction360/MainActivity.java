package com.nitro888.nitroaction360;

import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.nitro888.nitroaction360.Ads.NAGLAdView;
import com.nitro888.nitroaction360.cardboard.NACardboardOverlayView;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.nitro888.nitroaction360.nitroaction.NAGUIRelativeLayout;
import com.nitro888.nitroaction360.nitroaction.NAMediaPlayer;
import com.nitro888.nitroaction360.nitroaction.NAScreenGLRenderer;
import com.nitro888.nitroaction360.nitroaction.NAViewsToGLRenderer;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity {

    private NACardboardOverlayView      mNACardboardOverlayView;
    private CardboardView               mCardboardView;

    private NAGLAdView                  mNAGLAdView;
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

        // AdView
        mNAGLAdView             = (NAGLAdView) findViewById(R.id.AdViewFrame);
        mNAGLAdView.setViewToGLRenderer(mNAViewsToGLRenderer);

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

    public void setScreenShapeType(int screenID) {
        mNAScreenGLRenderer.setScreenShapeType(screenID);
    }
    public void setScreenRenderType(int renderType) {
        mNAScreenGLRenderer.setScreenRenderType(renderType);
    }
    public void setScreenTiltPosition(float degree) {
        mNAScreenGLRenderer.setScreenTiltPosition(degree);
    }
    public void setScreenScale(float scale) {
        mNAScreenGLRenderer.setScreenScale(scale);
    }

    @Override
    public void onCardboardTrigger() {
        mNAGUIRelativeLayout.onCardboardTrigger();
    }
}