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
    private static final int    SURFACE_TEXTURE_MAX             = 2;

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

        //testPlay();
    }
    /*
    // Play test
    private MediaPlayer mMediaPlayer            = null;

    private void testPlay() {
        if(mMediaPlayer==null) {
            mMediaPlayer    = new MediaPlayer();

            try {
                //mMediaPlayer.setDataSource("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
                //mMediaPlayer.setDataSource("http://r3---sn-i3b7rn7y.googlevideo.com/videoplayback?mm=31&pl=22&id=o-AB19EI2gRyr35MVVuK-omLCq2BmwaYh6MIXVfIyUe0tl&dur=291.596&ip=219.76.4.12&mt=1429044631&mv=m&ms=au&fexp=900720%2C907263%2C932627%2C932631%2C934954%2C9407115%2C9407432%2C9408023%2C9408041%2C9408195%2C9408347%2C9408469%2C9408707%2C946008%2C947233%2C947243%2C948124%2C948607%2C948703%2C951703%2C952612%2C952626%2C952637%2C957201%2C961404%2C961406&sver=3&initcwndbps=1166250&sparams=dur%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Cmime%2Cmm%2Cms%2Cmv%2Cnh%2Cpl%2Cratebypass%2Csource%2Cupn%2Cexpire&ipbits=0&expire=1429066342&mime=video%2Fmp4&ratebypass=yes&key=yt5&signature=A37F57940178EBC327CFBFF5AF06658970B180E6.48F6A6C5E750957E8244A73D440CAF69D0C9731E&nh=IgpwcjAzLmhrZzAxKgkxMjcuMC4wLjE&source=youtube&itag=22&upn=ReNITzQVZrc&title=AutoErotique+-+Asphyxiation+%28Official+Video%29");

                AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.big_buck_bunny);
                mMediaPlayer.setDataSource(
                        afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();


                ((MainActivity) mContext).setScreenRenderType(ScreenTypeHelper.SCREEN_RENDER_2D);
                mMediaPlayer.prepare();
                setTextureSize();
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
    private void setTextureSize() {
        setTextureWidth(
                NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,
                mMediaPlayer.getVideoWidth());
        setTextureHeight(
                NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,
                mMediaPlayer.getVideoHeight());
        createSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER);
        mMediaPlayer.setSurface(getSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER));
        //mMediaPlayer.setScreenOnWhilePlaying(true);
    }
    // Play test
    */

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