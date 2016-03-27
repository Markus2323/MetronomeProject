package com.shyker.metronom;

import android.os.Vibrator;

/**
 * Created by Олег on 24.03.2016.
 */
public class VibrationMetronome extends AbstractMetronome {
    private static Vibrator mVibrator;
    private static int silence;
    private static boolean play = true;
    private static VibrationMetronome vibrationMetronome;

    private VibrationMetronome(Vibrator vibrator){
        mVibrator = vibrator;
    }
    public static VibrationMetronome getInstance(Vibrator vibrator){
        if (vibrationMetronome == null){
            vibrationMetronome = new VibrationMetronome(vibrator);
        }
        return vibrationMetronome;
    }

    @Override
    void calcSilence() {
        silence = (int)(1000 - (bpm - 60)*8.33333);
    }

    @Override
    void play() {
//        calcSilence();
            mVibrator.vibrate(80);
            try {
                Thread.sleep(silence);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
