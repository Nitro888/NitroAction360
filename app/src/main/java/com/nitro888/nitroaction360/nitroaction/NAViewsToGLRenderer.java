package com.nitro888.nitroaction360.nitroaction;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Surface;

import com.nitro888.nitroaction360.MainActivity;
import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.utils.ScreenTypeHelper;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by nitro888 on 15. 4. 13..
 */
//-------------------------------------------------------------------
// from ViewToGLRenderer
// https://github.com/ArtemBogush/AndroidViewToGLRendering
//-------------------------------------------------------------------
public class NAViewsToGLRenderer {
    private Context             mContext;
    private static final String TAG = NAViewsToGLRenderer.class.getSimpleName();

    private static final int    DEFAULT_TEXTURE_WIDTH           = 1024;
    private static final int    DEFAULT_TEXTURE_HEIGHT          = 1024;

    public static final int     SURFACE_TEXTURE_EMPTY           = 0;
    public static final int     SURFACE_TEXTURE_FOR_GUI         = 0;
    public static final int     SURFACE_TEXTURE_FOR_MEDIAPLAYER = 1;
    public static final int     SURFACE_TEXTURE_FOR_PHOTO       = 2;
    private static final int    SURFACE_TEXTURE_MAX             = 3;

    private static surfaceTexture[] mSurfaces                   = new surfaceTexture[SURFACE_TEXTURE_MAX];

    public NAViewsToGLRenderer(Context context) {
        mContext                = context;

        for(int i = 0 ; i < mSurfaces.length ; i ++ ) {
            mSurfaces[i]    = new surfaceTexture();
            mSurfaces[i].setActivate(true);
        }
    }

    public void setActivate (int typeID, boolean activate) {
        mSurfaces[typeID].setActivate(activate);
    }

    public boolean isActivate (int typeID) {
        return mSurfaces[typeID].isActivate();
    }

    public void onSurfaceChanged(){
        // GUI Size Setting
        setTextureWidth(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI,
                ((Activity) mContext).findViewById(R.id.GUI).getWidth());
        setTextureHeight(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI,
                ((Activity) mContext).findViewById(R.id.GUI).getHeight());
        // GUI Size Setting

        for(int i = 0 ; i < mSurfaces.length ; i ++ )   createSurface(i);

        Log.d(TAG,"onSurfaceChanged()");
    }

    public void createSurface (int typeID) {
        mSurfaces[typeID].createSurface();
    }

    public void onDrawFrame(){
        synchronized (this){
            for(int i = 0 ; i < mSurfaces.length ; i ++ )
                mSurfaces[i].onDrawFrame();
        }
    }

    public int getGLSurfaceTexture(int typeID){
        return mSurfaces[typeID].getGLSurfaceTexture();
    }

    public Canvas onDrawViewBegin(int typeID){
        return mSurfaces[typeID].onDrawViewBegin();
    }

    public void onDrawViewEnd(int typeID){
        mSurfaces[typeID].onDrawViewEnd();
    }

    public void setTextureWidth(int typeID, int textureWidth) {
        mSurfaces[typeID].setTextureWidth(textureWidth);
    }

    public void setTextureHeight(int typeID, int textureHeight) {
        mSurfaces[typeID].setTextureHeight(textureHeight);
    }

    public int getTextureWidth(int typeID) {
        return mSurfaces[typeID].getTextureWidth();
    }

    public int getTextureHeight(int typeID) {
        return mSurfaces[typeID].getTextureHeight();
    }

    public Surface getSurface(int typeID){
        return mSurfaces[typeID].getSurface();
    }

    private class surfaceTexture {
        private boolean         mIsActivate         = false;
        private SurfaceTexture  mSurfaceTexture;
        private Surface         mSurface;

        private int             mGlSurfaceTexture   = SURFACE_TEXTURE_EMPTY;
        private Canvas          mSurfaceCanvas;

        private int             mTextureWidth       = DEFAULT_TEXTURE_WIDTH;
        private int             mTextureHeight      = DEFAULT_TEXTURE_HEIGHT;

        public void setActivate (boolean activate) {
            mIsActivate = activate;
        }

        public boolean isActivate () {
            return mIsActivate;
        }

        public void onDrawFrame(){
            if(mGlSurfaceTexture==SURFACE_TEXTURE_EMPTY)
                return;

            synchronized (this){
                if(mIsActivate)
                    mSurfaceTexture.updateTexImage();
            }
        }

        public void createSurface() {
            if(mIsActivate) {
                //Log.d(TAG,"private class surfaceTexture onSurfaceChanged : "+mTextureWidth+","+mTextureHeight);
                releaseSurface();
                mGlSurfaceTexture = createTexture();
                if (mGlSurfaceTexture > SURFACE_TEXTURE_EMPTY){
                    //attach the texture to a surface.
                    //It's a clue class for rendering an android view to gl level
                    mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
                    mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);
                    mSurface        = new Surface(mSurfaceTexture);
                }
            }
        }

        private void releaseSurface(){
            if(mSurface != null){
                mSurface.release();
            }
            if(mSurfaceTexture != null){
                mSurfaceTexture.release();
            }
            mSurface = null;
            mSurfaceTexture = null;
        }

        private int createTexture(){
            mGlSurfaceTexture   = SURFACE_TEXTURE_EMPTY;
            int[] textures      = new int[1];

            // Generate the texture to where android view will be rendered
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glGenTextures(1, textures, 0);
            checkGLError("Texture generate");

            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
            checkGLError("Texture bind");

            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

            return textures[0];
        }

        public int getGLSurfaceTexture(){
            return mGlSurfaceTexture;
        }
        public Surface getSurface() {
            return mSurface;
        }

        public Canvas onDrawViewBegin(){
            mSurfaceCanvas = null;
            if(mIsActivate) {
                if (mSurface != null) {
                    try {
                        mSurfaceCanvas = mSurface.lockCanvas(null);
                        mSurfaceCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    }catch (Exception e){
                        Log.e(TAG, "error while rendering view to gl: " + e);
                    }
                }
            }
            return mSurfaceCanvas;
        }

        public void onDrawViewEnd(){
            if(mIsActivate) {
                if(mSurfaceCanvas != null) {
                    mSurface.unlockCanvasAndPost(mSurfaceCanvas);
                }
                mSurfaceCanvas = null;
            }
        }

        public int getTextureWidth() {
            return mTextureWidth;
        }
        public int getTextureHeight() {
            return mTextureHeight;
        }

        public void setTextureWidth(int textureWidth) {
            mTextureWidth   = textureWidth;
        }
        public void setTextureHeight(int textureHeight) {
            mTextureHeight  = textureHeight;
        }
    }

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }
}