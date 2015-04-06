package com.nitro888.nitroaction360.utils;

import java.nio.FloatBuffer;

/**
 * Created by nitro888 on 15. 4. 6..
 */
public class MeshBufferHelper {
    private static final String TAG         = MeshBufferHelper.class.getSimpleName();
    private final int           mIndices;
    private final FloatBuffer[] mMeshBuffer = new FloatBuffer[3];  // vertex, texture, normal

    public  MeshBufferHelper(int indices,FloatBuffer[] meshBuffer) {
        mIndices        = indices;
        mMeshBuffer[0]  = meshBuffer[0];
        mMeshBuffer[1]  = meshBuffer[1];
        mMeshBuffer[2]  = meshBuffer[2];
    }

    public int getCount() {
        return mIndices;
    }
    public FloatBuffer[] getBuffer() {
        return mMeshBuffer;
    }
}
