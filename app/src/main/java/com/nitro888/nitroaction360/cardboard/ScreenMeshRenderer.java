package com.nitro888.nitroaction360.cardboard;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nitro888.nitroaction360.R;
import com.nitro888.nitroaction360.utils.RawResourceReader;
import com.nitro888.nitroaction360.utils.ShaderHelper;
import com.nitro888.nitroaction360.utils.WaveFrontObjHelper;

import java.nio.FloatBuffer;

/**
 * Created by nitro888 on 15. 4. 5..
 */
public class ScreenMeshRenderer extends ViewToGLRenderer {
    private Context mContext;
    private static final String TAG                     = ScreenMeshRenderer.class.getSimpleName();

    private float[]             mModelMatrix            = new float[16];
    private float[]             mMVPMatrix              = new float[16];
    private float[]             mMVMatrix               = new float[16];

    private float[]             mProjectionMatrix       = new float[16];

    private int                 mMVPMatrixHandle;
    private int                 mMVMatrixHandle;
    private int                 mTextureUniformHandle;
    private int                 mPositionHandle;
    private int                 mNormalHandle;
    private int                 mTextureCoordinateHandle;
    private int                 mProgramHandle;

    private final FloatBuffer[] mModelBuffer;

    public ScreenMeshRenderer(Context context,int meshId) {
        mContext                = context;
        mModelBuffer            = WaveFrontObjHelper.loadObj(mContext,meshId);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0, 0); // Obj appears below user.
    }

    public void onSurfaceCreated(){
        final String sVertex     = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.unlit_vertex);
        final String sFragment   = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.fragment);

        final int vertexShaderHandle    = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, sVertex);
        final int fragmentShaderHandle  = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, sFragment);

        mProgramHandle = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
                        new String[]{"a_Position", "a_Normal", "a_TexCoordinate"});
    }

    public void onSurfaceChanged(int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

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

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getGLSurfaceTextureBlenderTarget(), getGLSurfaceTexture());
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mModelBuffer[0].position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mModelBuffer[0]);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        mModelBuffer[1].position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mModelBuffer[1]);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        mModelBuffer[2].position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mModelBuffer[2]);
        GLES20.glEnableVertexAttribArray(mNormalHandle);

        Matrix.multiplyMM(mMVMatrix, 0, view, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        /*
        Matrix.multiplyMM(mMVMatrix, 0, view, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, perspective, 0, mMVMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        */

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6); // check!
        GLES20.glFinish();

        checkGLError("drawing obj");
    }
}