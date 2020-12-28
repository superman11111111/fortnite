package com.example.cringe.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cringe.R;

public class AudioInterceptorFrag extends Fragment {

    public static final String TAG = "===AudioInterceptorFrag";

    Button btn;
    TextView audioStatus;
    TextView text;
    AudioInterceptor audioInterceptor;
    boolean recording = false;
    boolean permissionGranted = true;

    byte[] audio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_audio, container, false);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = false;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1234);
        }

        text = rootView.findViewById(R.id.audio_txt);
        audioStatus = rootView.findViewById(R.id.audio_status);
        btn = rootView.findViewById(R.id.audio_btn);

        audio = new byte[4096];
        audioInterceptor = new AudioInterceptor(audio, text, getActivity());

        btn.setOnClickListener(v -> {
            if (permissionGranted) {
                if (!audioInterceptor.isRecording) {
                    audioInterceptor.start();
                    audioStatus.setText("RECORDING_ACTIVE");
                } else {
                    audioInterceptor.stop();
                    audioStatus.setText("RECORDING_STOPPED");
                }
            } else {
                Toast.makeText(rootView.getContext(), "Please give the RECORD_AUDIO permission in the settings!", Toast.LENGTH_SHORT).show();
            }
        });
        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1234:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (audioInterceptor.init() == 0) {
            Toast.makeText(this.getContext(), "Could not initialize Audio Interceptor", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Could not initialize Audio Interceptor");
        }
    }

    @Override
    public void onStop() {
        audioInterceptor.stop();
        audioInterceptor.destroy();
        super.onStop();
    }
}
