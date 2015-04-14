package com.nitro888.nitroaction360.nitroaction;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by nitro888 on 15. 4. 13..
 */
//-------------------------------------------------------------------
// from ViewToGLRenderer
// https://github.com/ArtemBogush/AndroidViewToGLRendering
//-------------------------------------------------------------------
public class NAViewsToGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = NAViewsToGLRenderer.class.getSimpleName();

    private static final int    DEFAULT_TEXTURE_WIDTH           = 1024;
    private static final int    DEFAULT_TEXTURE_HEIGHT          = 1024;

    private MediaPlayer         mMediaPlayer                    = null;

    public static final int     SURFACE_TEXTURE_FOR_GUI         = 0;
    public static final int     SURFACE_TEXTURE_FOR_MEDIAPLAYER = 1;
    public static final int     SURFACE_TEXTURE_FOR_SCREEN      = 2;
    public static final int     SURFACE_TEXTURE_FOR_PHOTO       = 3;
    public static final int     SURFACE_TEXTURE_FOR_YOUTUBE     = 4;
    private static final int    SURFACE_TEXTURE_MAX             = 5;

    private static surfaceTexture[] mSurfaces                   = new surfaceTexture[SURFACE_TEXTURE_MAX];

    public NAViewsToGLRenderer() {
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

    @Override
    public void onDrawFrame(GL10 gl){
        onDrawFrame();
    }

    public void onDrawFrame(){
        synchronized (this){
            for(int i = 0 ; i < mSurfaces.length ; i ++ ) {
                mSurfaces[i].onDrawFrame();
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        onSurfaceChanged(width,height);
    }

    public void onSurfaceChanged(int width, int height){
        for(int i = 0 ; i < mSurfaces.length ; i ++ ) {
            mSurfaces[i].onSurfaceChanged(width,height);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_FASTEST);
        gl.glClearColor(0,0,0,0);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        Log.d(TAG, extensions);
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
        private boolean         mIsActivate = false;
        private SurfaceTexture  mSurfaceTexture;
        private Surface         mSurface;

        private int             mGlSurfaceTexture;
        private Canvas          mSurfaceCanvas;

        private int             mTextureWidth = DEFAULT_TEXTURE_WIDTH;
        private int             mTextureHeight = DEFAULT_TEXTURE_HEIGHT;

        public void setActivate (boolean activate) {
            mIsActivate = activate;
        }

        public boolean isActivate () {
            return mIsActivate;
        }

        public void onDrawFrame(){
            synchronized (this){
                if(mIsActivate)
                    mSurfaceTexture.updateTexImage();
            }
        }

        public void onSurfaceChanged(int width, int height){
            if(mIsActivate) {
                releaseSurface();
                mGlSurfaceTexture = createTexture();
                if (mGlSurfaceTexture > 0){
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
            int[] textures = new int[1];

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
            mTextureWidth = textureWidth;
        }
        public void setTextureHeight(int textureHeight) {
            mTextureHeight = textureHeight;
        }
    }

    public static void checkGLError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }
}