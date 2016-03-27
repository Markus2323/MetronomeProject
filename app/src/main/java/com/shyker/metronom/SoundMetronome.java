package com.shyker.metronom;


public class SoundMetronome extends AbstractMetronome{
	private static SoundMetronome soundMetronome;
	private double[] silenceSoundArray;
	private double[] soundTickArray;
	private int silence;

	private AudioGenerator audioGenerator = new AudioGenerator(8000);

	private SoundMetronome(){
		audioGenerator.createPlayer();
	}
	public static SoundMetronome getInstance(){
		if (soundMetronome == null){
			soundMetronome = new SoundMetronome();
		}
		return soundMetronome;
	}

	@Override
	public void calcSilence() {
		int tick1 = 1000;
		silence = 60 * 8000 / bpm - tick1;
		soundTickArray = new double[tick1];
		silenceSoundArray = new double[silence];
		double beatSound = 2440;
		double[] tick = audioGenerator.getSineWave(tick1, 8000, beatSound);
		for(int i=0;i< tick1;i++) {
			soundTickArray[i] = tick[i];
		}
		for(int i=0;i< silence;i++)
			silenceSoundArray[i] = 0;
	}
	@Override
	public void play() {
			audioGenerator.writeSound(soundTickArray);
			audioGenerator.writeSound(silenceSoundArray);
	}
}
