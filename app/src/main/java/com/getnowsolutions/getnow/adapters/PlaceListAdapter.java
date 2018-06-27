package com.getnowsolutions.getnow.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getnowsolutions.getnow.R;
import com.getnowsolutions.getnow.interfaces.ListInteractionListner;
import com.getnowsolutions.getnow.models.Place;

import java.util.ArrayList;

/**
 * Created by dilip on 2/10/17.
 */

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {
    ArrayList<Place> places;
    ListInteractionListner listner;

    public PlaceListAdapter(ArrayList<Place> places, ListInteractionListner listner){
        this.places = places;
        this.listner = listner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.places_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.placeText.setText(places.get(position).placeAddress);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onListClicked(places.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView placeText;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            placeText = (TextView) view.findViewById(R.id.placeText);
        }
    }
}
