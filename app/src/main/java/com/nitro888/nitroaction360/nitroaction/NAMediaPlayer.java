package com.nitro888.nitroaction360.nitroaction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.SeekBar;

import com.nitro888.nitroaction360.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class NAMediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener{
    private static final String         TAG                     = NAMediaPlayer.class.getSimpleName();
    private Context                     mContext;

    private NAViewsToGLRenderer         mNAViewsToGLRenderer    = null;

    private MediaPlayer                 mMediaPlayer            = null;
    private SeekBar                     seekBarProgress         = null;

    public NAMediaPlayer(Context context) {
        mContext                = context;
        mMediaPlayer            = new MediaPlayer();
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
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

    public void openMovieFile(String fileName) {
        if(mMediaPlayer==null)  return;

        mMediaPlayer.reset();

        Log.d(TAG,"openMovieFile : " + fileName);

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            FileDescriptor fd               = fileInputStream.getFD();
            mMediaPlayer.setDataSource(fd);

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    setTextureSize();
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void openMovieStream(String url) {
        if(mMediaPlayer==null)  return;

        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(url);

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    setTextureSize();
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void play() {
        if(mMediaPlayer==null)  return;
        mMediaPlayer.start();
    }

    public void pause() {
        if(mMediaPlayer==null)  return;
        mMediaPlayer.pause();
    }

    public void skipPrevious() {
        if(mMediaPlayer==null)  return;

    }

    public void skipNext() {
        if(mMediaPlayer==null)  return;

    }

    public void fastRewind() {
        if(mMediaPlayer==null)  return;

    }

    public void fastForward() {
        if(mMediaPlayer==null)  return;

    }

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

    @Override
    public void onCompletion(MediaPlayer mp) {
        // MediaPlayer onCompletion event handler. Method which calls then song playing is complete
        //buttonPlayPause.setImageResource(R.drawable.button_play);
        Log.d(TAG,"onCompletion");
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // Method which updates the SeekBar secondary progress by current song loading from URL position
        if(seekBarProgress!=null)
            seekBarProgress.setSecondaryProgress(percent);

        Log.d(TAG,"buffer percent : " + percent);
        Log.d(TAG,"play : " + mp.getCurrentPosition() + " / " + mp.getDuration());

    }
}
