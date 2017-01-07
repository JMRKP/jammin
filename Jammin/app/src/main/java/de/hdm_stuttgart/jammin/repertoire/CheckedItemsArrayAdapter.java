package de.hdm_stuttgart.jammin.repertoire;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import de.hdm_stuttgart.jammin.R;

public class CheckedItemsArrayAdapter extends ArrayAdapter {

    public CheckedItemsArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        String viewClass = view.getClass().getName().toString();
        if ("android.widget.CheckedTextView".equals(viewClass)) {
            CheckedTextView ctv = (CheckedTextView) view;
            ctv.setCheckMarkDrawable(R.mipmap.delete);
            return ctv;
        } else {
            return view;
        }
    }
}