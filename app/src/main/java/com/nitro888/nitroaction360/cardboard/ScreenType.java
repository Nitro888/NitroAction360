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
}
