package com.nitro888.nitroaction360;

import android.os.Bundle;
import com.nitro888.nitroaction360.cardboard.CardboardOverlayView;
import com.nitro888.nitroaction360.cardboard.NACardboardView;
import com.google.vrtoolkit.cardboard.CardboardActivity;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class MainActivity extends CardboardActivity {

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

        mNACardboardView.initRenderer(this, R.raw.dome, R.mipmap.test, R.raw.kodak_pixpro_sp360_01);

    }
}