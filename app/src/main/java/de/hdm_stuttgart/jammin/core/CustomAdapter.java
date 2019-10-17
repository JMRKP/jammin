package de.hdm_stuttgart.jammin.core;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.hdm_stuttgart.jammin.R;
import de.hdm_stuttgart.jammin.repertoire.Repertoire;

/**
 * The CustomAdapter connects the shared repertoire with the view both in the server and in the client.
 *
 */

public class CustomAdapter extends BaseAdapter {

    private ArrayList<Map<JSONObject, Integer>> repertoire;
    private Context context;
    private LayoutInflater layoutInflater;

    public CustomAdapter(Context context, ArrayList<Map<JSONObject, Integer>> sharedRepertoire){
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.repertoire = sharedRepertoire;
    }

    @Override
    public int getCount() {
        return repertoire.size();
    }

    @Override
    public Object getItem(int position) {

        return repertoire.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.session_repertoire_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Map<JSONObject, Integer> song = (Map<JSONObject, Integer>) getItem(position);

        try {
            viewHolder.artistname.setText(song.keySet().iterator().next().getString(Repertoire.ARTIST_NAME));
            viewHolder.trackname.setText(song.keySet().iterator().next().getString(Repertoire.TRACK_NAME));
            viewHolder.counter.setText(Integer.toString(song.values().iterator().next()));
        } catch (JSONException e) {
            Log.d("JSON_ERROR", "Could not set listview in SessioRepertoireActivity.");
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {

        TextView artistname;
        TextView trackname;
        TextView counter;

        ViewHolder(View view){
            artistname = (TextView) view.findViewById(R.id.artistName);
            trackname = (TextView) view.findViewById(R.id.trackName);
            counter = (TextView) view.findViewById(R.id.counter);
        }
    }
}
