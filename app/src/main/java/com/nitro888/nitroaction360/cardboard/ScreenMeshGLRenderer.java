package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.utils.RawResourceReader;
import com.nitro888.nitroaction360.utils.ShaderHelper;
import com.nitro888.nitroaction360.utils.WaveFrontObjHelper;
import com.nitro888.nitroaction360.utils.MeshBufferHelper;

/**
 * Created by nitro888 on 15. 4. 6..
 */
public class ScreenMeshGLRenderer extends ViewToGLRenderer {
    private Context mContext;
    private static final String TAG                     = ScreenMeshGLRenderer.class.getSimpleName();

    protected static final float    Z_NEAR              = 1.0f;
    protected static final float    Z_FAR               = 500.0f;
    protected static final float    CAMERA_Z            = 0.01f;

    private float[]                 mModelMatrix        = new float[16];
    private float[]                 mMVPMatrix          = new float[16];
    private float[]                 mMVMatrix           = new float[16];

    private float[]                 mProjectionMatrix   = new float[16];

    private int                     mMVPMatrixHandle;
    private int                     mMVMatrixHandle;
    private int                     mTextureUniformHandle;
    private int                     mPositionHandle;
    private int                     mNormalHandle;
    private int                     mTextureCoordinateHandle;
    private int                     mProgramHandle;

    private final MeshBufferHelper  mModelBuffer;       // vertex, texture, normal

    public ScreenMeshGLRenderer(Context context,int meshId) {
        mContext                = context;
        mModelBuffer            = WaveFrontObjHelper.loadObj(mContext, meshId);
    }

    public void onSurfaceCreated(){
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
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
        final float left    = -ratio;
        final float right   = ratio;
        final float bottom  = -1.0f;
        final float top     = 1.0f;
        final float near    = Z_NEAR;
        final float far     = Z_FAR;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
        super.onSurfaceChanged(width, height);
    }

    public void onDrawEye(float[] perspective, float[] view) {
        super.onDrawFrame();

        GLES20.glUseProgram(mProgramHandle);

        mMVPMatrixHandle        = GLES20.glGetUniformLocation(mProgramHandle,"u_MVPMatrix");
        mMVMatrixHandle         = GLES20.glGetUniformLocation(mProgramHandle,"u_MVMatrix");
        mTextureUniformHandle   = GLES20.glGetUniformLocation(mProgramHandle,"u_Texture");
        mPositionHandle         = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mNormalHandle           = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle= GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        GLES20.glBindTexture(getGLSurfaceTextureBlenderTarget(), getGLSurfaceTexture());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0, 0);

        mModelBuffer.getBuffer()[0].position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mModelBuffer.getBuffer()[0]);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mModelBuffer.getBuffer()[1].position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mModelBuffer.getBuffer()[1]);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        mModelBuffer.getBuffer()[2].position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mModelBuffer.getBuffer()[2]);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        Matrix.multiplyMM(mMVMatrix, 0, view, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        //Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mModelBuffer.getCount());
    }
}