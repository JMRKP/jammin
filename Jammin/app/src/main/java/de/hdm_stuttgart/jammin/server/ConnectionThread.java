package de.hdm_stuttgart.jammin.server;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.hdm_stuttgart.jammin.repertoire.Repertoire;

/**
 * Created by charles on 08.11.16.
 */

public class ConnectionThread extends Thread {

    private final ConnectionManagerThread connectionManager;
    private final Handler mHandler;
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    //The device connected to
    public final BluetoothDevice clientDevice;

    //The remote devices repertoire
    private Repertoire clientRepertoire;


    public ConnectionThread(BluetoothSocket socket) {

        mmSocket = socket;
        clientDevice = socket.getRemoteDevice();
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            try {
                mmSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        connectionManager = ConnectionManagerThread.getInstance();
        mHandler = connectionManager.handler;
        clientRepertoire = new Repertoire();
    }

    public void run() {

        //Wait for incomming repertoire from client in a loop
        waitForRepertoireAndHandle();

        //Notify Connectionmanager on connection cancellation.
        Message msg = mHandler.obtainMessage(ConnectionManagerThread.CONNECTION_CLOSED);
        msg.arg1 = (int)currentThread().getId();
        msg.sendToTarget();
        Log.d("CONNECTION_CLOSE","Connection to " + clientDevice.getName() + " was closed.");
    }

    /**
     * Called from the connectionManager to send data to the remote device
     * */

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /**
     * Reads clients repertoires and forwards it to the connection manager to be handled.
     * */

    public void waitForRepertoireAndHandle(){
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (mmSocket.isConnected()) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                try {
                    clientRepertoire.fromStream(buffer, bytes);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mHandler.obtainMessage(ConnectionManagerThread.ADD_CLIENT_REPERTOIRE, clientRepertoire).sendToTarget();

            } catch (IOException e) {
                break;
            }
        }
    }


    /* Call this from the main activity to shutdown the connection */
    public void cancelConnection() {
        Log.d("METHOD_CALL","CancelConnection in ConnectionThread");
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.d("SOCKET_ERROR","Error while closing the socket.");
        }
    }


    /*
    * Get information about connection status from other threads
    * */
    public boolean isConnected(){
        if(mmSocket == null){
            return false;
        }
        return mmSocket.isConnected();
    }

    /*
    * Returns a copy of the repertoire to other threads
    * */

    public Repertoire getClientRepertoire () throws JSONException {
        return new Repertoire(clientRepertoire);
    }

}
