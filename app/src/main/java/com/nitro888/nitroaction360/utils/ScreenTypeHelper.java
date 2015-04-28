package com.nitro888.nitroaction360.utils;

import com.google.vrtoolkit.cardboard.Eye;
import com.nitro888.nitroaction360.R;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class ScreenTypeHelper {
    public static final int     SCREEN_RENDER_2D       = 0;
    public static final int     SCREEN_RENDER_3D_SBS   = 1;
    public static final int     SCREEN_RENDER_3D_TLBR  = 2;
    public static final int     SCREEN_RENDER_3D_TRBL  = 3;

    public final static int     SCREEN_SHAPE_CURVE      = R.raw.plane_sq;
    public final static int     SCREEN_SHAPE_DOME       = R.raw.dome;
    public final static int     SCREEN_SHAPE_SPHERE     = R.raw.sphere;
    public final static int     SCREEN_GUI              = R.raw.plane_sq_gui;   // GUI


    public static float[] getScreenOffset(int renderType, int eyeType){
        switch (renderType) {
            case SCREEN_RENDER_3D_SBS:
                return getSideBySideScreenOffset(eyeType);
            case SCREEN_RENDER_3D_TLBR:
                return getTLBRScreenOffset(eyeType);
            case SCREEN_RENDER_3D_TRBL:
                return getTRBLScreenOffset(eyeType);
        }

        return getFullScreenOffset(eyeType);
    }

    private static float[] getFullScreenOffset(int eyeType){
        final float[] offset = new float[4];
        offset[0]   = 1.0f; // width
        offset[1]   = 1.0f; // height
        offset[2]   = 0.0f; // offsetW
        offset[3]   = 0.0f; // offsetH
        return offset;
    }
    private static float[] getSideBySideScreenOffset(int eyeType){
        final float[] offset = new float[4];

        if(eyeType == Eye.Type.LEFT) {
            offset[0]   = 0.5f; // width
            offset[1]   = 1.0f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else if(eyeType == Eye.Type.RIGHT) {
            offset[0]   = 0.5f; // width
            offset[1]   = 1.0f; // height
            offset[2]   = 0.5f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else {
            return getFullScreenOffset(eyeType);
        }
        return offset;
    }
    private static float[] getTLBRScreenOffset(int eyeType){
        final float[] offset = new float[4];

        if(eyeType == Eye.Type.LEFT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else if(eyeType == Eye.Type.RIGHT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.5f; // offsetH
        } else {
            return getFullScreenOffset(eyeType);
        }
        return offset;
    }
    private static float[] getTRBLScreenOffset(int eyeType){
        final float[] offset = new float[4];

        if(eyeType == Eye.Type.LEFT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.5f; // offsetH
        } else if(eyeType == Eye.Type.RIGHT) {
            offset[0]   = 1.0f; // width
            offset[1]   = 0.5f; // height
            offset[2]   = 0.0f; // offsetW
            offset[3]   = 0.0f; // offsetH
        } else {
            return getFullScreenOffset(eyeType);
        }
        return offset;
    }

    public static float[] getScreenScaleRatioRotation(float fTilt, float fScale, int iWidth, int iHeight) {
        final float[]   transform = new float[4];

        transform[0]    =   fTilt;
        transform[1]    =   1.0f*fScale;
        transform[2]    =   (float)iHeight/(float)iWidth*fScale;
        transform[3]    =   1.0f;

        return transform;
    }
}
