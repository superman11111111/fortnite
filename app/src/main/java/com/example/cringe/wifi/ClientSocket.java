package com.example.cringe.wifi;


import android.content.Context;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocket extends AsyncTask{
    private static final String TAG = "===ClientSocket";

    private Socket socket;
    byte[] data;

    public ClientSocket(Context context, WifiP2PFrag activity, byte[] arr) {
        this.data = arr;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        sendBytes();
        return null;
    }

    public void sendBytes() {
        String host = WifiP2PFrag.IP;
        int port = 1234;
        int len;
        socket = new Socket();
        byte[] buf = new byte[1024];
        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 500);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = new ByteArrayInputStream(data);
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}