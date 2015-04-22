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
        public int     mDuration;
        public String  mDownloadUrl;
        public String  mQuality;
        public String  mCodecs;
        public String  mType;
        public String  mStereo3d;
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
            try {
                Map         map             = parseQueryString(result);
                String      title           = map.get("title").toString();
                String      videoDuration   = map.get("length_seconds").toString();
                String[]    videos          = URLDecoder.decode(map.get("url_encoded_fmt_stream_map").toString(), "utf-8").split(",");

                for(int i = 0 ; i < videos.length ; i ++ ) {
                    Map     item    = parseQueryString(videos[i]);

                    String server           = URLDecoder.decode(item.get("fallback_host").toString(),"utf-8");
                    String[] type           = URLDecoder.decode(item.get("type").toString(),"utf-8").split(";");
                    Object stereo3D         = item.get("stereo3d");

                    YouTubeVideoQuality videoItem   = new YouTubeVideoQuality();
                    videoItem.mVideoTitle   = title;
                    videoItem.mDuration     = Integer.parseInt(videoDuration);
                    videoItem.mDownloadUrl  = URLDecoder.decode(item.get("url").toString(),"utf-8") + "&fallback_host=" + server;
                    videoItem.mQuality      = URLDecoder.decode(item.get("quality").toString(),"utf-8");
                    videoItem.mType         = type[0];
                    videoItem.mCodecs       = type.length==2?type[1]:"";
                    videoItem.mStereo3d     = stereo3D!=null?URLDecoder.decode(stereo3D.toString(),"utf-8"):"";

                    list.add(videoItem);
                }
            }  catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        return list;
    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> map = new HashMap<String, String>();

        String[] parameters;

        parameters= queryString.split("&");

        for(int i = 0; i < parameters.length; i++) {
            String[] keyAndValue;

            keyAndValue = parameters[i].split("=");

            if(keyAndValue.length != 2) {
                Log.e(TAG, "invalid url parameter " + parameters[i]);
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
        String  url         = "";
        String  stereoType  = "";

        for(int i = 0 ; i < result.size() ; i++ )
            if((((YouTubeVideoQuality)result.get(i)).mStereo3d.equals("1")) &&
                    (((YouTubeVideoQuality)result.get(i)).mType.equals("video/mp4"))) {
                stereoType  = ((YouTubeVideoQuality)result.get(i)).mStereo3d;
                url         = ((YouTubeVideoQuality)result.get(0)).mDownloadUrl;
            }

        if(url=="")
            for(int i = 0 ; i < result.size() ; i++ ) {
                if (((YouTubeVideoQuality)result.get(i)).mType.equals("video/mp4"))
                    url = ((YouTubeVideoQuality) result.get(i)).mDownloadUrl;
            }

        if(url!="")
            ((MainActivity) mContext).openMovieStream(url, stereoType.equals("1")? ScreenTypeHelper.SCREEN_RENDER_3D_SBS:ScreenTypeHelper.SCREEN_RENDER_2D);
    }
}