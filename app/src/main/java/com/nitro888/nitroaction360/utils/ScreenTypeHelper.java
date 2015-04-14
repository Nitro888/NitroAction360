package com.nitro888.nitroaction360.utils;

import com.google.vrtoolkit.cardboard.Eye;
import com.nitro888.nitroaction360.R;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class ScreenTypeHelper {
    public static final int     SCREEN_2D       = 0;
    public static final int     SCREEN_3D_SBS   = 1;
    public static final int     SCREEN_3D_TLBR  = 2;
    public static final int     SCREEN_3D_TRBL  = 3;

    public final static int     SCREEN_CURVE    = R.raw.plane_sq;
    public final static int     SCREEN_DOME     = R.raw.dome;
    public final static int     SCREEN_GUI      = R.raw.plane_sq_gui;   // GUI


    public static float[] getScreenOffset(int renderType, int side){
        switch (renderType) {
            case SCREEN_3D_SBS:
                return getSideBySideScreenOffset(side);
            case SCREEN_3D_TLBR:
                return getTLBRScreenOffset(side);
            case SCREEN_3D_TRBL:
                return getTRBLScreenOffset(side);
        }

        return getFullScreenOffset(side);
    }

    private static float[] getFullScreenOffset(int side){
        final float[] offset = new float[4];
        offset[0]   = 1.0f; // width
        offset[1]   = 1.0f; // height
        offset[2]   = 0.0f; // offsetW
        offset[3]   = 0.0f; // offsetH
        return offset;
    }
    private static float[] getSideBySideScreenOffset(int side){
        final float[] offset = new float[4];

        if(side == Eye.Type.LEFT) {
            offset[0]   = 0.5f; // width
            offset[1]   = 1.0f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else if(side == Eye.Type.RIGHT) {
            offset[0]   = 0.5f; // width
            offset[1]   = 1.0f; // height
            offset[2]   = 0.5f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else {
            return getFullScreenOffset(side);
        }
        return offset;
    }
    private static float[] getTLBRScreenOffset(int side){
        final float[] offset = new float[4];

        if(side == Eye.Type.LEFT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else if(side == Eye.Type.RIGHT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.5f; // offsetH
        } else {
            return getFullScreenOffset(side);
        }
        return offset;
    }
    private static float[] getTRBLScreenOffset(int side){
        final float[] offset = new float[4];

        if(side == Eye.Type.LEFT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.5f; // offsetH
        } else if(side == Eye.Type.RIGHT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else {
            return getFullScreenOffset(side);
        }
        return offset;
    }

    public static float[] getScreenScaleRatioRotation(float fTilt, float fScale, int iWidth, int iHeight) {
        final float[]   transform = new float[4];

        if(fTilt>90.0f)    fTilt = 90.0f;
        if(fTilt<-90.0f)   fTilt =-90.0f;

        transform[0]    =   fTilt;
        transform[1]    =   1.0f*fScale;
        transform[2]    =   (float)iHeight/(float)iWidth*fScale;
        transform[3]    =   1.0f;

        return transform;
    }
}
