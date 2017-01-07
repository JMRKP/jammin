package de.hdm_stuttgart.jammin.server;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.hdm_stuttgart.jammin.core.ToastHandler;
import de.hdm_stuttgart.jammin.repertoire.Repertoire;


/**
 * Created by charles on 10.11.16.
 */

public class ConnectionManagerThread extends Thread {

    //Single Instance
    private static ConnectionManagerThread singleton;

    private AcceptThread at;
    private HashSet<ConnectionThread> connections;
    public final int MAX_CONNECTION_NUMBER = 8;

    //Message codes for the handler
    public final static int ADD_CLIENT_REPERTOIRE = 02;
    public final static int CONNECTION_CLOSED = 03;
    public final static int SESSION_CLOSE_REQUEST = 04;
    public final static int NEW_CLIENT_CONNECTION = 05;
    public final static int DISCONNECT_FROM_CLIENT = 06;


    //The threads handler
    public Handler handler;

    //The toast handler
    private Handler toastHandler;

    //Ui Handler
    private Handler startSessionActivityHandler;


    //Repertoire of all users
    private Repertoire sharedRepertoire;

    private ConnectionManagerThread(){

        connections = new HashSet<>();

        /*Initialize handler see bottom*/
    }

    @Override
    public void run() {

        //Looper for the message queue
        Looper.prepare();
        handler = initHandler();
        Log.d("Handler in ConM",""+ handler);

        //start the accept thread
        at = AcceptThread.getInstance();
        if(!at.isAlive()){
            at.start();
        }

        Looper.loop();
        return;
    }

    /**
     * Call this to get the Managers Instance
     * */

    public static ConnectionManagerThread getInstance(){
        if(singleton == null) {
            singleton = new ConnectionManagerThread();
        }else if(!singleton.isAlive()){
            singleton = new ConnectionManagerThread();
        }

        return singleton;

    }

    /**
     * Call this to add a connection
     * */

    private void addConnection(BluetoothSocket socket) {
        //Stop Accept-Thread, when connections full.
        if (connections.size() >= MAX_CONNECTION_NUMBER-1){
            at.cancel();
        }
        ConnectionThread connection = new ConnectionThread(socket);
        connection.start();
        connections.add(connection);
        toastHandler.obtainMessage(ToastHandler.USER_NOTIFICATION_TOAST, "The phone " + connection.clientDevice.getName() + " joined the session.")
                .sendToTarget();
        startSessionActivityHandler.obtainMessage(StartSessionActivity.CONNECTED_CLIENTS_UPDATED).sendToTarget();
    }


    /**
     * Call this to remove closed connection from the map with all connections.
     *
     * It unmerges the repertoire related to the connection and then notifies the ui about connection and repertoire changes
     * */

