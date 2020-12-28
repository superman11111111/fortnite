package com.example.cringe.shell;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.example.cringe.BuildConfig;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class CommandExecutor {
    public static final String TAG = "===CommandExecutor";

    @WorkerThread
    public static @NonNull
    String execute(final boolean needRoot, @NonNull final String[] commands) {
        Process process = null;
        DataOutputStream output2Process = null;
        BufferedReader successReader = null;
        BufferedReader errorReader = null;

        try {
            final String author;

            if (needRoot) {
                author = "su";
            } else {
                author = "sh";
            }

            process = Runtime.getRuntime().exec(author);
            output2Process = new DataOutputStream(process.getOutputStream());

            for (String command : commands) {
                if (!TextUtils.isEmpty(command)) {
                    output2Process.writeBytes(command + " " + "\n");
                }
            }

            output2Process.writeBytes("exit" + " " + "\n");
            output2Process.flush();
            process.waitFor();

            successReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            final StringBuilder successBuilder = new StringBuilder();
            final StringBuilder errorBuilder = new StringBuilder();
            String line;

            while (!TextUtils.isEmpty(line = successReader.readLine())) {
                successBuilder.append(line);
                successBuilder.append("\n");
            }

            while (!TextUtils.isEmpty(line = errorReader.readLine())) {
                errorBuilder.append(line);
                errorBuilder.append("\n");
            }

            if (!TextUtils.isEmpty(line = errorBuilder.toString())) {
            } else {
                line = successBuilder.toString();
            }

            return line;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return e.getMessage();
        } finally {
            try {
                if (output2Process != null) {
                    output2Process.close();
                }

                if (successReader != null) {
                    successReader.close();
                }

                if (errorReader != null) {
                    errorReader.close();
                }

                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }
}