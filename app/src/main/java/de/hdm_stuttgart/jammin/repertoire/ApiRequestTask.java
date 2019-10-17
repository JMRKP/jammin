package de.hdm_stuttgart.jammin.repertoire;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class ApiRequestTask extends AsyncTask<URL, Integer, JSONArray> {
    private ApiRequestListener mListener;

    public void onDoneLoading(ApiRequestListener listener) {
        mListener = listener;
    }

    @Override
    protected JSONArray doInBackground(URL... urls) {
        for (int i = 0; i < urls.length; i++) {
            HttpURLConnection httpConnection = null;
            Log.d("ITUNES_REQUEST", "Starting request to iTunes Search API...");
            try {
                httpConnection = (HttpURLConnection) urls[i].openConnection();
                try {
                    httpConnection.setRequestMethod("GET");
                    httpConnection.setRequestProperty("Accept", "application/json");
                    Log.d("ITUNES_REQUEST", "API Request Parameters successfully set!");

                    int httpResponseCode = httpConnection.getResponseCode();
                    Log.d("ITUNES_REQUEST", "API Response Code = " + httpResponseCode);

                    if (httpResponseCode == 200) {
                        BufferedReader responseReader = new BufferedReader(new InputStreamReader(
                                httpConnection.getInputStream()));

                        String responseLine;
                        StringBuffer response = new StringBuffer();

                        while ((responseLine = responseReader.readLine()) != null) {
                            response.append(responseLine + "\n");
                        }
                        responseReader.close();

                        return parseJsonAndGetResultsArray(response.toString());
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JSONArray();
    }

    @Override
    protected void onPostExecute(JSONArray songs) {
        if (mListener != null) {
            mListener.onDone(songs);
        }
    }

    protected JSONArray parseJsonAndGetResultsArray(String json) {
        try {
            JSONObject responseJSON = new JSONObject(json);
            JSONArray resultsArray = responseJSON.getJSONArray("results");
            return resultsArray;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public interface ApiRequestListener {
        void onDone(JSONArray songs);
    }
}
