package de.hdm_stuttgart.jammin.repertoire;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import de.hdm_stuttgart.jammin.R;

public class RepertoireActivity extends AppCompatActivity {

    private final String URL_ROOT = "https://itunes.apple.com/search?term=";
    private URL iTunesUrl;
    private JSONArray currentSongResults = new JSONArray();
    private Repertoire userRepertoire;

    private TabHost mTabHost;
    private ListView songListView;
    private ListView repertoireListView;
    private ArrayAdapter<String> songListAdapter;
    private ArrayAdapter<String> repertoireListAdapter;
    private Button itunesRequestButton;
    private EditText onlineSearchEditText;
    private EditText repertoireSearchEditText;
    private InputMethodManager imm;
    private SharedPreferences myRepertoirePreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repertoire);

        initTabs();
        initListViews();
        initRest();
        loadRepertoire();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveRepertoire();
    }

    /**
     * Initializes the repertoire tab and search tab
     */
    private void initTabs() {
        mTabHost = (TabHost) findViewById(R.id.repertoireTabHost);
        mTabHost.setup();

        TabHost.TabSpec repertoireTab = mTabHost.newTabSpec("Repertoire");
        repertoireTab.setIndicator("Repertoire Tab");
        repertoireTab.setContent(R.id.repertoireTab);
        mTabHost.addTab(repertoireTab);

        TabHost.TabSpec searchTab = mTabHost.newTabSpec("Search");
        searchTab.setIndicator("Search Tab");
        searchTab.setContent(R.id.searchTab);
        mTabHost.addTab(searchTab);

        // Set all checkboxes in repertoire tab to checked when the user changes to this tab
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                saveRepertoire();
                if (tabId.equals("Repertoire")) {
                    try {
                        if (userRepertoire.getRepertoire().toString().equals("[]")) {
                            Toast.makeText(getApplicationContext(), "Repertoire empty. Add songs from search tab", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if(tabId.equals("Search")) {
                    refreshSongListCheckedItems();
                }
            }
        });
    }

    /**
     * Initializes the list views including the underlying array adapters and sets listener for
     * when the user click on a list entry
     */
    private void initListViews() {
        // Set listviews with corresponding arrayadapters
        songListView = (ListView) findViewById(R.id.searchListView);
        songListAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.custom_listview_item);
        songListView.setAdapter(songListAdapter);
        songListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        repertoireListView = (ListView) findViewById(R.id.repertoireListView);
        repertoireListAdapter = new CheckedItemsArrayAdapter(getApplicationContext(), R.layout.custom_listview_item);
        repertoireListView.setAdapter(repertoireListAdapter);
        repertoireListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Set checkbox event listener with method calls for adding and removing songs from the repertoire
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView ctv = (CheckedTextView) view;
                String songFullName = songListView.getItemAtPosition(position).toString();
                JSONObject song = getJsonSongFromFullName(songFullName);
                try {
                    if (ctv.isChecked()) {
                        repertoireListAdapter.add(songFullName);
                        if (userRepertoire.addSong(song.getJSONObject("song"))) {
                            Log.d("REPERTOIRE", "Added new song: " + songFullName);
                        }
                    } else {
                        repertoireListAdapter.remove(songFullName);
                        if (userRepertoire.removeSong(song)) {
                            Log.d("REPERTOIRE", "Removed song: " + songFullName);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        repertoireListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songFullName = repertoireListView.getItemAtPosition(position).toString();
                repertoireListAdapter.remove(songFullName);
                repertoireListAdapter.notifyDataSetChanged();
                try {
                    if (userRepertoire.removeSong(songFullName)) {
                        Log.d("REPERTOIRE", "Removed song: " + songFullName);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Initializes button functionality and the hiding of the keyboard when starting to send API Requests
     */
    private void initRest() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        myRepertoirePreference = getSharedPreferences(Repertoire.SHAREDPREF_KEY, Context.MODE_PRIVATE);

        onlineSearchEditText = (EditText) findViewById(R.id.repertoireOnlineSearchTextBox);
        onlineSearchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                //If the keyevent is a key-down event on the "enter" button
                if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendApiRequest(onlineSearchEditText.getText().toString());
                    imm.hideSoftInputFromWindow(onlineSearchEditText.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        itunesRequestButton = (Button) findViewById(R.id.itunesRequestButton);
        itunesRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendApiRequest(onlineSearchEditText.getText().toString());
                imm.hideSoftInputFromWindow(itunesRequestButton.getWindowToken(), 0);
            }
        });

/*          filter not compatible with remove style of elements that we use
            repertoireSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RepertoireActivity.this.repertoireListAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*/
    }

    /**
     * Helper method to either check or uncheck all items in a ListView
     * @param lv The ListView to be taken care of
     * @param checked The desired check value
     */
    private void setAllListItemsChecked(ListView lv, boolean checked) {
        for (int i = 0; i < lv.getChildCount(); i++) {
            lv.setItemChecked(i, checked);
        }
    }

    /**
     * Returns the matching JSONObject of a song from the JSONArray in the background
     *
     * @param checkedSongFullName A songs full name including track artist in the format 'Artist - Songname'
     * @return The matching JSONObject of a song
     */
    private JSONObject getJsonSongFromFullName(String checkedSongFullName) {
        for (int i = 0; i < currentSongResults.length(); i++) {
            try {
                JSONObject currentSong = currentSongResults.getJSONObject(i);
                if (checkedSongFullName.equals(getFullNameFromJsonSong(currentSong))) {
                    return currentSong;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    /**
     * Concatenates the full song name from a song JSONObject
     *
     * @param song The JSONObject to a song
     * @return The song's full name including track artist in the format 'Artist - Songname'
     */
    private String getFullNameFromJsonSong(JSONObject song) {
        try {
            JSONObject songInfo = song.getJSONObject("song");
            return songInfo.getString("artistName") + " - " + songInfo.getString("trackName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Starts the AsyncTask for sending API requests to the iTunes Search API
     *
     * @param suffix The suffix for the request url
     */
    private void sendApiRequest(String suffix) {
        suffix = suffix.replace(' ', '+');
        suffix += "&limit=10&media=music"; // limit search results to 10 per request and only search for music
        try {
            iTunesUrl = new URL(URL_ROOT + suffix);
            Log.d("ITUNES_REQUEST", "Executing async iTunes API request with url: " + iTunesUrl.toString());
            ApiRequestTask.ApiRequestListener listener = new ApiRequestTask.ApiRequestListener() {
                @Override
                public void onDone(JSONArray songs) {
                    setSongsListView(songs);
                }
            };
            ApiRequestTask mApiRequestTask = new ApiRequestTask();
            mApiRequestTask.onDoneLoading(listener);
            mApiRequestTask.execute(iTunesUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after each API Request this method first updates the song list with the entries
     * from each new Request
     *
     * @param songs The JSONArray containing the found songs in JSONObjects
     */
    private void setSongsListView(JSONArray songs) {
        // clear song list and reset checkboxes
        songListAdapter.clear();
        setAllListItemsChecked(songListView, false);

        if (songs.length() == 0) {
            Toast.makeText(getApplicationContext(), "No results found!", Toast.LENGTH_SHORT).show();
        } else {
            // if results were found, format artist and track name into a string array for the songlist arrayadapter
            int resultCount = songs.length();
            String[] songStrings = new String[resultCount];
            currentSongResults = new JSONArray();
            for (int i = 0; i < resultCount; i++) {
                JSONObject currentResult = null;
                JSONObject song = new JSONObject();
                try {
                    currentResult = songs.getJSONObject(i);
                    if (currentResult.get("wrapperType").toString().equals("track")) {
                        song.put("song", currentResult);
                        songStrings[i] = getFullNameFromJsonSong(song);
                        currentSongResults.put(song);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            songListAdapter.addAll(songStrings);
            refreshSongListCheckedItems();
        }
    }

    /**
     * Iterates over search song list and set checkboxes if songs are in repertoire
     */
    private void refreshSongListCheckedItems() {
        setAllListItemsChecked(songListView, false);
        Log.e("songListView.getC", String.valueOf(songListView.getChildCount()));
        for (int i = 0; i < songListView.getChildCount(); i++) {
            String currentSong = songListView.getItemAtPosition(i).toString();
            try {
                if (!userRepertoire.getSong(currentSong).toString().equals("{}")) {
                    songListView.setItemChecked(i, true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the users repertoire
     */
    private void saveRepertoire() {
        userRepertoire.saveOnDevice(myRepertoirePreference);
    }

    /**
     * Load the users repertoire
     */
    private void loadRepertoire() {
        userRepertoire = new Repertoire();
        try {
            userRepertoire.loadFromDevice(myRepertoirePreference);
            JSONArray loadedRepertoire = userRepertoire.getRepertoire();
            if (userRepertoire.getRepertoire().toString().equals("[]")) {
                Toast.makeText(getApplicationContext(), "Repertoire empty. Add songs from search tab", Toast.LENGTH_LONG).show();
            }
            for (int i = 0; i < loadedRepertoire.length(); i++) {
                repertoireListAdapter.add(getFullNameFromJsonSong(loadedRepertoire.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Log.d("SHARED_PREF_ERROR", "Error while loading repertoire from device.");
            Toast.makeText(getApplicationContext(), "Could not load your song repertoire from device.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
