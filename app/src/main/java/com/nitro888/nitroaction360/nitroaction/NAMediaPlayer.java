package com.nitro888.nitroaction360.nitroaction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.nitro888.nitroaction360.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class NAMediaPlayer {
    private static final String         TAG                     = NAMediaPlayer.class.getSimpleName();
    private Context                     mContext;

    private NAViewsToGLRenderer         mNAViewsToGLRenderer    = null;

    private MediaPlayer                 mMediaPlayer            = null;

    public NAMediaPlayer(Context context) {
        mContext                = context;
        mMediaPlayer            = new MediaPlayer();
    }

    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer    = viewTOGLRenderer;
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
            FileDescriptor fd               = fileInputStream.getFD();
            mMediaPlayer.setDataSource(fd);
            mMediaPlayer.prepare();
            setTextureSize();
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
    public void testPlay() {
        if(mMediaPlayer==null)
            mMediaPlayer    = new MediaPlayer();

        try {
            //mMediaPlayer.setDataSource("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");
            AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.big_buck_bunny);
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            setTextureSize();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        mMediaPlayer.setSurface(mNAViewsToGLRenderer.getSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER));
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }
*/
    private void setTextureSize() {
        mNAViewsToGLRenderer.setTextureWidth(
                NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,
                mMediaPlayer.getVideoWidth());
        mNAViewsToGLRenderer.setTextureHeight(
                NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,
                mMediaPlayer.getVideoHeight());
        mNAViewsToGLRenderer.createSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER);
        mMediaPlayer.setSurface(mNAViewsToGLRenderer.getSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER));
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }
}
