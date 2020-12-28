package com.example.cringe.audio;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AudioInterceptor {
    public static final String TAG = "===AudioInterceptor";

    public final int SAMPLE_RATE = 8000;
    public final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT; // 32 Bits gives Invalid operation error
    public int BUFFER_SIZE = 0;

    byte[] outArr;
    String path;
    TextView textView;
    final Activity activity;

    AudioRecord record;
    ExecutorService executorService;
    Future future;

    boolean isRecording;

    byte[] buff;

    public AudioInterceptor(File file, Activity activity) {
        this.path = file.getPath();
        this.activity = activity;
    }

    public AudioInterceptor(String file, Activity activity) {
        this(new File(activity.getFilesDir(), file), activity);
    }

    public AudioInterceptor(byte[] outArr, Activity activity) {
        this.outArr = outArr;
        this.activity = activity;
    }

    public AudioInterceptor(byte[] outArr, TextView textView, Activity activity) {
        this(outArr, activity);
        this.textView = textView;
    }



    public int init() {
        if (isRecording) return 0;
        executorService = Executors.newSingleThreadExecutor();
        Log.d(TAG, "BUFFER_SIZE=" + BUFFER_SIZE);
        BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
        if (BUFFER_SIZE > 0) {
            buff = new byte[BUFFER_SIZE];
        } else {
            Log.e(TAG, "Unknown error");
            return 0;
        }
        record = new AudioRecord(
                MediaRecorder.AudioSource.UNPROCESSED,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_ENCODING,
                BUFFER_SIZE
        );
        Log.d(TAG, "Initialized!");
        return 1;
    }

    public void start() {
        if (!isRecording) {
            if (record != null) {
                record.startRecording();
                future = executorService.submit(new AudioRunnable());
                isRecording = true;
            }
        }
    }

    public void stop() {
        if (isRecording) {
            if (record != null) {
                record.stop();
                future.cancel(true);
                isRecording = false;
            }
        }
    }

    public void destroy() {
        if (record != null) {
            record.release();
        }
    }

    private boolean all_zeros(byte[] arr) {
        for (byte b : arr) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    private class AudioRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG, "" + buff.length + " " + outArr.length);
            while (isRecording) {
                int result = record.read(buff, 0, buff.length);
                if (result > 0) {
                    if (!all_zeros(buff)) {
//                        System.arraycopy(buff, 0, outArr, 0, buff.length);
                        activity.runOnUiThread(() -> {
                            String[] str = new String[buff.length];
                            for (int i = 0; i < buff.length; i++) {
                                str[i] = String.valueOf(Integer.valueOf(buff[i]));
                            }
                            textView.setText(TextUtils.join(",", str));
                        });
//                        Arrays.fill(buff, (byte) 0);
                    }
                } else if (result == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e("Recording", "Invalid operation error");
                    break;
                } else if (result == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e("Recording", "Bad value error");
                    break;
                } else if (result == AudioRecord.ERROR) {
                    Log.e("Recording", "Unknown error");
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
