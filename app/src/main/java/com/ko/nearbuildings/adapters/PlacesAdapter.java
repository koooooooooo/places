package com.ko.nearbuildings.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.ko.nearbuildings.R;
import com.ko.nearbuildings.net.Result;

import java.util.ArrayList;
import java.util.List;

public class PlacesAdapter extends BaseAdapter {

    private List<Result> places = new ArrayList<>();
    private LayoutInflater inflater;

    public PlacesAdapter(Context context, List<Result> results) {
        this.inflater = LayoutInflater.from(context);
        this.places.addAll(results);
    }

    @Override
    public int getCount() {
        return this.places.size();
    }

    @Override
    public Object getItem(int i) {
        return places.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static class ViewHolder {
        private TextView ownerName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.places_item, viewGroup, false);
            viewHolder = new ViewHolder();
            initViews(view, viewHolder);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        init(viewHolder, places.get(position));
        return view;
    }

    private void initViews(View view, ViewHolder viewHolder) {
        viewHolder.ownerName = (TextView) view.findViewById(R.id.owner_name);
    }

    private void init(ViewHolder viewHolder, Result place) {

    }

    public void notifyUpdates(List<Result> data) {
        places.clear();
        places.addAll(data);
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

}