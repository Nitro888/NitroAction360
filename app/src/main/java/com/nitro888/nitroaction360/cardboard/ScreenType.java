package com.nitro888.nitroaction360.cardboard;

import com.google.vrtoolkit.cardboard.Eye;
/**
 * Created by nitro888 on 15. 4. 8..
 */
public class ScreenType {
    public static float[] getFullScreenOffset(int side){
        final float[] offset = new float[4];
        offset[0]   = 1.0f; // width
        offset[1]   = 1.0f; // height
        offset[2]   = 0.0f; // offsetW
        offset[3]   = 0.0f; // offsetH
        return offset;
    }
    public static float[] getSideBySideScreenOffset(int side){
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
    public static float[] getTLBRScreenOffset(int side){
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
    public static float[] getTRBLScreenOffset(int side){
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

    public static float[] getScreenScaleRatioRotation(int iTilt, float fScale, int iWidth, int iHeight) {
        // iTilt 0 = top, 1 = 35, 2 = front, 3 = -45, 4 = down
        final float[]   transform = new float[4];

        switch (iTilt) {
            case 0: transform[0]    = 90.0f;    break;
            case 1: transform[0]    = 35.0f;    break;
            case 3: transform[0]    = -45.0f;   break;
            case 4: transform[0]    = -90.0f;   break;
            case 2:
            default:    transform[3]= 0.0f;     break;
        }

        transform[1]    =   1.0f*fScale;
        transform[2]    =   (float)iHeight/(float)iWidth*fScale;
        transform[3]    =   1.0f;

        return transform;
    }
}
