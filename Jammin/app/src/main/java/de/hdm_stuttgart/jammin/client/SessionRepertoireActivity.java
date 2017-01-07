package de.hdm_stuttgart.jammin.client;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.hdm_stuttgart.jammin.R;
import de.hdm_stuttgart.jammin.core.CustomAdapter;
import de.hdm_stuttgart.jammin.core.ToastHandler;

public class SessionRepertoireActivity extends AppCompatActivity {

    private ArrayList<Map<JSONObject, Integer>> sharedRepertoire;
    private Handler toastHandler;
    private ConnectThread connection;
    private CustomAdapter adapter;
    private ListView listView;

    public final static int UPDATE_SHARED_REPERTOIRE = 2001;
    public static final int CONNECTION_CLOSED = 2002;


    /*
    * The activities handler
    *
    * It updates the view, when the shared repertoire changes.
    * On connection shutdown it goes back to JoinSessionActivity
    * */

    final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case (UPDATE_SHARED_REPERTOIRE):
                    Log.d("HANDLER_RECEIVE","Handled UPDATE_SHARED_REPERTOIRE");
                    try {
                        if (connection.getSharedRepertoire().size() < 1) {
                            Toast.makeText(getApplicationContext(), "There are no matching songs amongst the connected devices.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        sharedRepertoire.clear();
                        sharedRepertoire.addAll(connection.getSharedRepertoire());
                        adapter.notifyDataSetChanged();
                        Log.d("SHARED_REPERTOIRE", ""+sharedRepertoire.toString());

                    } catch (JSONException e) {
                        Log.d("JSON_ERROR", "Could not get SharedRepertoire from ConnectThread.");
                        e.printStackTrace();
                    }

                    break;
                case (CONNECTION_CLOSED):
                    Intent intent = new Intent(getApplicationContext(), JoinSessionActivity.class);
                    startActivity(intent);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_repertoire);
        toastHandler = ToastHandler.getToastHandler(getApplicationContext());
        connection = ConnectThread.getInstance();
        connection.setSessionRepertoireActivityHandler(mHandler);
        if(!connection.isAlive()){
                connection.start();
        }
        Log.d("LABEL","Starting Listview");
        listView = (ListView) findViewById(R.id.sessionRepertoireList);
        try {
            sharedRepertoire = connection.getSharedRepertoire();
            Log.d("SHARED_REPERTOIRE","IS: " + sharedRepertoire);
            adapter = new CustomAdapter(getApplicationContext(), sharedRepertoire);
            listView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSON_ERROR","Could not get sharedRepertoire from ConnectionManager.");
        }
    }


    /*
    * Navigate up in the navigation hierarchy when the user presses the back-button
    * */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent intent = new Intent(getApplicationContext(), JoinSessionActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), JoinSessionActivity.class);
        startActivity(intent);
    }
}
