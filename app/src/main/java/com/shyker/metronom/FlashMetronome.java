package com.shyker.metronom;

import android.hardware.Camera;
import android.util.Log;

/**
 * Created by Олег on 24.03.2016.
 */
public class FlashMetronome extends AbstractMetronome {
    private static FlashMetronome flashMetronome;
    private Camera camera;
    private boolean isFlashOn;
    private Camera.Parameters params;
    private volatile int silence;

    private FlashMetronome() {
        getCamera();
    }
    public static FlashMetronome getInstance(){
        if (flashMetronome ==null){
            flashMetronome = new FlashMetronome();
        }
        return flashMetronome;
    }
    @Override
    void calcSilence() {

        silence = (int)(510 - (bpm - 60)*7.2);
        Log.d("FLASH_METRONOME", "silence="+silence+ " bpm="+bpm);
    }
    @Override
    void play() {
                turnOnFlash();
                turnOffFlash();
            try {
                Thread.sleep(silence);
            } catch (InterruptedException e) {
                /*NOP*/
            }
    }
    private void getCamera() {
        if (camera == null) {
            camera = Camera.open();
            params = camera.getParameters();
        }
    }
    private void turnOnFlash() {
		if (!isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
			isFlashOn = true;
		}
    }
    private void turnOffFlash() {
		if (isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
			isFlashOn = false;
		}
    }
}
