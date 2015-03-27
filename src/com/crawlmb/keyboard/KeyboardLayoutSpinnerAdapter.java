package com.crawlmb.keyboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.crawlmb.Preferences;
import com.crawlmb.R;

/**
 * Created by michael on 25/03/15.
 */
public class KeyboardLayoutSpinnerAdapter extends BaseAdapter{
    @Override
    public int getCount() {
        int layoutCount = Preferences.getLayoutCount();
        return layoutCount + 1;
    }

    @Override
    public Object getItem(int i) {
        // Might return something else here?
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        String text;
        if (position == 0){
            text = parent.getResources().getString(R.string.default_layout);
        }else{
            text = parent.getResources().getString(R.string.custom_layout);
        }
        ((TextView) convertView).setText(text);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        String text;
        if (position == 0){
            text = parent.getResources().getString(R.string.default_layout);
        }else{
            text = parent.getResources().getString(R.string.custom_layout);
        }
        ((TextView) convertView).setText(text);
        return convertView;
    }
}
