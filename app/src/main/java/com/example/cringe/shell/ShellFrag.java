package com.example.cringe.shell;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.cringe.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShellFrag extends Fragment {
    public static final String TAG = "===AudioInterceptorFrag";

    Button btn;
    TextView textView;
    ExecutorService executorService;
    TextInputEditText inputEditText;
    String[] commands;
    String result;
    Fragment instance;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_shell, container, false);
        instance = this;
        commands = new String[]{
                "echo 'hello world'",
        };
        executorService = Executors.newSingleThreadExecutor();
        btn = rootView.findViewById(R.id.shell_btn);
        textView = rootView.findViewById(R.id.shell_output);
        inputEditText = rootView.findViewById(R.id.shell_input);
        btn.setOnClickListener(v -> {
            commands = inputEditText.getText().toString().split("\n");
            executorService.execute(new CommandRunnable());
        });
        return rootView;
    }

    private class CommandRunnable implements Runnable {
        @Override
        public void run() {
            synchronized (mHandler) {
                result = CommandExecutor.execute(true, commands);
                instance.getActivity().runOnUiThread(() -> textView.setText(result));
            }
        }
    }
}
