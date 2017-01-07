package de.hdm_stuttgart.jammin.server;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.hdm_stuttgart.jammin.R;
import de.hdm_stuttgart.jammin.core.CustomAdapter;
import de.hdm_stuttgart.jammin.core.StartActivity;
import de.hdm_stuttgart.jammin.core.ToastHandler;
import de.hdm_stuttgart.jammin.repertoire.Repertoire;

public class StartSessionActivity extends AppCompatActivity {

    BluetoothAdapter bta;
    ConnectionManagerThread connectionManager;
    private Repertoire userRepertoire;
    private Button closeServerButton;
    private ListView connectedDevicesList;
    private ListView sessionRepertoireListView;
    private CustomAdapter sessionRepertoireAdapter;
    private ArrayList<String> devices;
    private ArrayList<Map<JSONObject, Integer>> sharedRepertoire;

    private ConnectedDevicesAdapter deviceAdapter;
    private TabHost mTabHost;
    public final static int CONNECTED_CLIENTS_UPDATED = 3001;
    public final static int SHARED_REPERTOIRE_UPDATED = 3003;
    private final static int SESSION_CLOSE_REQUEST = 3002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_session);

        //Apps toast handler
        final Handler toastHandler = ToastHandler.getToastHandler(getApplicationContext());

        bta = BluetoothAdapter.getDefaultAdapter();

        /*
        * Load the users repertoire from the device
        * */
        SharedPreferences myRepertoire = getSharedPreferences(Repertoire.SHAREDPREF_KEY, Context.MODE_PRIVATE);
        userRepertoire = new Repertoire();
        try {
            userRepertoire.loadFromDevice(myRepertoire);
        } catch (JSONException e) {
            Log.d("SHARED_PREF_ERROR", "Error while loading repertoire from device.");
            Toast.makeText(getApplicationContext(), "Could not load your song repertoire from device.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //Get the connectionManager
        connectionManager = ConnectionManagerThread.getInstance();
        connectionManager.setToastHandler(toastHandler);
        connectionManager.setStartSessionActivityHandler(mHandler);

        if(!connectionManager.isAlive()) {
            connectionManager.start();
        }
        //Send the user Repertoire to ConnectionManager
        connectionManager.setSharedRepertoire(userRepertoire);


        init();
        initTabs();
    }

    /*
    * Initialize the UI
    * */
    private void init() {
        closeServerButton = (Button) findViewById(R.id.Close);
        closeServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionManager.handler.obtainMessage(ConnectionManagerThread.SESSION_CLOSE_REQUEST).sendToTarget();
                Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                startActivity(intent);
            }
        });

        connectedDevicesList = (ListView) findViewById(R.id.connectedDeviceListview);
        devices = new ArrayList<>();
        deviceAdapter = new ConnectedDevicesAdapter(getApplicationContext(), new MyButtonOnClickListener(), devices);
        connectedDevicesList.setAdapter(deviceAdapter);

        sessionRepertoireListView = (ListView) findViewById(R.id.sessionRepertoireList);
        try {
            sharedRepertoire = connectionManager.getSharedRepertoire();
            sessionRepertoireAdapter = new CustomAdapter(getApplicationContext(), sharedRepertoire);
            sessionRepertoireListView.setAdapter(sessionRepertoireAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /*
    * Initialize the tabs
    * */
    private void initTabs() {
        mTabHost = (TabHost) findViewById(R.id.sessionTabHost);
        mTabHost.setup();

        TabHost.TabSpec clientsTab = mTabHost.newTabSpec("Session Clients");
        clientsTab.setIndicator("Session Clients");
        clientsTab.setContent(R.id.sessionTab);
        mTabHost.addTab(clientsTab);

        TabHost.TabSpec sessionRepertoireTab = mTabHost.newTabSpec("Session Repertoire");
        sessionRepertoireTab.setIndicator("Session Repertoire");
        sessionRepertoireTab.setContent(R.id.sessionRepertoireTab);
        mTabHost.addTab(sessionRepertoireTab);
    }

    /*
    *
    * The activities handler
    * */

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case(CONNECTED_CLIENTS_UPDATED):
                    Log.d("CONNECTION_UPDATE", "Msg received in Handler of StartSessionActivity");
                    devices.clear();
                    devices.addAll(connectionManager.getConnectedDevices());
                    deviceAdapter.notifyDataSetChanged();
                    break;
                case (SHARED_REPERTOIRE_UPDATED):
                    try {
                        sharedRepertoire.clear();
                        sharedRepertoire.addAll(connectionManager.getSharedRepertoire());
                        sessionRepertoireAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    /*
    * Onclicklistener to remove buttons
    * */
    private class MyButtonOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            int position = connectedDevicesList.getPositionForView((View) v.getParent());
            String deviceName = (String) deviceAdapter.getItem(position);
            connectionManager.handler.obtainMessage(ConnectionManagerThread.DISCONNECT_FROM_CLIENT, deviceName).sendToTarget();
        }
    }
}
