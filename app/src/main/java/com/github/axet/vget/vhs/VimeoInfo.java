package com.github.axet.vget.vhs;

import java.net.URL;

import com.github.axet.vget.info.VideoInfo;

public class VimeoInfo extends VideoInfo {

    // keep it in order hi->lo
    public enum VimeoQuality {
        pHi, pLow
    }

    private VimeoQuality vq;

    public VimeoInfo(URL web) {
        super(web);
    }

    public VimeoQuality getVideoQuality() {
        return vq;
    }

    public void setVideoQuality(VimeoQuality vq) {
        this.vq = vq;
    }

}