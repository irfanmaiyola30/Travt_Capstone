package com.dicoding.travt.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dicoding.travt.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private List<ApiCaller.Data> dataList;
    private Context context;
    private OnItemClickListener mListener;

    public DataAdapter(Context context, List<ApiCaller.Data> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vertikal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiCaller.Data data = dataList.get(position);
        holder.name.setText(data.name);
        holder.city.setText(data.city);
        holder.rating.setText(String.valueOf(data.rating));
        Picasso.get().load(data.photo).into(holder.photo);

        // Tambahkan listener klik pada item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(data);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public Iterable<? extends ApiCaller.Data> getData() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, city, rating;
        ImageView photo, starIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            city = itemView.findViewById(R.id.city);
            rating = itemView.findViewById(R.id.rating);
            photo = itemView.findViewById(R.id.photo);
            starIcon = itemView.findViewById(R.id.star_icon);
        }
    }
    public void filterList(List<ApiCaller.Data> filteredList) {
        dataList = filteredList;
        notifyDataSetChanged();
    }

    // Interface untuk menangani klik item
    public interface OnItemClickListener {
        void onItemClick(ApiCaller.Data item);
    }

    // Metode untuk mengatur listener klik item
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
