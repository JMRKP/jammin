package de.hdm_stuttgart.jammin.server;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import de.hdm_stuttgart.jammin.core.StartActivity;

/**
 * Created by charles on 04.11.16.
 */

public class AcceptThread extends Thread {


    private static AcceptThread acceptThread;
    private final BluetoothServerSocket mmServerSocket;
    private boolean keepOnListening = true;
    private Handler connectionManagerHandler;
    ConnectionManagerThread connectionManager;


    private AcceptThread() {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        try {
            tmp = bta.listenUsingRfcommWithServiceRecord("app", StartActivity.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
        connectionManager = ConnectionManagerThread.getInstance();
        connectionManagerHandler = connectionManager.handler;
    }

    public void run() {
        BluetoothSocket socket = null;
        Log.d("ACCEPT_THREAD", "is running");

        // Keep listening until exception occurs or a socket is returned
        while (keepOnListening) {
            Log.d("ACCEPT_THREAD", "Server listening for requests.");
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("BLUETOOTH_ERROR", "Error while waiting for BT clients to connect.");
            }

            // If a connection was accepted
            if (socket != null) {

                //Pass the connection to the connection manager 's queue
                connectionManagerHandler.obtainMessage(ConnectionManagerThread.NEW_CLIENT_CONNECTION, socket).sendToTarget();
                //free socket for next client
                socket = null;
            }

        }

        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {

        keepOnListening = false;
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.d("BLUETOOTH_SERVER_ERROR","Error while closing the BluetoothServer.");
        }

    }

    public static AcceptThread getInstance(){
        if(acceptThread == null){
            acceptThread = new AcceptThread();
        }else if(!acceptThread.isAlive()){
            acceptThread = new AcceptThread();
        }
        return acceptThread;
    }


}
