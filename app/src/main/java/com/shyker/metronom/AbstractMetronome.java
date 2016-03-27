package com.shyker.metronom;


import android.os.Handler;

/**
 * Created by Олег on 23.03.2016.
 */
public abstract class AbstractMetronome {
    protected short bpm;
    protected Handler mHandler;
    public short getBpm() {
        return bpm;
    }
    public void setBpm(short bpm) {
        this.bpm = bpm;
    }
    abstract void calcSilence();
    abstract void play();
}
