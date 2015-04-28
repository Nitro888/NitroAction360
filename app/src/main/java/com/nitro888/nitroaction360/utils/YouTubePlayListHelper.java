package com.nitro888.nitroaction360.utils;

import android.content.Context;
import android.util.Log;

import com.akoscz.youtube.GetYouTubePlaylistAsyncTask;
import com.akoscz.youtube.Playlist;
import com.akoscz.youtube.PlaylistItem;
import com.github.kevinsawicki.etag.EtagCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by nitro888 on 15. 4. 20..
 */
public class YouTubePlayListHelper {
    private static final String     TAG                                     = YouTubePlayListHelper.class.getSimpleName();
    private final Context mContext;

    public static final int     YOUTUBE_CH_3D_EARTH_PLAYLIST_ID             = 0;
    public static final int     YOUTUBE_CH_3D_SPACE_PLAYLIST_ID             = 1;
    public static final int     YOUTUBE_CH_3D_GAME_PLAYLIST_ID              = 2;
    public static final int     YOUTUBE_CH_3D_MV_PLAYLIST_ID                = 3;
    public static final int     YOUTUBE_CH_3D_MOVIE_TRAILER_PLAYLIST_ID     = 4;
    public static final int     YOUTUBE_CH_3D_ANIMATION_PLAYLIST_ID         = 5;
    public static final int     YOUTUBE_CH_3D_DEMO_PLAYLIST_ID              = 6;
    public static final int     YOUTUBE_CH_SP360_PLAYLIST_ID                = 7;
    public static final int     YOUTUBE_CH_YT360_PLAYLIST_ID                = 8;
    private static final int    YOUTUBE_CH_MAX_ID                           = 9;

    private static final String YOUTUBE_CH_3D_EARTH_PLAYLIST                = "PLJki1W8ICQGQAW9C6O2u4rjl453QZ652u";
    private static final String YOUTUBE_CH_3D_SPACE_PLAYLIST                = "PLJki1W8ICQGRu_AR8xBrskxVKLbYKaoiP";
    private static final String YOUTUBE_CH_3D_GAME_PLAYLIST                 = "PLJki1W8ICQGTEfwle5oRcP-ccR4q8ezwZ";
    private static final String YOUTUBE_CH_3D_MV_PLAYLIST                   = "PLJki1W8ICQGQpoJxXWhd1fMl6isdGbZ9h";
    private static final String YOUTUBE_CH_3D_MOVIE_TRAILER_PLAYLIST        = "PLJki1W8ICQGRXWAcb8cuJcXLMTCghT57m";
    private static final String YOUTUBE_CH_3D_ANIMATION_PLAYLIST            = "PLJki1W8ICQGS-vEb3KunxbVdMBz-_3UA7";
    private static final String YOUTUBE_CH_3D_DEMO_PLAYLIST                 = "PLJki1W8ICQGQmOpJ0AJdWc9uQbmyD0bPb";
    private static final String YOUTUBE_CH_SP360_PLAYLIST                   = "PLJki1W8ICQGQVIXd-nQ2FTm_nxa9A2xRd";
    private static final String YOUTUBE_CH_YT360_PLAYLIST                   = "PLJki1W8ICQGSUEOUp-5729SZ5XyibJtNg";

    private GetYouTubePlaylist[]    mYoutubePlayList                        = new GetYouTubePlaylist[YOUTUBE_CH_MAX_ID];

    public YouTubePlayListHelper(Context context) {
        mContext            = context;

        mYoutubePlayList[YOUTUBE_CH_3D_EARTH_PLAYLIST_ID]           = new GetYouTubePlaylist(YOUTUBE_CH_3D_EARTH_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_SPACE_PLAYLIST_ID]           = new GetYouTubePlaylist(YOUTUBE_CH_3D_SPACE_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_GAME_PLAYLIST_ID]            = new GetYouTubePlaylist(YOUTUBE_CH_3D_GAME_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_MV_PLAYLIST_ID]              = new GetYouTubePlaylist(YOUTUBE_CH_3D_MV_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_MOVIE_TRAILER_PLAYLIST_ID]   = new GetYouTubePlaylist(YOUTUBE_CH_3D_MOVIE_TRAILER_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_ANIMATION_PLAYLIST_ID]       = new GetYouTubePlaylist(YOUTUBE_CH_3D_ANIMATION_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_3D_DEMO_PLAYLIST_ID]            = new GetYouTubePlaylist(YOUTUBE_CH_3D_DEMO_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_SP360_PLAYLIST_ID]              = new GetYouTubePlaylist(YOUTUBE_CH_SP360_PLAYLIST);
        mYoutubePlayList[YOUTUBE_CH_YT360_PLAYLIST_ID]              = new GetYouTubePlaylist(YOUTUBE_CH_YT360_PLAYLIST);

        for(int i = 0 ; i < mYoutubePlayList.length ; i ++ )
            mYoutubePlayList[i].GetYouTubePlaylistAsyncTask();
    }

    public int getCount(int category) {
        if(mYoutubePlayList[category].mIsLoadComplete)
            return mYoutubePlayList[category].getPlaylist().getCount();

        return 0;
    }

    public PlaylistItem getItem(int category, int position) {
        if(mYoutubePlayList[category].mIsLoadComplete)
            return mYoutubePlayList[category].getPlaylist().getItem(position);

        return null;
    }

    private class GetYouTubePlaylist {
        private EtagCache mEtagCache;
        private Playlist mPlaylist;
        private String              mPlaylistID;

        public  boolean             mIsLoadComplete = false;

        public GetYouTubePlaylist(String playlistID) {
            mPlaylistID = playlistID;
        }
        public Playlist getPlaylist() {
            return mPlaylist;
        }

        public void GetYouTubePlaylistAsyncTask() {
            mIsLoadComplete = false;

            File cacheFile  = new File(mContext.getFilesDir(), mPlaylistID);
            mEtagCache      = EtagCache.create(cacheFile, EtagCache.FIVE_MB);

            new GetYouTubePlaylistAsyncTask() {
                @Override
                public EtagCache getEtagCache() {
                    return mEtagCache;
                }

                @Override
                public void onPostExecute(JSONObject result) {
                    handlePlaylistResult(result);
                }
            }.execute(mPlaylistID, null);
        }

        private void handlePlaylistResult(JSONObject result) {
            Log.d(TAG, result.toString());
            try {
                if (mPlaylist == null) {
                    mPlaylist = new Playlist(result);
                } else {
                    mPlaylist.addPage(result);
                }
                mIsLoadComplete = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}