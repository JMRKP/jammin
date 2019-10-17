package de.hdm_stuttgart.jammin.server;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdm_stuttgart.jammin.R;

/**
 * Custom adapter to list the connected devices in the StartSessionActivity
 */

public class ConnectedDevicesAdapter extends BaseAdapter {


    private ArrayList<String> devices;
    private Context context;
    private LayoutInflater layoutInflater;
    private View.OnClickListener onClickListener;

    public ConnectedDevicesAdapter(Context context, View.OnClickListener onClickListener, ArrayList<String> devices){
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.devices = devices;
        this.onClickListener = onClickListener;
    }


    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ConnectedDevicesAdapter.ViewHolder viewHolder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.connected_devices_list_item, parent, false);
            viewHolder = new ConnectedDevicesAdapter.ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ConnectedDevicesAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.deviceName.setText(devices.get(position));
        viewHolder.removeClientButton.setOnClickListener(onClickListener);

        return convertView;
    }

    private class ViewHolder {

        TextView deviceName;
        Button removeClientButton;

        ViewHolder(View view){
            deviceName = (TextView) view.findViewById(R.id.deviceName);
            removeClientButton = (Button) view.findViewById(R.id.remove_client_button);
        }
    }
}
