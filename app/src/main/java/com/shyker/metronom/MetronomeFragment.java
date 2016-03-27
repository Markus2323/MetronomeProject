package com.shyker.metronom;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class MetronomeFragment extends Fragment {
    private ServiceConnection sConn;
    private Intent intent;
    private MetronomeService metronomeService;
    private boolean bound;

    private ImageButton mVibrationButton;
    private ImageButton mFlashButton;
    private ImageButton mSoundButton;
    private Button mStartStopButton;
    private EditText mEditText;
    private ImageButton mMinusButton;
    private ImageButton mPlusButton;
    private SeekBar mSeekBar;
    private ImageButton mIndicator;

    private static Handler mHandler = new Handler();
    private final short minBpm = 40;
    private final short maxBpm = 130;
    private short bpm = 60;

    public MetronomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getActivity(), MetronomeService.class);
        sConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                metronomeService = ((MetronomeService.ServiceBinder)binder).getService();
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        mHandler = getHandler();
    }

    @Override
    public void onStart() {
        super.onStart();
        getContext().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
        bound = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!bound) return;
        getActivity().unbindService(sConn);
        bound = false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mVibrationButton = (ImageButton)v.findViewById(R.id.btn_vibration);
        mVibrationButton.setEnabled(true);
        mVibrationButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                mVibrationButton.setEnabled(false);
                mSoundButton.setEnabled(true);
                mFlashButton.setEnabled(true);
                metronomeService.changeMode(MetronomeService.MODE_VIBRATION);
            }
        });
        mFlashButton = (ImageButton)v.findViewById(R.id.btn_flash);
        mFlashButton.setEnabled(true);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashButton.setEnabled(false);
                mVibrationButton.setEnabled(true);
                mSoundButton.setEnabled(true);
                metronomeService.changeMode(MetronomeService.MODE_FLASH);
            }
        });
        mSoundButton = (ImageButton)v.findViewById(R.id.btn_sound);
        mSoundButton.setEnabled(false);
        mSoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSoundButton.setEnabled(false);
                mVibrationButton.setEnabled(true);
                mFlashButton.setEnabled(true);
                metronomeService.changeMode(MetronomeService.MODE_SOUND);
            }
        });
        mStartStopButton = (Button)v.findViewById(R.id.btn_start_stop);
        mStartStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartStopClick();
            }
        });
        mEditText = (EditText)v.findViewById(R.id.rate_textview);

        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setCursorVisible(true);
            }
        });
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String bpmStr = mEditText.getText().toString();
                    short rate = Short.parseShort(bpmStr);
                    if (rate > maxBpm) {
                        bpm = maxBpm;
                        mEditText.setText(bpm + "");
                        mSeekBar.setProgress(bpm);
                    } else if (rate < minBpm) {
                        bpm = minBpm;
                        mEditText.setText(bpm + "");
                        mSeekBar.setProgress(bpm);
                    } else {
                        bpm = rate;
                        mEditText.setText(bpm + "");
                        metronomeService.setBpm(bpm);
                        maxBpmGuard();
                    }
                    metronomeService.setBpm(bpm);
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mEditText.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });
        mMinusButton = (ImageButton)v.findViewById(R.id.btn_minus);
        mMinusButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onMinusClick(20);
            }
        });
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMinusClick(1);
            }
        });
        mPlusButton = (ImageButton)v.findViewById(R.id.btn_plus);
        mPlusButton.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return onPlusClick(20);
            }
        });
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlusClick(1);
            }
        });
        mSeekBar = (SeekBar)v.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bpm = (short) (progress + 40);
                mEditText.setText(String.format("%s", bpm));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                metronomeService.setBpm(bpm);
                maxBpmGuard();
            }
        });
        mIndicator = (ImageButton)v.findViewById(R.id.indicator);

        return v;
    }
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mIndicator.isActivated()){
                    mIndicator.setActivated(false);
                } else {
                    mIndicator.setActivated(true);
                }
            }
        };
    }
    public synchronized void onStartStopClick() {
        String buttonText = mStartStopButton.getText().toString();
        if(buttonText.equalsIgnoreCase("start")) {
            if (!bound){
                getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
                bound = true;
            }
            metronomeService.setBpm(bpm);
            metronomeService.setHandler(mHandler);
            metronomeService.play();
            mStartStopButton.setText(R.string.stop);
        } else {
            if (bound){
                metronomeService.stopPlaying();
                getActivity().unbindService(sConn);
                metronomeService.stopSelf();
                bound = false;
            }
            mStartStopButton.setText(R.string.start);
            mIndicator.setActivated(false);
        }
    }
    public boolean onPlusClick(int value) {
        bpm+=value;
        if (bpm >= maxBpm)
            bpm = maxBpm;
        mEditText.setText(bpm+"");
        metronomeService.setBpm(bpm);
        maxBpmGuard();
        return true;
    }
    public boolean onMinusClick(int value) {
        bpm-=value;
        if(bpm <= minBpm)
            bpm = minBpm;
        mEditText.setText(bpm+"");
        metronomeService.setBpm(bpm);
        minBpmGuard();
        return true;
    }
    private void maxBpmGuard() {
        if(bpm >= maxBpm) {
            mPlusButton.setEnabled(false);
            mPlusButton.setPressed(false);
        } else if(!mMinusButton.isEnabled() && bpm>minBpm) {
            mMinusButton.setEnabled(true);
        }
    }
    private void minBpmGuard() {
        if(bpm <= minBpm) {
            mMinusButton.setEnabled(false);
            mMinusButton.setPressed(false);
        } else if(!mPlusButton.isEnabled() && bpm<maxBpm) {
            mPlusButton.setEnabled(true);
        }
    }

}
