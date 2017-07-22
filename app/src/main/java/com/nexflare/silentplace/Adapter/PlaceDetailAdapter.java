package com.nexflare.silentplace.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nexflare.silentplace.Pojo.PlaceDetail;
import com.nexflare.silentplace.R;

import java.util.ArrayList;

/**
 * Created by 15103068 on 22-07-2017.
 */

public class PlaceDetailAdapter extends RecyclerView.Adapter<PlaceDetailAdapter.PlaceViewHolder> {

    ArrayList<PlaceDetail> mPlaceDetailArrayList;
    Context mContext;

    public PlaceDetailAdapter(ArrayList<PlaceDetail> placeDetailArrayList, Context context) {
        mPlaceDetailArrayList = placeDetailArrayList;
        mContext = context;
    }

    public void updateArray(ArrayList<PlaceDetail> placeDetailArrayList){
        mPlaceDetailArrayList=placeDetailArrayList;
        notifyDataSetChanged();
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v=inflater.inflate(R.layout.layout_place,parent,false);
        return new PlaceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        PlaceDetail detail=mPlaceDetailArrayList.get(position);
        holder.tvPlaceName.setText(detail.getName());
    }

    @Override
    public int getItemCount() {
        return mPlaceDetailArrayList.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder{
        TextView tvPlaceName;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            tvPlaceName= (TextView) itemView.findViewById(R.id.tvName);
        }
    }

}


