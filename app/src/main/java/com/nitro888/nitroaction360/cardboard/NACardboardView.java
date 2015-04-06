package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class NACardboardView extends CardboardView {
    private static final String TAG                     = NACardboardView.class.getSimpleName();

    private Context             mContext;
    private MediaPlayer         mMediaPlayer    = null;
    private ScreenRenderer      mScreenRenderer;

    public NACardboardView (Context context) {
        super(context);
        mContext        = context;
    }
    public NACardboardView (Context context, AttributeSet attrs) {
        super(context,attrs);
        mContext        = context;
    }

    public void initRenderer (Context context, int meshId, int textureID, int movieID) {
        mContext        = context;

        createMediaPlayer(movieID);

        mScreenRenderer = new ScreenRenderer(context,meshId);
        mScreenRenderer.setMediPlayer(mMediaPlayer);

        setRenderer((StereoRenderer)mScreenRenderer);
    }

    private void createMediaPlayer(int ResourceID) {
        mMediaPlayer    = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
/*
            AssetFileDescriptor afd = getResources().openRawResourceFd(ResourceID);
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
*/
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private class ScreenRenderer extends CubeMeshGLRenderer implements NACardboardView.StereoRenderer {

        private static final float          Z_NEAR      = 5.0f;
        private static final float          Z_FAR       = 500.0f;
        private static final float          CAMERA_Z    = 0.01f;

        private float[]                     mCamera     = new float[16];
        private float[]                     mView       = new float[16];
        private float[]                     mHeadView   = new float[16];

        public ScreenRenderer(Context context,int meshId) {
            super(context,meshId);
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
            float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
            super.onDrawEye(perspective,mView);
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
