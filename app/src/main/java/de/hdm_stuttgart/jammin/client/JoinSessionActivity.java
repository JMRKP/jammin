package de.hdm_stuttgart.jammin.client;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.HashMap;

import de.hdm_stuttgart.jammin.R;
import de.hdm_stuttgart.jammin.core.StartActivity;
import de.hdm_stuttgart.jammin.core.ToastHandler;
import de.hdm_stuttgart.jammin.repertoire.Repertoire;

public class JoinSessionActivity extends AppCompatActivity {

    private final int GRANT_LOCATION_PERMISSION = 4;


    private BroadcastReceiver deviceDiscoveredReceiver;

    private Handler toastHandler;

    private ArrayAdapter<String> arrayAdapter;
    private ListView deviceList;
    private HashMap<String, BluetoothDevice> deviceMap;
    private Button closeConnectionButton;

    private BluetoothAdapter bta;
    private ConnectThread ct;
    private Repertoire userRepertoire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bta = BluetoothAdapter.getDefaultAdapter();

        //Get the apps toast handler
        toastHandler = ToastHandler.getToastHandler(getApplicationContext());

        //All the devices found while discovering bluetooth devices are saved here
        deviceMap = new HashMap<>();

        initView();

        //Register the Receiver
        registerDiscoverReceiver();

        //Before discovery starts clear the dataset and the adapter
        arrayAdapter.clear();
        deviceMap.clear();

        //Start the disvovery
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, GRANT_LOCATION_PERMISSION);
            } else {
                bta.startDiscovery();
            }
        } else {
            bta.startDiscovery();
        }

        /*
        * Load the users repertoire
        * */
        SharedPreferences myRepertoire = getSharedPreferences(Repertoire.SHAREDPREF_KEY, Context.MODE_PRIVATE);
        userRepertoire = new Repertoire();
        try {
            userRepertoire.loadFromDevice(myRepertoire);
        } catch (JSONException e) {
            Log.d("SHARED_PREF_ERROR", "Error while loading repertoire from device.");
            Toast toast = Toast.makeText(getApplicationContext(), "Could not load your song repertoire from device.", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
    }


    /*
    * Initialize the view
    * */

    private void initView(){
        setContentView(R.layout.activity_join_session);
        deviceList = (ListView) findViewById(R.id.deviceList);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1);
        deviceList.setAdapter(arrayAdapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String deviceName = arrayAdapter.getItem(position);
                BluetoothDevice device = deviceMap.get(deviceName);
                ct = ConnectThread.getInstance();
                if(ct.isAlive()){
                    if(!device.getName().equals(ct.getConnectedDeviceName())){
                        Toast.makeText(getApplicationContext(),"You are already connected to device " + ct.getConnectedDeviceName()+". Close the current connection before connecting to new device", Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        Log.d("CONNECTION_THREAD",""+ct.isAlive());
                    }
                }else{
                    Log.d("NULL_POINTER", userRepertoire +"");
                    Log.d("CONNECTION_THREAD","else else "+ct.isAlive());
                    ct.setUserRepertoire(userRepertoire);
                    ct.setConnectionDevice(device);
                    ct.setToastHandler(toastHandler);
                    ct.start();
                    Log.d("CONNECTION_THREAD","is alive? "+ct.isAlive());
                }
                Intent intent = new Intent(getApplicationContext(), SessionRepertoireActivity.class);
                startActivity(intent);

            }
        });
        closeConnectionButton = (Button) findViewById(R.id.close_connection_button);
        closeConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ct != null && ct.isAlive()){
                    ct.cancel();
                }
            }
        });
    }


    /*
    * User must confirm disvovery start
    * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == GRANT_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                bta.startDiscovery();
            } else {
                Toast.makeText(getApplicationContext(), "You should allow discovery so you can find devices you wanna connect to.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(deviceDiscoveredReceiver);
        } catch( IllegalArgumentException e) {
            e.printStackTrace();
            Log.d("RECEIVER_ERROR","Error while unregistering Broadcastreceiver in onDestroy of JoinSessionActivity");
        }
    }

    public void registerDiscoverReceiver(){

        //Broadcast Receiver that receives an intent, when the system finds a device
        deviceDiscoveredReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("DEVICE_FOUND",device.toString()+ " Name: " + device.getName());
                    if(device.getName() != null && !deviceMap.containsKey(device.getName())){
                        Log.d("DEVICE_ADD","Added device");
                        deviceMap.put(device.getName(), device);
                        arrayAdapter.add(device.getName());
                    }
                }
            }
        };

        //Receiver registrieren
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceDiscoveredReceiver, filter); // Don't forget to unregister during onDestroy
    }



    /*
    * Navigate up in the navigation hierarchy when the user presses the back-button
    * */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(intent);
    }
}
