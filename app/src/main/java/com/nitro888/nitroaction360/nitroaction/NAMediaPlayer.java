package com.nitro888.nitroaction360.nitroaction;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.wget.info.DownloadInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nitro888 on 15. 4. 14..
 */
public class NAMediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnSeekCompleteListener {
    private static final String         TAG                     = NAMediaPlayer.class.getSimpleName();
    private Context                     mContext;

    public static final int             PLAYER_IDLE             = 0;
    public static final int             PLAYER_INITIALIZED      = 1;
    public static final int             PLAYER_PREPARED         = 2;
    public static final int             PLAYER_STOP             = 3;
    public static final int             PLAYER_PLAY             = 4;
    public static final int             PLAYER_PAUSE            = 5;
    public static final int             PLAYER_PLAY_COMPLETE    = 6;

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
        mMediaPlayer.setOnSeekCompleteListener(this);
    }

    public void pause(){
        if(mIsSetDataSource) {
            mPlayState          = PLAYER_PAUSE;
            mCurrentPosition    = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();
        }
    }

    public void stop() {
    }

    public void resume() {
        if(mIsSetDataSource&&(mPlayState==PLAYER_PAUSE)) {
            mPlayState          = PLAYER_PLAY;
            setTextureSize(mMediaPlayer.getVideoWidth(),mMediaPlayer.getVideoHeight());
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(mCurrentPosition);
        }
    }

    public void onSurfaceChanged() {
        if(mIsSetDataSource) {
            setTextureSize(mMediaPlayer.getVideoWidth(),mMediaPlayer.getVideoHeight());
        }
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
        mCurrentPosition    = 0;
        mIsBufferingStart   = false;

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
        mCurrentPosition    = 0;
        mIsBufferingStart   = true;

        mMediaPlayer.reset();
/*
        try {
            final URL   source  = new URL("https://www.youtube.com/watch?v="+url);
            final VGet  vg      = new VGet(source);

            if (vg.empty()) {
                vg.extract(null, new AtomicBoolean(false), new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
*/
        url = "http://r3---sn-25g7sm7e.googlevideo.com/videoplayback?dur=2639.899&sver=3&expire=1429498850&pl=24&nh=EAI&ratebypass=yes&itag=22&ipbits=0&signature=F6958D6909E6C433C1E91C2204E8F7105A18E108.71742A0E0C99DF6746CF83549151C31219461510&ms=au&mt=1429477116&mv=m&source=youtube&key=yt5&mm=31&id=o-AFXatyhNqLU_N06oBK38pzMas1NNdxGjeWl6o7LSfbvb&upn=AG6W7QsGOKw&initcwndbps=395000&ip=88.248.3.207&mime=video%2Fmp4&sparams=dur%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Cmime%2Cmm%2Cms%2Cmv%2Cnh%2Cpl%2Cratebypass%2Csource%2Cupn%2Cexpire&fexp=900720%2C906335%2C907263%2C916636%2C931383%2C932627%2C934954%2C938028%2C938688%2C9405989%2C9407115%2C9407440%2C9408206%2C9408226%2C9408347%2C9408707%2C9408787%2C9409071%2C947233%2C948124%2C948703%2C951703%2C952612%2C952637%2C957201%2C961404%2C961406&title=IMAX-+Hubble+3D+%28CZ+DOKUMENT+3D+HD+1080p%29";
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public void playOrPause() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        switch (mPlayState) {
            case PLAYER_PAUSE:
                mPlayState          = PLAYER_PLAY;
                mMediaPlayer.pause();
                mMediaPlayer.seekTo(mCurrentPosition);
                break;
            case PLAYER_PLAY:
                pause();
                break;
        }

    }

    public void skipPrevious() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        int now         = mMediaPlayer.getCurrentPosition() - STEP_SKIP;

        mMediaPlayer.pause();
        if(now>0)   mMediaPlayer.seekTo(now);
        else        mMediaPlayer.seekTo(0);
    }

    public void skipNext() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        int end         = mMediaPlayer.getDuration();
        int now         = mMediaPlayer.getCurrentPosition() + STEP_SKIP;

        if(now<end) {
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(now);
        }
    }

    public void fastRewind() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        int now     = mMediaPlayer.getCurrentPosition() - STEP_FAST;

        mMediaPlayer.pause();
        if(now>0)   mMediaPlayer.seekTo(now);
        else        mMediaPlayer.seekTo(0);
    }

    public void fastForward() {
        if((mMediaPlayer==null)||(!mIsSetDataSource))  return;

        int end     = mMediaPlayer.getDuration();
        int now     = mMediaPlayer.getCurrentPosition() + STEP_FAST;

        if(now<end) {
            mMediaPlayer.pause();
            mMediaPlayer.seekTo(now);
        }
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
    public void onSeekComplete(MediaPlayer mp) {
        switch (mPlayState) {
            case PLAYER_PLAY:
                mMediaPlayer.start();
                break;
            case PLAYER_PAUSE:
                mMediaPlayer.pause();
                break;
            case PLAYER_STOP:
                mMediaPlayer.stop();
                break;
        }

        mCurrentPosition= mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp,int width,int height) {
        setTextureSize(width,height);
    }
}
