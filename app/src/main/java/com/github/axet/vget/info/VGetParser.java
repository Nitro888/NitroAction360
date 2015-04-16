package com.github.axet.vget.info;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.info.VideoInfo.States;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.ex.DownloadInterruptedError;

public abstract class VGetParser {

    public abstract VideoInfo info(URL web);

    public void info(VideoInfo info, AtomicBoolean stop, Runnable notify) {
        try {
            DownloadInfo dinfo = extract(info, stop, notify);

            info.setInfo(dinfo);

            dinfo.setReferer(info.getWeb());

            dinfo.extract(stop, notify);
        } catch (DownloadInterruptedError e) {
            info.setState(States.STOP, e);

            throw e;
        } catch (RuntimeException e) {
            info.setState(States.ERROR, e);

            throw e;
        }
    }

    public abstract DownloadInfo extract(final VideoInfo vinfo, final AtomicBoolean stop, final Runnable notify);

}
