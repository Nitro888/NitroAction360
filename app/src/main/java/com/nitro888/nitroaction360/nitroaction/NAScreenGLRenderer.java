package com.nitro888.nitroaction360.nitroaction;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.nitro888.nitroaction360.MainActivity;
import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.utils.MeshBufferHelper;
import com.nitro888.nitroaction360.utils.RawResourceReader;
import com.nitro888.nitroaction360.utils.ScreenTypeHelper;
import com.nitro888.nitroaction360.utils.ShaderHelper;
import com.nitro888.nitroaction360.utils.WaveFrontObjHelper;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class NAScreenGLRenderer implements CardboardView.StereoRenderer {
    private Context                     mContext;
    private static final String         TAG                 = NAScreenGLRenderer.class.getSimpleName();

    private static final float          Z_NEAR              = 1.0f;
    private static final float          Z_FAR               = 500.0f;
    private static final float          CAMERA_Z            = 0.01f;

    private float                       mStereoMultiplier   = 1.0f;
    private float                       mEyeGap             = 0.06f;
    private float[]                     mCameraR            = new float[16];
    private float[]                     mCameraL            = new float[16];
    private float[]                     mView               = new float[16];
    private float[]                     mHeadView           = new float[16];

    private int                         mScreenShapeType    = ScreenTypeHelper.SCREEN_SHAPE_CURVE;
    private int                         mScreenRenderType   = ScreenTypeHelper.SCREEN_RENDER_2D;
    private float                       mScreenTiltPosition = 0.0f;
    private float                       mScreenScale        = 1.0f;

    private int                         mPlayGLSurfaceTextureID = NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER;

    private NAScreenGLRendererCore      mCore;
    private NAViewsToGLRenderer         mNAViewsToGLRenderer= null;
    private int                         mBtnIndex           = -1;

    private static final float          STEP_TILT           = 5.0f;
    private static final float          STEP_SCALE          = 0.1f;


    public NAScreenGLRenderer(Context context) {
        mContext                = context;
        mCore                   = new NAScreenGLRendererCore(context);
    }
    public void setScreenShapeType(int screenID) {
        mScreenShapeType        = screenID;
    }
    public void setScreenRenderType(int renderType) {
        mScreenRenderType       = renderType;
    }
    public void setScreenTiltPosition(float step) {
        mScreenTiltPosition     +=STEP_TILT*step;

        if(step==0.0f)                  mScreenTiltPosition = 0.0f;
        if(mScreenTiltPosition>90.0f)   mScreenTiltPosition = 90.0f;
        if(mScreenTiltPosition<-90.0f)  mScreenTiltPosition =-90.0f;
    }
    public void setScreenScale(float step) {
        if(ScreenTypeHelper.SCREEN_SHAPE_DOME==mScreenShapeType) return;
        if(step==0.0f)  mScreenScale = 1.0f;
        else            mScreenScale +=STEP_SCALE*step;
    }

    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer    = viewTOGLRenderer;
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        //mNAViewsToGLRenderer.onSurfaceCreated(null,config);
        mCore.onSurfaceCreated();
    }
    @Override
    public void onSurfaceChanged(int width, int height){
        if(mNAViewsToGLRenderer!=null)
            mNAViewsToGLRenderer.onSurfaceChanged();
        mCore.onSurfaceChanged(width,height);
        ((MainActivity) mContext).onSurfaceChanged();
    }
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        float fGap  = mStereoMultiplier * mEyeGap;
        Matrix.setLookAtM(mCameraL, 0, -fGap, 0.0f, CAMERA_Z, -fGap, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.setLookAtM(mCameraR, 0, fGap, 0.0f, CAMERA_Z, fGap, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(mHeadView, 0);
    }
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(mNAViewsToGLRenderer!=null)
            mNAViewsToGLRenderer.onDrawFrame();

        if(eye.getType()==Eye.Type.LEFT) {
            Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCameraL, 0);
            mCore.onDrawEye(eye.getPerspective(Z_NEAR, Z_FAR), mView, eye.getType());
        } else {
            Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCameraR, 0);
            mCore.onDrawEye(eye.getPerspective(Z_NEAR, Z_FAR), mView, eye.getType());
        }
    }
    @Override
    public void onFinishFrame(Viewport viewport) {
    }
    @Override
    public void onRendererShutdown() {
    }

    private class NAScreenGLRendererCore {
        private Context mContext;

        private float[]                 mModelMatrix        = new float[16];
        private float[]                 mMVPMatrix          = new float[16];
        private float[]                 mMVMatrix           = new float[16];

        private float[]                 mProjectionMatrix   = new float[16];

        private int                     mProgramHandle;
        private int                     mMVPMatrixHandle;
        private int                     mMVMatrixHandle;
        private int                     mTextureUniformHandle;
        private int                     mPositionHandle;
        private int                     mNormalHandle;
        private int                     mTextureCoordinateHandle;
        private int                     mScreenOffsetHandle;

        private final MeshBufferHelper  mModelBuffer0;          // vertex, texture, normal
        private final MeshBufferHelper  mModelBuffer1;          // vertex, texture, normal
        private final MeshBufferHelper  mModelBuffer2;          // vertex, texture, normal
        private final MeshBufferHelper  mModelBuffer3;          // vertex, texture, normal

        public NAScreenGLRendererCore(Context context) {
            mContext                = context;

            mModelBuffer0           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_SHAPE_SPHERE);
            mModelBuffer1           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_SHAPE_CURVE);
            mModelBuffer2           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_SHAPE_DOME);
            mModelBuffer3           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_GUI);

            initBtnPosition();
        }

        public void onSurfaceCreated(){
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            final String sVertex     = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.unlit_vertex);
            final String sFragment   = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.gl_oes_fragment);

            final int vertexShaderHandle    = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, sVertex);
            final int fragmentShaderHandle  = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, sFragment);

            mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                    new String[]{"a_Position", "a_Normal", "a_TexCoordinate"});
        }

        public void onSurfaceChanged(int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            final float ratio   = (float) width / height;
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, Z_NEAR, Z_FAR);
        }

        public void onDrawEye(float[] perspective, float[] view, int eyeType) {
            if(mNAViewsToGLRenderer==null)  return;

            float[] perspectiveView     = view; // perspective or view
            float[] offset              = ScreenTypeHelper.getScreenOffset(mScreenRenderType, eyeType);

            GLES20.glUseProgram(mProgramHandle);

            mMVPMatrixHandle            = GLES20.glGetUniformLocation(mProgramHandle,"u_MVPMatrix");
            mMVMatrixHandle             = GLES20.glGetUniformLocation(mProgramHandle,"u_MVMatrix");
            mTextureUniformHandle       = GLES20.glGetUniformLocation(mProgramHandle,"u_Texture");
            mScreenOffsetHandle         = GLES20.glGetUniformLocation(mProgramHandle,"u_Offset");

            mPositionHandle             = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
            mNormalHandle               = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
            mTextureCoordinateHandle    = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

            // SCREEN
            float[] rationAndRotation   = ScreenTypeHelper.getScreenScaleRatioRotation(
                                            mScreenTiltPosition,mScreenScale,
                                            mNAViewsToGLRenderer.getTextureWidth(mPlayGLSurfaceTextureID),
                                            mNAViewsToGLRenderer.getTextureHeight(mPlayGLSurfaceTextureID));

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 0, 0, 0);
            Matrix.setRotateM(mModelMatrix,0,rationAndRotation[0],1.0f,0.0f,0.0f);

            switch (mScreenShapeType) {
                case ScreenTypeHelper.SCREEN_SHAPE_SPHERE:
                    Matrix.scaleM(mModelMatrix,0, 1.0f,1.0f, 1.0f);
                    renderMesh(mModelBuffer0,perspectiveView,offset, mNAViewsToGLRenderer.getGLSurfaceTexture(mPlayGLSurfaceTextureID));
                    break;
                case ScreenTypeHelper.SCREEN_SHAPE_DOME:
                    Matrix.scaleM(mModelMatrix,0, 1.0f,1.0f, 1.0f);
                    renderMesh(mModelBuffer2,perspectiveView,offset, mNAViewsToGLRenderer.getGLSurfaceTexture(mPlayGLSurfaceTextureID));
                    break;
                default:
                    Matrix.scaleM(mModelMatrix,0,rationAndRotation[1],rationAndRotation[2],rationAndRotation[3]);
                    renderMesh(mModelBuffer1,perspectiveView,offset, mNAViewsToGLRenderer.getGLSurfaceTexture(mPlayGLSurfaceTextureID));
                    break;
            }

            // GUI
            rationAndRotation   = ScreenTypeHelper.getScreenScaleRatioRotation(
                                            mScreenTiltPosition,1.0f,
                                            mNAViewsToGLRenderer.getTextureWidth(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI),
                                            mNAViewsToGLRenderer.getTextureHeight(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI));

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 0, 0, 0);
            //Matrix.setRotateM(mModelMatrix,0,rationAndRotation[0],1.0f,0.0f,0.0f);
            Matrix.scaleM(mModelMatrix,0,rationAndRotation[1],rationAndRotation[2],rationAndRotation[3]);

            offset              = ScreenTypeHelper.getScreenOffset(ScreenTypeHelper.SCREEN_RENDER_2D, eyeType);
            renderMesh(mModelBuffer3,perspectiveView,offset,mNAViewsToGLRenderer.getGLSurfaceTexture(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI));

            mBtnIndex = checkLookingAtObject(perspectiveView,rationAndRotation);

            ((NAGUIRelativeLayout)((Activity) mContext).findViewById(R.id.GUI)).updateLookAtBtn(mBtnIndex);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ((Activity) mContext).runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            ((Activity) mContext).findViewById(R.id.GUI).invalidate();
                        }
                    });
                }
            }).start();
        }

        private void renderMesh(MeshBufferHelper renderMesh, float[] perspectiveView, float[] offset, int texture) {
            if(mNAViewsToGLRenderer.getGLSurfaceTexture(mPlayGLSurfaceTextureID)==NAViewsToGLRenderer.SURFACE_TEXTURE_EMPTY)
                return;

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(mTextureUniformHandle, 0);

            renderMesh.getBuffer()[0].position(0);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, renderMesh.getBuffer()[0]);
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            renderMesh.getBuffer()[1].position(0);
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, renderMesh.getBuffer()[1]);
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

            renderMesh.getBuffer()[2].position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, renderMesh.getBuffer()[2]);
            GLES20.glEnableVertexAttribArray(mNormalHandle);

            Matrix.multiplyMM(mMVMatrix, 0, perspectiveView, 0, mModelMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
            //Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, mMVMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform4f(mScreenOffsetHandle,offset[0],offset[1],offset[2],offset[3]);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderMesh.getCount());
        }

        /*
            Check GUI Button
        */
        private static final float  LIMIT_YAW           = 0.15f;
        private static final float  LIMIT_PITCH         = 0.15f;
        private final float         mBtnPosition[][]     = new float[9][3];

        private void initBtnPosition() {
            for(int i=0 ; i < mBtnPosition.length ; i++) {
                int x   = i%3;
                int y   = i/3;

                mBtnPosition[i][0]  = x==0?-4:x==1?0:4;
                mBtnPosition[i][1]  = y==0?4:y==1?0:-4;
                mBtnPosition[i][2]  = -10.0f;
            }
        }

        private int checkLookingAtObject(float[] perspectiveView, float[] ratioAndRotation) {
            float[] initVec             = { 0, 0, 0, 1.0f };
            float[] objPositionVec      = new float[4];
            int     btnIndex            = -1;

            for(int i=0 ; i < mBtnPosition.length ; i++) {
                Matrix.setIdentityM(mModelMatrix, 0);
                Matrix.translateM(mModelMatrix, 0, mBtnPosition[i][0], mBtnPosition[i][1], mBtnPosition[i][2]);
                //Matrix.setRotateM(mModelMatrix,0,ratioAndRotation[0],1.0f,0.0f,0.0f);
                Matrix.scaleM(mModelMatrix,0,ratioAndRotation[1],ratioAndRotation[2],ratioAndRotation[3]);
                Matrix.multiplyMM(mMVMatrix, 0, perspectiveView, 0, mModelMatrix, 0);
                Matrix.multiplyMV(objPositionVec, 0, mMVMatrix, 0, initVec, 0);

                float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
                float yaw   = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

                btnIndex = (Math.abs(pitch) < LIMIT_PITCH && Math.abs(yaw) < LIMIT_YAW)?i:-1;
                if(btnIndex>-1) return btnIndex;
            }
            return btnIndex;
        }
    }
}