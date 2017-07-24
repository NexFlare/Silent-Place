package com.nexflare.silentplace.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nexflare.silentplace.Interface.DeleteItem;
import com.nexflare.silentplace.Model.PlaceDetail;
import com.nexflare.silentplace.R;

import java.util.ArrayList;

/**
 * Created by 15103068 on 22-07-2017.
 */

public class PlaceDetailAdapter extends RecyclerView.Adapter<PlaceDetailAdapter.PlaceViewHolder> {

    ArrayList<PlaceDetail> mPlaceDetailArrayList;
    Context mContext;
    DeleteItem deleteItemInterface;

    public PlaceDetailAdapter(ArrayList<PlaceDetail> placeDetailArrayList, Context context, DeleteItem deleteItemInterface) {
        mPlaceDetailArrayList = placeDetailArrayList;
        mContext = context;
        this.deleteItemInterface = deleteItemInterface;
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
    public void onBindViewHolder(final PlaceViewHolder holder, int position) {
        final PlaceDetail detail=mPlaceDetailArrayList.get(position);
        holder.tvPlaceName.setText(detail.getName());
        holder.tvInitial.setText(detail.getName().substring(0,1));
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(mContext)
                        .setMessage("Do you want to delete "+detail.getName()+" ?")
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteItemInterface.onItemSelected(detail.getName());
                            }
                        });
                AlertDialog dialog=builder.create();
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaceDetailArrayList.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder{
        TextView tvPlaceName;
        TextView tvInitial;
        View view;
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/round-elegance.ttf");

        public PlaceViewHolder(View itemView) {
            super(itemView);
            tvPlaceName= (TextView) itemView.findViewById(R.id.tvName);
            tvInitial= (TextView) itemView.findViewById(R.id.tvInitial);
            tvPlaceName.setTypeface(typeface);
            view=itemView;
        }
    }

}


