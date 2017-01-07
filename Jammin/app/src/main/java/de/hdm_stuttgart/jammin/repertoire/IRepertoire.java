package de.hdm_stuttgart.jammin.repertoire;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface IRepertoire {

    boolean removeSong(JSONObject song) throws JSONException;

    boolean removeSong(String songFullName) throws JSONException;

    JSONObject getSong(String songFullName) throws JSONException;

    boolean addSong(JSONObject song) throws JSONException;

    int contains(JSONObject song) throws JSONException;

    byte[] makeStreamable() throws JSONException;

    JSONArray getRepertoire() throws JSONException;

    void fromStream(byte[] input, int bytes) throws JSONException;

    void mergeWithRepertoire(Repertoire userRepertoire) throws JSONException;

    void unmergeFromRepertoire(Repertoire userRepertoire) throws JSONException;

    void saveOnDevice(SharedPreferences sharedPref);

    void loadFromDevice(SharedPreferences sharedPref) throws JSONException;
}
