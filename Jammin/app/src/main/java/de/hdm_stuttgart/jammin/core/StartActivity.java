package de.hdm_stuttgart.jammin.core;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.UUID;

import de.hdm_stuttgart.jammin.R;
import de.hdm_stuttgart.jammin.client.ConnectThread;
import de.hdm_stuttgart.jammin.client.JoinSessionActivity;
import de.hdm_stuttgart.jammin.repertoire.RepertoireActivity;
import de.hdm_stuttgart.jammin.server.ConnectionManagerThread;
import de.hdm_stuttgart.jammin.server.StartSessionActivity;

public class StartActivity extends AppCompatActivity {

    final int ENABLE_BT_REQUEST_CODE_SERVER = 1;
    final int ENABLE_DISCOVERABILTY_REQUEST_CODE = 2;
    final int ENABLE_BT_REQUEST_CODE_CLIENT = 3;
    public static final UUID MY_UUID = UUID.fromString("1d39505e-a1b7-11e6-80f5-76304dec7eb7");

    BluetoothAdapter bta;
    Button startSessionActivityButton;
    Button joinSessionActivityButton;
    Button repertoireActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        //Register the apps toasthandler

        final Handler toastHandler = ToastHandler.getToastHandler(getApplicationContext());


        /*
        * Initialize the view
        * */

        repertoireActivityButton = (Button) findViewById(R.id.repertoireActivityButton);
        repertoireActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RepertoireActivity.class);
                startActivity(intent);
            }
        });


        /*
        * To join a session bluetooth must be enabled and there should be no open server connection
        * */

        joinSessionActivityButton = (Button) findViewById(R.id.joinSessionActivityButton);
        joinSessionActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!bta.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_CODE_CLIENT);
                } else if (ConnectionManagerThread.getInstance().isAlive()) {
                    Toast.makeText(getApplicationContext(), "To join a new session cancel the session you are hosting first.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), JoinSessionActivity.class);
                    startActivity(intent);
                }


            }
        });

        /*
        * To create a session bluetooth and discoverability must be enabled.
        * There should be connection as a client to another server
        * */

        startSessionActivityButton = (Button) findViewById(R.id.startSessionActivityButton);
        startSessionActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Turn down connections to other server
                ConnectThread connectThread = ConnectThread.getInstance();

                if(connectThread != null && connectThread.isAlive()){
                    Toast.makeText(getApplicationContext(), "Leave the current session before you create a new one.", Toast.LENGTH_LONG).show();
                }else if (!bta.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_CODE_SERVER);
                }else {
                    //Ask for Discoverability
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(discoverableIntent, ENABLE_DISCOVERABILTY_REQUEST_CODE);
                }
            }
        });


        bta = BluetoothAdapter.getDefaultAdapter();


        /*
        * Without bluetooth support you cant establish connections
        * */
        if (bta == null) {
            startSessionActivityButton.setEnabled(false);
            joinSessionActivityButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
            return;
        }

    }


    /*
    * User dialog result handler method
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT_REQUEST_CODE_SERVER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth switched on.", Toast.LENGTH_SHORT).show();

                //Ask for Discoverability
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                startActivityForResult(discoverableIntent, ENABLE_DISCOVERABILTY_REQUEST_CODE);

            } else {
                Toast.makeText(getApplicationContext(), "Please turn on Bluetooth to start a session.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ENABLE_DISCOVERABILTY_REQUEST_CODE) {
            if (resultCode != RESULT_CANCELED) {
                Intent intent = new Intent(getApplicationContext(), StartSessionActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Please start session again and confirm discoverability.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == ENABLE_BT_REQUEST_CODE_CLIENT) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(getApplicationContext(), JoinSessionActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Please turn on Bluetooth to join a session.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
