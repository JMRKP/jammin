package de.hdm_stuttgart.jammin.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdm_stuttgart.jammin.core.StartActivity;
import de.hdm_stuttgart.jammin.core.ToastHandler;
import de.hdm_stuttgart.jammin.repertoire.Repertoire;

/**
 * Created by charles on 08.11.16.
 */

public class ConnectThread extends Thread {

    private static ConnectThread singleton;

    private BluetoothAdapter bta;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private Handler toastHandler;
    private Handler sessionRepertoireActivityHandler;

    private Repertoire userRepertoire;
    private Repertoire sharedRepertoire;

    private final int CONNECTION_FAIL = -1;
    private final int CONNECTION_SUCCESS = 1;


    private ConnectThread() {
        bta = BluetoothAdapter.getDefaultAdapter();
    }

    public void run() {

        // Cancel discovery because it will slow down the connection
        bta.cancelDiscovery();

        //Connect
        if (!connect()) {
            //Cancel Thread if connection fails
            return;
        }

        //Initialize stream
        if(!initializeStream()){
            //Cancel Thread if stream initialization fails
            return;
        }

        //Wait for one second before sending the repertoire so that the server can establish the connection
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Send the client's repertoire
        sendRepertoire();

        //Wait for shared repertoire. The server broadcasts it every time it is updated.
        waitForSharedRepertoire();


        //Before finishing send notification about connection shutdown to user and to SessionRepertoireActivity
        toastHandler.obtainMessage(ToastHandler.USER_NOTIFICATION_TOAST, "Connection was closed.").sendToTarget();
        sessionRepertoireActivityHandler.obtainMessage(SessionRepertoireActivity.CONNECTION_CLOSED).sendToTarget();
    }


    /**
     * Will cancel an in-progress connection, and close the socket
     */

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

    /**
    * Call this from the main activity to send data to the remote device
    */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
        }
    }

    /**
     * This sends the users repertoire to the server as soon as connected
     * */

    public void sendRepertoire(){

        byte[] send = null;
        try {
            send = userRepertoire.makeStreamable();
        } catch (JSONException e) {
            Log.d("JSON_ERROR","Error while making repertoire streamable.");
            e.printStackTrace();
            return;
        }

        try {
            mmOutStream.write(send);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *  Will establish a connection
     *  */

    private boolean connect() {

        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(StartActivity.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mmSocket = tmp;
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            toastHandler.obtainMessage(ToastHandler.USER_NOTIFICATION_TOAST, "Could not establish a connection. Session might be offline.").sendToTarget();
            Log.d("CONNECTION_FAIL", "Client unable to call mmSocket.connect() to connect to the server.");
            try {
                mmSocket.close();
            } catch (IOException closeException) {

            } finally {
                return false;
            }

        }
        return true;
    }


    /**
     *  Will initialize the stream
     *  */

    private boolean initializeStream() {

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final

        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.d("BLUETOOTH_ERROR","Error while getting in- and outputstreams from sockets");
            try {
                mmSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }finally {
                return false;
            }
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        return true;
    }

    /**
     * Will listen for incomming repertoires in a loop.
     * When the connection is finished the loop terminates and the thread finishes.
     * */

    private void waitForSharedRepertoire() {

        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (mmSocket.isConnected()) {
            try {

                bytes = mmInStream.read(buffer);
                try {
                    sharedRepertoire.fromStream(buffer, bytes);
                } catch (JSONException e) {
                    Log.d("JSON_ERROR","Error while getting shared repertoire from stream.");
                    e.printStackTrace();
                }

                sessionRepertoireActivityHandler
                        .obtainMessage(SessionRepertoireActivity.UPDATE_SHARED_REPERTOIRE)
                        .sendToTarget();


            } catch (IOException e) {
                break;
            }
        }
    }

    /**
     * Get singleton instance of ConnectThread. If the thread has died it will return a new instance.
     * */

    public static ConnectThread getInstance(){
        if (singleton == null){
            singleton = new ConnectThread();
        }else if (!singleton.isAlive()) {
            singleton = new ConnectThread();
        }
        return singleton;
    }
    public String getConnectedDeviceName(){
        return mmDevice.getName();
    }

    public ArrayList<Map<JSONObject, Integer>> getSharedRepertoire() throws JSONException {
        ArrayList<Map<JSONObject, Integer>> asList = new ArrayList<>();
        JSONArray repertoire = sharedRepertoire.getRepertoire();
        for(int i = 0; i < repertoire.length(); i++){
            JSONObject song = repertoire.getJSONObject(i).getJSONObject(Repertoire.KEY_SONG);
            Integer counter = repertoire.getJSONObject(i).getInt(Repertoire.KEY_COUNTER);

            // only show songs in the shared repertoire that have a counter bigger than 1
            if (counter > 1) {
                HashMap<JSONObject, Integer > entry = new HashMap<>();
                entry.put(song, counter);
                asList.add(entry);
            }
        }
        return asList;
    }


    /*
    * Setter for SessionRepertoireActivity to pass its handler. Can set it only once to each instance.
    * */

    public void setSessionRepertoireActivityHandler(Handler handler){
        if(sessionRepertoireActivityHandler == null){
            sessionRepertoireActivityHandler = handler;
        }
    }

    /*
    * Setter for JoinSessionActivity to pass the user's repertoire. Can set it only once to each instance.
    * The shared repertoire is initialized with the repertoire of the user that hosts the session
    * */

    public void setUserRepertoire(Repertoire userRepertoire){
        if(this.userRepertoire==null){
            this.userRepertoire = userRepertoire;
            this.sharedRepertoire = userRepertoire;
        }
    }

    /*
    * Setter for the toasthandler
    * */

    public void setToastHandler(Handler toastHandler){
        if(this.toastHandler == null){
            this.toastHandler = toastHandler;
        }
    }


    /*
    * Setter for the device to connect to.
    * */
    public void setConnectionDevice(BluetoothDevice device){
        if(this.mmDevice == null){
            this.mmDevice = device;
        }
    }

}
