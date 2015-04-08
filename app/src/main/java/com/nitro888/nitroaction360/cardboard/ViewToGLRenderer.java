package com.nitro888.nitroaction360.cardboard;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class ViewToGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = ViewToGLRenderer.class.getSimpleName();

    private static final int    DEFAULT_TEXTURE_WIDTH   = 2048;
    private static final int    DEFAULT_TEXTURE_HEIGHT  = 2048;

    private SurfaceTexture      mSurfaceTexture;
    private Surface             mSurface;

    private int                 mGlSurfaceTexture;
    private int                 mTextureBlenderTarget   = GLES20.GL_TEXTURE_2D;

    private int                 mTextureWidth           = DEFAULT_TEXTURE_WIDTH;
    private int                 mTextureHeight          = DEFAULT_TEXTURE_HEIGHT;

    //-------------------------------------------------------------------
    // for MediaPlayer
    //-------------------------------------------------------------------
    private MediaPlayer mMediaPlayer    = null;

    public void setMediPlayer(MediaPlayer mp){
        mTextureBlenderTarget   = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        mMediaPlayer            = mp;
    }
    private void setMediPlayerToSurface() {
        if(mSurface==null) return;
        mMediaPlayer.setSurface(mSurface);
        mMediaPlayer.setScreenOnWhilePlaying(true);

        if(!mMediaPlayer.isPlaying()){
            try {
                mMediaPlayer.prepare();
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }
            mTextureWidth   = mMediaPlayer.getVideoWidth();
            mTextureHeight  = mMediaPlayer.getVideoHeight();
            mMediaPlayer.start();
        }
    };

    //-------------------------------------------------------------------
    // from ViewToGLRenderer
    // https://github.com/ArtemBogush/AndroidViewToGLRendering
    //-------------------------------------------------------------------
    @Override
    public void onDrawFrame(GL10 gl){}
    public void onDrawFrame(){
        synchronized (this){
            // update texture
            mSurfaceTexture.updateTexImage();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){
        releaseSurface();
        mGlSurfaceTexture = createTexture();
        if (mGlSurfaceTexture > 0){
            if(mMediaPlayer!=null) {
                mTextureWidth   = mMediaPlayer.getVideoWidth();
                mTextureHeight  = mMediaPlayer.getVideoHeight();
            }
            mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
            mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);
            mSurface        = new Surface(mSurfaceTexture);
            setMediPlayerToSurface();
        }
    }

    public void onSurfaceChanged(int width, int height){
        releaseSurface();
        mGlSurfaceTexture = createTexture();
        if (mGlSurfaceTexture > 0){
            if(mMediaPlayer!=null) {
                mTextureWidth   = mMediaPlayer.getVideoWidth();
                mTextureHeight  = mMediaPlayer.getVideoHeight();
            }
            mSurfaceTexture = new SurfaceTexture(mGlSurfaceTexture);
            mSurfaceTexture.setDefaultBufferSize(mTextureWidth, mTextureHeight);
            mSurface        = new Surface(mSurfaceTexture);
            setMediPlayerToSurface();
        }
    }

    public void releaseSurface(){
        if(mSurface != null){
            mSurface.release();
        }
        if(mSurfaceTexture != null){
            mSurfaceTexture.release();
        }
        mSurface = null;
        mSurfaceTexture = null;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config){
        final String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        Log.d(TAG, extensions);
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
    public int getGLSurfaceTextureBlenderTarget(){
        return mTextureBlenderTarget;
    }

    public static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}