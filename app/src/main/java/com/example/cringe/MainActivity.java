package com.example.cringe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "===MainActivity";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pDevice device;

    Button buttonDiscoveryStart;
    Button buttonDiscoveryStop;
    Button buttonConnect;
    Button buttonServerStart;
    Button buttonClientStart;
    Button buttonClientStop;
    Button buttonServerStop;
    Button buttonConfigure;
    EditText editTextTextInput;

    ListView listViewDevices;
    TextView textViewDiscoveryStatus;
    TextView textViewWifiP2PStatus;
    TextView textViewConnectionStatus;
    TextView textViewReceivedData;
    TextView textViewReceivedDataStatus;
    public static String IP = null;
    public static boolean IS_OWNER = false;

    static boolean stateDiscovery = false;
    static boolean stateWifi = false;
    public static boolean stateConnection = false;

    ServerSocketThread serverSocketThread;

    ArrayAdapter<Object> mAdapter;
    WifiP2pDevice[] deviceListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpUI();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);

        serverSocketThread = new ServerSocketThread();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpIntentFilter();
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setUpIntentFilter() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void setUpUI() {
        buttonDiscoveryStart = findViewById(R.id.main_activity_button_discover_start);
        buttonDiscoveryStop = findViewById(R.id.main_activity_button_discover_stop);
        buttonConnect = findViewById(R.id.main_activity_button_connect);
        buttonServerStart = findViewById(R.id.main_activity_button_server_start);
        buttonServerStop = findViewById(R.id.main_activity_button_server_stop);
        buttonClientStart = findViewById(R.id.main_activity_button_client_start);
        buttonClientStop = findViewById(R.id.main_activity_button_client_stop);
        buttonConfigure = findViewById(R.id.main_activity_button_configure);
        listViewDevices = findViewById(R.id.main_activity_list_view_devices);
        textViewConnectionStatus = findViewById(R.id.main_activiy_textView_connection_status);
        textViewDiscoveryStatus = findViewById(R.id.main_activiy_textView_dicovery_status);
        textViewWifiP2PStatus = findViewById(R.id.main_activiy_textView_wifi_p2p_status);
        textViewReceivedData = findViewById(R.id.main_acitivity_data);
        textViewReceivedDataStatus = findViewById(R.id.main_acitivity_received_data);

        editTextTextInput = findViewById(R.id.main_acitivity_input_text);

        buttonServerStart.setOnClickListener(this);
        buttonServerStop.setOnClickListener(this);
        buttonClientStart.setOnClickListener(this);
        buttonClientStop.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonDiscoveryStop.setOnClickListener(this);
        buttonDiscoveryStart.setOnClickListener(this);
        buttonConfigure.setOnClickListener(this);

        buttonClientStop.setVisibility(View.INVISIBLE);
        buttonClientStart.setVisibility(View.INVISIBLE);
        buttonServerStop.setVisibility(View.INVISIBLE);
        buttonServerStart.setVisibility(View.INVISIBLE);
        editTextTextInput.setVisibility(View.INVISIBLE);
        textViewReceivedDataStatus.setVisibility(View.INVISIBLE);
        textViewReceivedData.setVisibility(View.INVISIBLE);


        listViewDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            device = deviceListItems[i];
            Toast.makeText(MainActivity.this, "Selected device :" + device.deviceName, Toast.LENGTH_SHORT).show();
        });


    }

    @SuppressLint("MissingPermission")
    private void discoverPeers() {
        Log.d(MainActivity.TAG, "discoverPeers()");
        setDeviceList(new ArrayList<>());
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = true;
                Log.d(MainActivity.TAG, "peer discovery started");
                makeToast("peer discovery started");
                MyPeerListener myPeerListener = new MyPeerListener(MainActivity.this);
                mManager.requestPeers(mChannel, myPeerListener);

            }

            @Override
            public void onFailure(int i) {
                stateDiscovery = false;
                if (i == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG, " peer discovery failed :" + "P2P_UNSUPPORTED");
                    makeToast(" peer discovery failed :" + "P2P_UNSUPPORTED");

                } else if (i == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG, " peer discovery failed :" + "ERROR");
                    makeToast(" peer discovery failed :" + "ERROR");

                } else if (i == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG, " peer discovery failed :" + "BUSY");
                    makeToast(" peer discovery failed :" + "BUSY");
                }
            }
        });
    }

    private void stopPeerDiscover() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = false;
                Log.d(MainActivity.TAG, "Peer Discovery stopped");
                makeToast("Peer Discovery stopped");
                //buttonDiscoveryStop.setEnabled(false);

            }

            @Override
            public void onFailure(int i) {
                Log.d(MainActivity.TAG, "Stopping Peer Discovery failed");
                makeToast("Stopping Peer Discovery failed");
                //buttonDiscoveryStop.setEnabled(true);

            }
        });

    }

    public void makeToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    public void connect(final WifiP2pDevice device) {
        // Picking the first device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.d(MainActivity.TAG, "Trying to connect : " + device.deviceName);
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(MainActivity.TAG, "Connected to :" + device.deviceName);
                Toast.makeText(getApplication(), "Connection successful with " + device.deviceName, Toast.LENGTH_SHORT).show();
                //setDeviceList(new ArrayList<WifiP2pDevice>());
            }

            @Override
            public void onFailure(int reason) {
                if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG, "P2P_UNSUPPORTED");
                    makeToast("Failed establishing connection: " + "P2P_UNSUPPORTED");
                } else if (reason == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG, "Conneciton falied : ERROR");
                    makeToast("Failed establishing connection: " + "ERROR");

                } else if (reason == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG, "Conneciton falied : BUSY");
                    makeToast("Failed establishing connection: " + "BUSY");

                }
            }
        });
    }

    public void setDeviceList(ArrayList<WifiP2pDevice> deviceDetails) {

        deviceListItems = new WifiP2pDevice[deviceDetails.size()];
        String[] deviceNames = new String[deviceDetails.size()];
        for (int i = 0; i < deviceDetails.size(); i++) {
            deviceNames[i] = deviceDetails.get(i).deviceName;
            deviceListItems[i] = deviceDetails.get(i);
        }
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, deviceNames);
        listViewDevices.setAdapter(mAdapter);
    }

    @SuppressLint("SetTextI18n")
    public void setStatusView(int status) {

        switch (status) {
            case Constants.DISCOVERY_INITATITED:
                stateDiscovery = true;
                textViewDiscoveryStatus.setText("DISCOVERY_INITIATED");
                break;
            case Constants.DISCOVERY_STOPPED:
                stateDiscovery = false;
                textViewDiscoveryStatus.setText("DISCOVERY_STOPPED");
                break;
            case Constants.P2P_WIFI_DISABLED:
                stateWifi = false;
                textViewWifiP2PStatus.setText("P2P_WIFI_DISABLED");
                buttonDiscoveryStart.setEnabled(false);
                buttonDiscoveryStop.setEnabled(false);
                break;
            case Constants.P2P_WIFI_ENABLED:
                stateWifi = true;
                textViewWifiP2PStatus.setText("P2P_WIFI_ENABLED");
                buttonDiscoveryStart.setEnabled(true);
                buttonDiscoveryStop.setEnabled(true);
                break;
            case Constants.NETWORK_CONNECT:
                stateConnection = true;
                makeToast("It's a connect");

                textViewConnectionStatus.setText("Connected");
                break;
            case Constants.NETWORK_DISCONNECT:
                stateConnection = false;
                textViewConnectionStatus.setText("Disconnected");
                makeToast("State is disconnected");
                break;
            default:
                Log.d(MainActivity.TAG, "Unknown status");
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.main_activity_button_discover_start:
                if (!stateDiscovery) {
                    discoverPeers();
                }
                break;
            case R.id.main_activity_button_discover_stop:
                if (stateDiscovery) {
                    stopPeerDiscover();
                }
                break;
            case R.id.main_activity_button_connect:

                if (device == null) {
                    Toast.makeText(MainActivity.this, "Please discover and select a device", Toast.LENGTH_SHORT).show();
                    return;
                }
                connect(device);
                break;
            case R.id.main_activity_button_server_start:
                serverSocketThread = new ServerSocketThread();
                serverSocketThread.setUpdateListener(this::setReceivedText);
                serverSocketThread.execute();
                break;
            case R.id.main_activity_button_server_stop:
                if (serverSocketThread != null) {
                    serverSocketThread.setInterrupted(true);
                } else {
                    Log.d(MainActivity.TAG, "serverSocketThread is null");
                }
                //makeToast("Yet to do...");
                break;
            case R.id.main_activity_button_client_start:
                //serviceDisvcoery.startRegistrationAndDiscovery(mManager,mChannel);
                String dataToSend = editTextTextInput.getText().toString();
                ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, dataToSend);
                clientSocket.execute();
                break;
            case R.id.main_activity_button_configure:
                mManager.requestConnectionInfo(mChannel, this);
                break;
            case R.id.main_activity_button_client_stop:
                makeToast("Yet to do");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        String hostAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();

        /*

        makeToast("Am I group owner : " + String.valueOf(wifiP2pInfo.isGroupOwner));
        makeToast(hostAddress);

        */
        Log.d(MainActivity.TAG, "wifiP2pInfo.groupOwnerAddress.getHostAddress() " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        IP = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        IS_OWNER = wifiP2pInfo.isGroupOwner;

        if (IS_OWNER) {
            buttonClientStop.setVisibility(View.GONE);
            buttonClientStart.setVisibility(View.GONE);
            editTextTextInput.setVisibility(View.GONE);

            buttonServerStop.setVisibility(View.VISIBLE);
            buttonServerStart.setVisibility(View.VISIBLE);

            textViewReceivedData.setVisibility(View.VISIBLE);
            textViewReceivedDataStatus.setVisibility(View.VISIBLE);
        } else {
            //buttonClientStop.setVisibility(View.VISIBLE);
            buttonClientStart.setVisibility(View.VISIBLE);
            editTextTextInput.setVisibility(View.VISIBLE);
            buttonServerStop.setVisibility(View.GONE);
            buttonServerStart.setVisibility(View.GONE);
            textViewReceivedData.setVisibility(View.GONE);
            textViewReceivedDataStatus.setVisibility(View.GONE);
        }

        makeToast("Configuration Completed");
    }

    public void setReceivedText(final String data) {
        runOnUiThread(() -> textViewReceivedData.setText(data));
    }
}