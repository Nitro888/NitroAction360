package com.nitro888.nitroaction360.nitroaction;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
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

    private float[]                     mCamera             = new float[16];
    private float[]                     mView               = new float[16];
    private float[]                     mHeadView           = new float[16];

    private int                         mScreenShapeType    = ScreenTypeHelper.SCREEN_CURVE;
    private int                         mScreenRenderType   = ScreenTypeHelper.SCREEN_2D;
    private float                       mScreenTiltPosition = 0.0f;
    private float                       mScreenScale        = 1.0f;

    private int                         mPlayGLSurfaceTextureID = NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER;

    private NAScreenGLRendererCore      mCore;
    private NAViewsToGLRenderer         mNAViewsToGLRenderer    = null;

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
    public void setScreenTiltPosition(float degree) {
        mScreenTiltPosition     = degree;
    }
    public void setScreenScale(float scale) {
        mScreenScale            = scale;
    }

    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer    = viewTOGLRenderer;
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {
        mCore.onSurfaceCreated();
    }
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        headTransform.getHeadView(mHeadView, 0);
    }
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        mCore.onDrawEye(eye.getPerspective(Z_NEAR, Z_FAR),mView,
                ScreenTypeHelper.getScreenOffset(mScreenRenderType, eye.getType()),
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
        mCore.onSurfaceChanged(width,height);
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

        private final MeshBufferHelper  mModelBuffer1;          // vertex, texture, normal
        private final MeshBufferHelper  mModelBuffer2;          // vertex, texture, normal
        private final MeshBufferHelper  mModelBuffer3;          // vertex, texture, normal

        public NAScreenGLRendererCore(Context context) {
            mContext                = context;

            mModelBuffer1           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_CURVE);
            mModelBuffer2           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_DOME);
            mModelBuffer3           = WaveFrontObjHelper.loadObj(mContext, ScreenTypeHelper.SCREEN_GUI);
        }

        public void onSurfaceCreated(){
            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.1f);
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

        public void onDrawEye(float[] perspective, float[] view, float[] offset, int screenType) {
            if(mNAViewsToGLRenderer==null)  return;

            GLES20.glUseProgram(mProgramHandle);

            mMVPMatrixHandle            = GLES20.glGetUniformLocation(mProgramHandle,"u_MVPMatrix");
            mMVMatrixHandle             = GLES20.glGetUniformLocation(mProgramHandle,"u_MVMatrix");
            mTextureUniformHandle       = GLES20.glGetUniformLocation(mProgramHandle,"u_Texture");
            mScreenOffsetHandle         = GLES20.glGetUniformLocation(mProgramHandle,"u_Offset");

            mPositionHandle             = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
            mNormalHandle               = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
            mTextureCoordinateHandle    = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");


            float[] rationAndRotation   = ScreenTypeHelper.getScreenScaleRatioRotation(
                                            mScreenTiltPosition,mScreenScale,
                                            mNAViewsToGLRenderer.getTextureWidth(mPlayGLSurfaceTextureID),
                                            mNAViewsToGLRenderer.getTextureHeight(mPlayGLSurfaceTextureID));

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 0, 0, 0);
            Matrix.setRotateM(mModelMatrix,0,rationAndRotation[0],1.0f,0.0f,0.0f);
            Matrix.scaleM(mModelMatrix,0,rationAndRotation[1],rationAndRotation[2],rationAndRotation[3]);

            if(ScreenTypeHelper.SCREEN_CURVE==screenType)   renderMesh(mModelBuffer1,view,offset);
            else                                            renderMesh(mModelBuffer2,view,offset);

            rationAndRotation   = ScreenTypeHelper.getScreenScaleRatioRotation(
                                            mScreenTiltPosition,mScreenScale,
                                            mNAViewsToGLRenderer.getTextureWidth(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI),
                                            mNAViewsToGLRenderer.getTextureHeight(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI));

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, 0, 0, 0);
            Matrix.setRotateM(mModelMatrix,0,rationAndRotation[0],1.0f,0.0f,0.0f);
            Matrix.scaleM(mModelMatrix,0,rationAndRotation[1],rationAndRotation[2],rationAndRotation[3]);

            renderGUI(mModelBuffer3,view,offset);
        }

        private void renderMesh(MeshBufferHelper renderMesh, float[] view, float[] offset) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mNAViewsToGLRenderer.getGLSurfaceTexture(mPlayGLSurfaceTextureID));
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

            Matrix.multiplyMM(mMVMatrix, 0, view, 0, mModelMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
            //Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, mMVMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform4f(mScreenOffsetHandle,offset[0],offset[1],offset[2],offset[3]);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderMesh.getCount());
        }

        private void renderGUI(MeshBufferHelper renderMesh, float[] view, float[] offset) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mNAViewsToGLRenderer.getGLSurfaceTexture(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_GUI));
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

            Matrix.multiplyMM(mMVMatrix, 0, view, 0, mModelMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
            //Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, mMVMatrix, 0);
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform4f(mScreenOffsetHandle,offset[0],offset[1],offset[2],offset[3]);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, renderMesh.getCount());
        }
    }
}