    private void removeConnection(int threadId) throws IllegalArgumentException {
        Log.d("METHOD_CALL","RemoveConnection in ConnectionManagerThread");
        if(connections.size() <= 0){
            throw new IllegalArgumentException("No connection left to remove.");
        } else {

            //Search for the connection to be removed
            ConnectionThread connection;
            ConnectionThread tmp = null;
            Iterator<ConnectionThread> it = connections.iterator();
            while(it.hasNext()) {
                tmp = it.next();
                if (tmp.getId()==threadId) {
                    break;
                } else {
                    tmp = null;
                }
            }

            if (tmp != null) {
                connection = tmp;
            } else {
                Log.d("CONNECTION", "No such connection found for ConnectionManagerThread.removeConnection!");
                return;
            }


            //Unmerge the repertoire
            try {
                sharedRepertoire.unmergeFromRepertoire(connection.getClientRepertoire());
            } catch (JSONException e) {
                Log.d("JSON_ERROR", "Error while unmerging repertoire.");
            }
            try {
                Log.d("SHARED_REPERTOIRE","After client has left session. " + sharedRepertoire.getRepertoire());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            connections.remove(connection);

            //Send message to UI
            toastHandler.obtainMessage(ToastHandler.USER_NOTIFICATION_TOAST, "The phone " + connection.clientDevice.getName() +" left the session.").sendToTarget();
            startSessionActivityHandler.obtainMessage(StartSessionActivity.CONNECTED_CLIENTS_UPDATED).sendToTarget();

            //Start Accept Thread, if space for new connections.
            if(at==null || !at.isAlive()){
                at = AcceptThread.getInstance();
                at.start();
            }
        }

    }

    /**
     * Iterates through all current connections and writes the shared repertoire to the outputstream.
     * */

    public void broadcastSharedRepertoire(){

        //Convert repertoire to streamable
        byte[] send = null;
        try {
            send = sharedRepertoire.makeStreamable();
        } catch (JSONException e) {
            Log.d("JSON_ERROR", "Error while converting repertoire to byte stream.");
            e.printStackTrace();
        }

        //Iterate through connections and write the repertoire to each stream.
        Iterator<ConnectionThread> it = connections.iterator();
        while (it.hasNext() && send != null){
            it.next().write(send);
        }

    }

    /*
    * Initializes the repertoire on thread start with user repertoire
    * */
    public void setSharedRepertoire (Repertoire sharedRepertoire){
        if(this.sharedRepertoire == null){
            this.sharedRepertoire = sharedRepertoire;
        }
    }


    /**
     * Cancel a connection from the UI by device name
     * */

    private void cancelConnection(String deviceName){
        Log.d("METHOD_CALL","CancelConnection in connectionManager with parameter" + deviceName);
        Iterator<ConnectionThread> it = connections.iterator();
        while(it.hasNext()){
            ConnectionThread connection = it.next();
            Log.d("REMOVE_DEVICE","Connected Device. Name: " + connection.clientDevice.getName());
            if(connection.clientDevice.getName().equals(deviceName)){
                connection.cancelConnection();
            }
        }
    }

    /**
     *
     * Closes all the connections and the accept thread and turns down the this thread.
     *
     * */

    private void closeConnectionManager(){

        if(at.isAlive()){
            at.cancel();
        }
        //close all connections
        if(connections.size() > 0){
            ConnectionThread connection;
            Iterator<ConnectionThread> it = connections.iterator();
            while(it.hasNext()){
                connection = it.next();
                connection.cancelConnection();
            }
        }

        Looper.myLooper().quit();
        toastHandler.obtainMessage(ToastHandler.USER_NOTIFICATION_TOAST, "The session you hosted is finished.").sendToTarget();
    }


    /**
     * Call this from the UI to get the shared repertoire
     * */
    public ArrayList<Map<JSONObject, Integer>> getSharedRepertoire() throws JSONException {
        ArrayList<Map<JSONObject, Integer>> asList = new ArrayList<>();
        JSONArray repertoire = sharedRepertoire.getRepertoire();
        for (int i = 0; i < repertoire.length(); i++) {
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
    * Sets the toast handler on thread initialization
    * */

    public void setToastHandler(Handler toastHandler) {
        if(this.toastHandler == null){
            this.toastHandler = toastHandler;
        }
    }


    //Sets the ui handler
    public void setStartSessionActivityHandler(Handler startSessionActivityHandler) {
        if(this.startSessionActivityHandler == null){
            this.startSessionActivityHandler = startSessionActivityHandler;
        }
    }


    /*
    * Call this from the UI to get a list of the connected devices
    * */
    public ArrayList<String> getConnectedDevices(){
        ArrayList<String> devices = new ArrayList<>();
        Iterator<ConnectionThread> it = connections.iterator();
        while (it.hasNext()){
            String deviceName = it.next().clientDevice.getName();
            devices.add(deviceName);
        }
        return devices;

    }

    /*
    * The threads handler
    * */

    private Handler initHandler(){
        return new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case ADD_CLIENT_REPERTOIRE:
                        Repertoire fromClient = (Repertoire) msg.obj;
                        try {
                            sharedRepertoire.mergeWithRepertoire(fromClient);
                        } catch (JSONException e) {
                            Log.d("JSON_ERROR","Error merging client Repertoire.");
                            e.printStackTrace();
                            return;
                        }
                        startSessionActivityHandler.obtainMessage(StartSessionActivity.SHARED_REPERTOIRE_UPDATED).sendToTarget();
                        broadcastSharedRepertoire();
                        break;
                    case CONNECTION_CLOSED:
                        Log.d("MESSAGE", "CONNECTION_CLOSED in ConnectionManager Thread with Thread ID:" + msg.arg1);
                        removeConnection(msg.arg1);
                        startSessionActivityHandler.obtainMessage(StartSessionActivity.SHARED_REPERTOIRE_UPDATED).sendToTarget();
                        broadcastSharedRepertoire();
                        break;
                    case SESSION_CLOSE_REQUEST:
                        closeConnectionManager();
                        break;
                    case NEW_CLIENT_CONNECTION:
                        BluetoothSocket socket = (BluetoothSocket) msg.obj;
                        addConnection(socket);
                        break;
                    case DISCONNECT_FROM_CLIENT:
                        String deviceName = (String) msg.obj;
                        Log.d("DISCONNECT_FROM_CLIENT","DeviceName:" + deviceName);
                        cancelConnection(deviceName);
                        break;
                }
            }
        };
    }
}
