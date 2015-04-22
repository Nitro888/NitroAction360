package com.nitro888.nitroaction360.utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.kevinsawicki.etag.CacheRequest;
import com.github.kevinsawicki.etag.EtagCache;
import com.nitro888.nitroaction360.MainActivity;

/**
 * Created by nitro888 on 15. 4. 20..
 */
public abstract class YouTubeDownloadHelper extends AsyncTask<String, Void, List> {
    private static final    String      TAG          = YouTubeDownloadHelper.class.getSimpleName();
    private final Context               mContext;
    private EtagCache                   mEtagCache;

    private class YouTubeVideoQuality {
        public String  mVideoTitle;
        public String  mExtention;
        public String  mDownloadUrl;
        public String  mVideoUrl;
        public int     mVideoSize;
        public int     mVideoScreenWidth;
        public int     mVideoScreenHeight;
        public int     mVideoScreenHight;
        public int     mVideoLength;

        public String formatSize(int width, int height) {
            String s = height >= 720 ? " HD" : "";
            return width + " x " + height + s;
        }
    }

    public YouTubeDownloadHelper(Context context, String youtubeID) {
        mContext        = context;
        File cacheFile  = new File(context.getFilesDir(), youtubeID);
        mEtagCache      = EtagCache.create(cacheFile, EtagCache.FIVE_MB);
    }

    @Override
    protected List<YouTubeVideoQuality> doInBackground(String... params) {
        final List<YouTubeVideoQuality> list = new ArrayList<YouTubeVideoQuality>();

        final String    infoUrl =   "http://www.youtube.com/get_video_info?&video_id=" +
                                    params[0] +
                                    "&el=detailpage&ps=default&eurl=&gl=US&hl=en";
        mUriBuilder = Uri.parse(infoUrl).buildUpon();

        final String result = doGetUrl(mUriBuilder.build().toString());

        if (result == null) {
            Log.e(TAG, "Failed to get url");
            return null;
        } else {
            Map         map             = parseQueryString(result,false);

            String      title           = map.get("title").toString();
            String      videoDuration   = map.get("length_seconds").toString();
            String[]    videos          = map.get("url_encoded_fmt_stream_map").toString().split("%2C");    // %2C is ,

            for(int i = 0 ; i < videos.length ; i ++ ) {
                try {
                    Map item = parseQueryString(videos[i], true);
                    String server   = item.get("fallback_host").toString();
                    String url      = URLDecoder.decode(URLDecoder.decode(item.get("url").toString(),"utf-8"),"utf-8") + "&fallback_host=" + server;

                    YouTubeVideoQuality videoItem   = new YouTubeVideoQuality();
                    videoItem.mDownloadUrl          = url;
                    videoItem.mVideoTitle           = title;
                    //videoItem.mVideoLength          = Integer.getInteger(videoDuration);


                    String tagInfo   = item.get("itag").toString();
                    Log.d(TAG,tagInfo);
                    Log.d(TAG,videos[i]);

                    list.add(videoItem);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        return list;
    }

    private static Map<String, String> parseQueryString(String queryString, boolean isUTF8) {
        Map<String, String> map = new HashMap<String, String>();

        String[] parameters;

        if(isUTF8)  parameters= queryString.split("%26");
        else        parameters= queryString.split("&");

        for(int i = 0; i < parameters.length; i++) {
            String[] keyAndValue;

            if(isUTF8)  keyAndValue = parameters[i].split("%3D");
            else        keyAndValue = parameters[i].split("=");

            if(keyAndValue.length != 2) {
                Log.d(TAG, "invalid url parameter " + parameters[i]);
                continue;
            }

            String key = keyAndValue[0];
            String value = keyAndValue[1];

            map.put(key, value);
        }

        return map;
    }

    protected Uri.Builder mUriBuilder;

    public EtagCache getEtagCache() {
        return mEtagCache;
    }

    public String doGetUrl(String url) {
        CacheRequest request = CacheRequest.get(url, getEtagCache());
//        Log.d(TAG, "Response was " + request.body());

        StringBuilder builder = new StringBuilder();
        InputStream is = request.stream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (request.cached()) {
            Log.d(TAG, "Cache hit");
        } else {
            Log.d(TAG, "Cache miss");
        }

        return builder.toString();
    }

    public static void GetYouTubeDownloadURLAsyncTask(Context context, String youtubeID) {
        new YouTubeDownloadHelper(context,youtubeID) {
            @Override
            public void onPostExecute(List result) {
                handlePlaylistResult(result);
            }
        }.execute(youtubeID, null);
    }

    public void handlePlaylistResult(List result) {
        String url = ((YouTubeVideoQuality)result.get(0)).mDownloadUrl;
        ((MainActivity) mContext).openMovieStream(url);
    }
}