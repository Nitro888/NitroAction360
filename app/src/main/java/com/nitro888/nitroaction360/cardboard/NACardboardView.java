package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class NACardboardView extends CardboardView {
    private static final String TAG                     = NACardboardView.class.getSimpleName();

    private Context             mContext;
    private MediaPlayer         mMediaPlayer    = null;
    private ScreenRenderer      mScreenRenderer;

    private int                 mScreenWidth    = 1;
    private int                 mScreenHeight   = 1;


    public NACardboardView (Context context) {
        super(context);
        mContext        = context;
    }
    public NACardboardView (Context context, AttributeSet attrs) {
        super(context,attrs);
        mContext        = context;
    }

    public void initRenderer (Context context, int meshId1, int meshId2) {
        mContext        = context;

        if(mMediaPlayer==null)
            mMediaPlayer    = new MediaPlayer();

        mScreenRenderer = new ScreenRenderer(context, meshId1, meshId2);
        mScreenRenderer.setMediPlayer(mMediaPlayer);

        setRenderer((StereoRenderer)mScreenRenderer);
    }

    /*
        Play movie
    */
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void playMovie(String fileName) {
        if(mMediaPlayer==null)  return;

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            FileDescriptor  fd              = fileInputStream.getFD();

            mMediaPlayer.setDataSource(fd);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void pauseMovie() {
        if(mMediaPlayer==null)  return;
        mMediaPlayer.pause();
    }

    public void skipForward() {

    }
    public void skipPrevious() {

    }

    public void fastForward() {
        //mMediaPlayer.

    }
    public void fastRewind() {

    }

    /*
    private void createMediaPlayer(int ResourceID) {
        if(mMediaPlayer==null)
            mMediaPlayer    = new MediaPlayer();

        try {
            //mMediaPlayer.setDataSource("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");

            AssetFileDescriptor afd = getResources().openRawResourceFd(ResourceID);
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    */

    /*
        Setting Screen
    */
    public void setScreenShapeType(int screenID) {
        mScreenRenderer.setScreenShapeType(screenID);
    }
    public void setScreenRenderType(int renderType) {
        mScreenRenderer.setScreenRenderType(renderType);
    }
    public void setScreenTiltPosition(float degree) {
        mScreenRenderer.setScreenTiltPosition(degree);
    }
    public void setScreenScale(float scale) {
        mScreenRenderer.setScreenScale(scale);
    }


    private class ScreenRenderer extends ScreenMeshGLRenderer implements NACardboardView.StereoRenderer {
        private float[]                     mCamera     = new float[16];
        private float[]                     mView       = new float[16];
        private float[]                     mHeadView   = new float[16];
        private int                         mScreenShapeType;
        private int                         mScreenRenderType;
        private float                       mScreenTiltPosition;
        private float                       mScreenScale;

        public ScreenRenderer(Context context, int meshId1, int meshId2) {
            super(context,meshId1,meshId2);
            mScreenShapeType        = meshId1;
            mScreenRenderType       = ScreenType.SCREEN_2D;
            mScreenTiltPosition     = 0.0f;
            mScreenScale            = 1.0f;
        }

        public void setScreenShapeType(int screenID) {
            mScreenShapeType        = screenID;
        }
        public void setScreenRenderType(int renderType) {
            mScreenRenderType       = renderType;
        }
        public void setScreenTiltPosition(float degree) {
            mScreenTiltPosition     = degree;
        }
        public void setScreenScale(float scale) {
            mScreenScale = scale;
        }

        @Override
        public void onSurfaceCreated(EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            super.onSurfaceCreated();
            checkGLError("onSurfaceCreated");
        }
        @Override
        public void onNewFrame(HeadTransform headTransform) {
            Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            headTransform.getHeadView(mHeadView, 0);
            checkGLError("onReadyToDraw");
        }
        @Override
        public void onDrawEye(Eye eye) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            checkGLError("colorParam");
            Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

            if(mMediaPlayer!=null) {
                if(mMediaPlayer.isPlaying()) {
                    mScreenWidth    = mMediaPlayer.getVideoWidth();
                    mScreenHeight   = mMediaPlayer.getVideoHeight();
                }
            }

            super.onDrawEye(eye.getPerspective(Z_NEAR, Z_FAR),mView,
                    ScreenType.getScreenOffset(mScreenRenderType,eye.getType()),
                    ScreenType.getScreenScaleRatioRotation(mScreenTiltPosition,mScreenScale,mScreenWidth,mScreenHeight),
                    mScreenShapeType);
        }
        @Override
        public void onFinishFrame(Viewport viewport) {
        }
        @Override
        public void onRendererShutdown() {
        }
        @Override
        public void onSurfaceChanged(int width, int height){
            super.onSurfaceChanged(width,height);
        }
    }
}
