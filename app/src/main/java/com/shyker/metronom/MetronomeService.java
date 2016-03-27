package com.shyker.metronom;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;

import java.util.concurrent.ExecutorService;

public class MetronomeService extends Service {
    private static final String TAG = MetronomeService.class.getSimpleName();
    public static final int MODE_VIBRATION = 1;
    public static final int MODE_FLASH = 2;
    public static final int MODE_SOUND = 3;

    private static int currentMode = 3;
    private short bpm = 100;

    protected ServiceBinder binder = new ServiceBinder();
    private AbstractMetronome metronome;
    private ExecutorService es;
    private static Vibrator mVibrator;
    private static WorkerThread backgroundThread;
    private static Handler mHandler;

    public int getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;
    }

    public MetronomeService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        metronome = createMetronom(currentMode);
    }

    @Override
    public IBinder onBind(Intent intent) {
        metronome.setBpm(bpm);
        return binder;
    }

    public void changeMode(int mode){
        if (mode != currentMode){
            currentMode = mode;
            if (backgroundThread != null){
                backgroundThread.stopPlay();
            }
            metronome = createMetronom(currentMode);
        }
    }
    public AbstractMetronome createMetronom(int mode){
        switch (mode){
            case MetronomeService.MODE_VIBRATION:
                currentMode = MODE_VIBRATION;
                mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                return VibrationMetronome.getInstance(mVibrator);
            case MetronomeService.MODE_FLASH:
                currentMode = MODE_FLASH;
                return FlashMetronome.getInstance();
            case MetronomeService.MODE_SOUND:
                currentMode = MODE_SOUND;
                return SoundMetronome.getInstance();
            default:
                throw new IllegalArgumentException();
        }
    }
    public void play(){
        backgroundThread = new WorkerThread();
        backgroundThread.start();
    }
    public void stopPlaying() {
//        metronome.stop();
        backgroundThread.stopPlay();
    }
    public void setBpm(short bpm) {
        this.bpm = bpm;
        metronome.setBpm(this.bpm);
        metronome.calcSilence();
    }
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    class ServiceBinder extends Binder {
        protected MetronomeService getService(){
            return MetronomeService.this;
        }
    }

    private class WorkerThread extends Thread {
        private volatile boolean play;

        @Override
        public void run() {
            play = true;
            metronome.setBpm(bpm);
            do{
                metronome.play();
                mHandler.sendMessage(new Message());
            } while (play);
        }
        public void stopPlay() {
            play = false;
        }
    }
}
