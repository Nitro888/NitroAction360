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
public class NAMediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {
    private static final String         TAG                     = NAMediaPlayer.class.getSimpleName();
    private Context                     mContext;

    public static final int             PLAYER_STOP             = 0;
    public static final int             PLAYER_PLAY             = 1;
    public static final int             PLAYER_PAUSE            = 2;

    private NAViewsToGLRenderer         mNAViewsToGLRenderer    = null;

    private MediaPlayer                 mMediaPlayer            = null;

    private final static int            STEP_SKIP               = 60000;
    private final static int            STEP_FAST               = 30000;
    private int                         mCurrentPosition        = 0;
    private int                         mBufferingPercent       = 0;
    private boolean                     mIsSetDataSource        = false;
    private boolean                     mIsBufferingStart       = false;
    private int                         mPlayState              = PLAYER_STOP;

    public NAMediaPlayer(Context context) {
        mContext                = context;
        mMediaPlayer            = new MediaPlayer();

        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
    }

    public void setViewToGLRenderer(NAViewsToGLRenderer viewTOGLRenderer){
        mNAViewsToGLRenderer    = viewTOGLRenderer;
    }

    public int  getPlayState() {
        return mPlayState;
    }

    /*
    Play movie
    */
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void openMovieFile(String fileName) {
        if(mMediaPlayer==null)  return;
        mBufferingPercent   = 0;
        mIsBufferingStart   = false;

       Log.d(TAG,"openMovieFile");

        mMediaPlayer.reset();

        try {
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            FileDescriptor fd               = fileInputStream.getFD();
            mMediaPlayer.setDataSource(fd);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void openMovieStream(String url) {
        if(mMediaPlayer==null)  return;
        mBufferingPercent   = 0;
        mIsBufferingStart   = true;

        Log.d(TAG,"openMovieStream");
        /*
            public static void main(String[] args) {
                try {
                    // ex: http://www.youtube.com/watch?v=Nj6PFaDmp6c
                    String url = args[0];
                    // ex: "/Users/axet/Downloads"
                    String path = args[1];
                    VGet v = new VGet(new URL(url), new File(path));
                    v.download();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        */

        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void playOrPause() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        Log.d(TAG,"playOrPause");

        switch (mPlayState) {
            case PLAYER_PAUSE:
                mPlayState          = PLAYER_PLAY;
                mMediaPlayer.seekTo(mCurrentPosition);
                mMediaPlayer.start();
                break;
            case PLAYER_PLAY:
                mPlayState          = PLAYER_PAUSE;
                mCurrentPosition= mMediaPlayer.getCurrentPosition();
                mMediaPlayer.pause();
                break;
        }

    }

    public void skipPrevious() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        Log.d(TAG,"skipPrevious");

        int now         = mMediaPlayer.getCurrentPosition() - STEP_SKIP;

        if(now>0)   mMediaPlayer.seekTo(now);
        else        mMediaPlayer.seekTo(0);
        mCurrentPosition= mMediaPlayer.getCurrentPosition();
    }

    public void skipNext() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        Log.d(TAG,"skipNext");

        int end         = mMediaPlayer.getDuration();
        int now         = mMediaPlayer.getCurrentPosition() + STEP_SKIP;

        if(now<end) mMediaPlayer.seekTo(now);
        mCurrentPosition= mMediaPlayer.getCurrentPosition();
    }

    public void fastRewind() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        Log.d(TAG,"fastRewind");

        int now     = mMediaPlayer.getCurrentPosition() - STEP_FAST;

        if(now>0)   mMediaPlayer.seekTo(now);
        else        mMediaPlayer.seekTo(0);
        mCurrentPosition= mMediaPlayer.getCurrentPosition();
    }

    public void fastForward() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        Log.d(TAG,"fastForward");

        int end     = mMediaPlayer.getDuration();
        int now     = mMediaPlayer.getCurrentPosition() + STEP_FAST;

        if(now<end) mMediaPlayer.seekTo(now);
        mCurrentPosition= mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getBufferingPercent() {
        return mBufferingPercent;
    }
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    private void setTextureSize(int width,int height) {
        Log.d(TAG,"setTextureSize ("+width+","+height+")");
        mNAViewsToGLRenderer.setTextureWidth(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,width);
        mNAViewsToGLRenderer.setTextureHeight(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER,height);
        mNAViewsToGLRenderer.createSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER);
        mMediaPlayer.setSurface(mNAViewsToGLRenderer.getSurface(NAViewsToGLRenderer.SURFACE_TEXTURE_FOR_MEDIAPLAYER));
        mMediaPlayer.setScreenOnWhilePlaying(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsSetDataSource    = true;
        mPlayState          = PLAYER_PLAY;
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG,"onCompletion");
        mPlayState          = PLAYER_STOP;
        mIsSetDataSource    = false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // Method which updates the SeekBar secondary progress by current song loading from URL position
        mBufferingPercent   = percent;

        if(!mIsBufferingStart) {
            mIsBufferingStart   = true;
            mIsSetDataSource    = true;
            mPlayState          = PLAYER_PLAY;
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp,int width,int height) {
        setTextureSize(width,height);
    }
}
