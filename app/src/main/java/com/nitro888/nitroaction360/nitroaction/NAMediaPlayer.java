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
    private static final String TAG             = NAMediaPlayer.class.getSimpleName();
    private Context             mContext;

    private MediaPlayer         mMediaPlayer    = null;

    public NAMediaPlayer() {
        mMediaPlayer    = new MediaPlayer();
    }

    public int getWidth() {
        if(mMediaPlayer!=null)
            return mMediaPlayer.getVideoWidth();
        return -1;
    }

    public int getHeight() {
        if(mMediaPlayer!=null)
            return mMediaPlayer.getVideoHeight();
        return -1;
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
            FileDescriptor fd              = fileInputStream.getFD();

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

    private void testPlay() {
        if(mMediaPlayer==null)
            mMediaPlayer    = new MediaPlayer();

        try {
            //mMediaPlayer.setDataSource("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov");

            AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(R.raw.big_buck_bunny);
            mMediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
