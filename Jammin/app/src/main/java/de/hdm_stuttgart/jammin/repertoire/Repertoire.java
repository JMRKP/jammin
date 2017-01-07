package de.hdm_stuttgart.jammin.repertoire;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by charles on 16.11.16.
 */

public class Repertoire implements IRepertoire {

    /*
    * Key Value Codes
    **/
    public static final String ARTIST_NAME = "artistName";
    public static final String TRACK_NAME = "trackName";

    public static final String KEY_SONG = "song";
    public static final String KEY_COUNTER= "counter";

    public static final String SHAREDPREF_KEY = "repertoire";

    private JSONArray repertoire;

    /*
    * Initializes a new repertoire where you cann add songs
    * */

    public Repertoire(){
            repertoire = new JSONArray();
    }

    public Repertoire(Repertoire repertoire) throws JSONException {
        this.repertoire = repertoire.getRepertoire();
    }

    public Repertoire(JSONArray repertoire) throws JSONException {
        String str = repertoire.toString();
        this.repertoire=new JSONArray(str);
    }

    /**
    * This method removes the song from the repertoire
    * @param song the song should be in the right format containing the counter.
    * */

    @Override
    public boolean removeSong(JSONObject song) throws JSONException {
        boolean isContained = false;
        JSONArray nRepertoire = new JSONArray();
        String songTitle = song.getJSONObject(KEY_SONG).getString(ARTIST_NAME) + " - " + song.getJSONObject(KEY_SONG).getString(TRACK_NAME);
        for (int i = 0; i < repertoire.length(); i++) {
            JSONObject repertoireSong = repertoire.getJSONObject(i).getJSONObject(KEY_SONG);
            String repertoireTitle = repertoireSong.getString(ARTIST_NAME) + " - " + repertoireSong.getString(TRACK_NAME);
            if (!repertoireTitle.equals(songTitle)) {
                nRepertoire.put(repertoire.get(i));
            }else{
                isContained = true;
            }
        }
        repertoire = nRepertoire;
        return isContained;
    }

    @Override
    public JSONObject getSong(String songFullName) throws JSONException {
        for (int i = 0; i < repertoire.length(); i++) {
            JSONObject repertoireSong = repertoire.getJSONObject(i).getJSONObject(KEY_SONG);
            String repertoireTitle = repertoireSong.getString(ARTIST_NAME) + " - " + repertoireSong.getString(TRACK_NAME);
            if (repertoireTitle.equals(songFullName)) {
                return repertoire.getJSONObject(i);
            }
        }
        return new JSONObject();
    }

    @Override
    public boolean removeSong(String songFullName) throws JSONException {
        return removeSong(getSong(songFullName));
    }

    /*
    * Adds a song as JSON to the repertoire if returned true, if the song is already contained, returns false.
    * */

    @Override
    public boolean addSong(JSONObject song) throws JSONException {
        String[] names = {ARTIST_NAME, TRACK_NAME};
        JSONObject tmp = new JSONObject();

        //JSON Object with two inner Objects: the song and the counter
        // The song key holds a JSONObject where only the needed attributes from the method parameter are copied
        tmp.put(KEY_SONG, new JSONObject(song, names))
                .put(KEY_COUNTER, 1);

        if(this.contains(tmp)>-1){
                return false;
        }
        repertoire.put(tmp);
        return true;

    }

    /*
    * Returns the index, if contained, otherwise -1. Ignores the counter
    * */
    @Override
    public int contains(JSONObject song) throws JSONException {
        int isContainedAtIndex = -1;
        //look if the array is containing the song
        for (int i = 0; i < repertoire.length(); i++) {
            if (repertoire.getJSONObject(i).getJSONObject(KEY_SONG).toString().equals(song.getJSONObject(KEY_SONG).toString())) {
                isContainedAtIndex = i;
                break;
            }
        }
        return isContainedAtIndex;
    }

    /*
    * Returns a streamable byte array from the repertoire
    * */


    @Override
    public byte[] makeStreamable() throws JSONException {
        byte[] streamable = repertoire.toString().getBytes();
        return streamable;
    }



    /*
    * Public getter for repertoire. Returns a copy
    * */
    @Override
    public JSONArray getRepertoire() throws JSONException {
        Repertoire copy = new Repertoire(this.repertoire);
        return copy.repertoire;
    }

    /*
    * Creates a Repertoire from a byte-Stream.
    * */


    @Override
    public void fromStream(byte[] input, int bytes) throws JSONException {
        String str = new String(input, 0, bytes);
        repertoire = new JSONArray(str);
    }


    /**
     * Gets a users Repertoire and iterates through each entry. If the current value is contained,
     * the counter for the song is raised by one, otherwise the song is added with counter = 1
     * */


    @Override
    public void mergeWithRepertoire(Repertoire userRepertoire) throws JSONException {
        JSONArray uRepertoire = userRepertoire.repertoire;
        for (int i = 0; i < uRepertoire.length(); i++) {
            JSONObject song = uRepertoire.getJSONObject(i);
            int containedAtIndex = this.contains(song);
            if(containedAtIndex==-1){
                repertoire.put(song); // song is not contained in own repertoire so new song can be imported as is with counter = 1
            }else{
                int counter = repertoire.getJSONObject(containedAtIndex).getInt(KEY_COUNTER);
                repertoire.getJSONObject(containedAtIndex).put(KEY_COUNTER, ++counter);
            }
        }
    }

    /**
     * Gets a users Repertoire and iterates through each entry. If the current value is contained and smaller or equals one, the song is removed
     * Otherwise the counter for the song is degraded by one.
     */

    @Override
    public void unmergeFromRepertoire(Repertoire userRepertoire) throws JSONException {
        JSONArray uRepertoire = userRepertoire.repertoire;
        JSONArray nRepertoire = new JSONArray();
        for (int i = 0; i < uRepertoire.length(); i++) {
            JSONObject song = uRepertoire.getJSONObject(i);
            //Check if current song is contained in this repertoire and get the index
            int containedAtIndex = this.contains(song);
            if(containedAtIndex == -1){
                break;
            }else if (this.repertoire.getJSONObject(containedAtIndex).getInt(KEY_COUNTER)<=1){
                this.removeSong(song);
            }else{
                int counter = this.repertoire.getJSONObject(containedAtIndex).getInt(KEY_COUNTER);
                this.repertoire.getJSONObject(containedAtIndex).put(KEY_COUNTER, --counter);
            }
        }
    }


    /**
     * Saves the complete JSONArray repertoire with counter persistently on the device.
     *@param sharedPref is a rreference to the SharedPreference Object you can maintain via ApplicationContext.
     *
     * */

    @Override
    public void saveOnDevice(SharedPreferences sharedPref){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SHAREDPREF_KEY, repertoire.toString());
        editor.commit();
    }

    /**
     * Loads the repertoire from the device.
     * @param sharedPref is a reference to the SharedPreference Object you can maintain via ApplicationContext.
     * */

    @Override
    public void loadFromDevice(SharedPreferences sharedPref) throws JSONException {
        String repertoireStr = sharedPref.getString(SHAREDPREF_KEY, new JSONArray().toString());
        this.repertoire = new JSONArray(repertoireStr);
    }
}
