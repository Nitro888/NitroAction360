package com.nitro888.nitroaction360.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import com.github.kevinsawicki.etag.CacheRequest;
import com.github.kevinsawicki.etag.EtagCache;

/**
 * Created by nitro888 on 15. 4. 20..
 */
public abstract class YouTubeDownloadHelper extends AsyncTask<String, Void, List> {
    private static final String     TAG = YouTubeDownloadHelper.class.getSimpleName();

    private class YouTubeVideoQuality
    {
        public String  mVideoTitle;
        public String  mExtention;
        public String  mDownloadUrl;
        public String  mVideoUrl;
        public int     mVideoSize;
        public int     mVideoScreenWidth;
        public int     mVideoScreenHeight;
        public int     mVideoScreenHight;
        public int     mVideoLength;

        public String formatSize(int width, int height)
        {
            String s = height >= 720 ? " HD" : "";
            return width + " x " + height + s;
        }
    }

    public YouTubeDownloadHelper(String VideoID) {
        final String                    id      = VideoID;
        final String                    infoUrl =   "http://www.youtube.com/get_video_info?&video_id=" + id +
                "&el=detailpage&ps=default&eurl=&gl=US&hl=en";
        mUriBuilder = Uri.parse(infoUrl).buildUpon();
    }

    @Override
    protected List<YouTubeVideoQuality> doInBackground(String... params) {
        final List<YouTubeVideoQuality> list = new ArrayList<YouTubeVideoQuality>();

        final String result = doGetUrl(mUriBuilder.build().toString());

        if (result == null) {
            Log.e(TAG, "Failed to get playlist");
            return null;
        } else {


            //String videos           = infoValues.get("url_encoded_fmt_stream_map"].Split(',');
        }

        return list;
    }

    protected Uri.Builder mUriBuilder;

    public abstract EtagCache getEtagCache();

    public String doGetUrl(String url) {
        Log.d(TAG, url);

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
}
