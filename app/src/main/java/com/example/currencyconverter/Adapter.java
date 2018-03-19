package com.example.currencyconverter;

/**
 * Custom adapter used for to populate ListView from a HashMap in AllCurrenciesActivity
 */

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class Adapter extends BaseAdapter{
    private final ArrayList mapData;

    /**
     * Constructor
     * @param map the hashmap that should be in the ListView
     */
    public Adapter(HashMap<String, String> map){
        mapData = new ArrayList();
        mapData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mapData.size();
    }

    @Override
    public HashMap.Entry<String,String> getItem(int position) {
        return (HashMap.Entry) mapData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if(convertView == null){
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item,parent,false);
        }else{
            result = convertView;
        }

        HashMap.Entry<String,String> item = getItem(position);
        ((TextView) result.findViewById(android.R.id.text1)).setText(item.getKey());
        ((TextView) result.findViewById(android.R.id.text2)).setText(item.getValue());
        ((TextView) result.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
        ((TextView) result.findViewById(android.R.id.text1)).setTextSize(20);
        ((TextView) result.findViewById(android.R.id.text2)).setTextColor(Color.BLACK);
        ((TextView) result.findViewById(android.R.id.text2)).setTextSize(20);

        return result;
    }
}
