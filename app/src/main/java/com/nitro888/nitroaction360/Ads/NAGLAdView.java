package com.nitro888.nitroaction360.Ads;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.nitroaction.NAViewsToGLRenderer;

/**
 * Created by nitro888 on 15. 4. 15..
 */
public class NAGLAdView extends RelativeLayout {
    private static final String TAG                         = NAGLAdView.class.getSimpleName();
    private final Context mContext;

    private NAViewsToGLRenderer mNAViewsToGLRenderer        = null;
    private boolean             mFinishInit                 = false;

    private AdView              mAdView                     = null;
    private AdRequest           mADRequest;

    public NAGLAdView(Context context) {
        super(context);
        setWillNotDraw(false);
        mContext            = context;
    }

    public NAGLAdView (Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mContext            = context;
    }

    private void initLayout() {
        mAdView             = (AdView) findViewById(R.id.adView);
        mADRequest          = new AdRequest.Builder().build();
        mAdView.loadAd(mADRequest);
        mFinishInit         = true;
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

        //returns canvas attached to gl texture to draw on
        Canvas glAttachedCanvas = mNAViewsToGLRenderer.onDrawViewBegin(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_ADS);
        if(glAttachedCanvas != null) {
            //translate canvas to reflect view scrolling
            glAttachedCanvas.translate(-getScrollX(), -getScrollY());
            //draw the view to provided canvas
            super.dispatchDraw(glAttachedCanvas);
        }
        // notify the canvas is updated
        mNAViewsToGLRenderer.onDrawViewEnd(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_ADS);
    }
    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer = viewTOGLRenderer;
    }
}