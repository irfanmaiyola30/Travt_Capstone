package com.dicoding.travt.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dicoding.travt.R;
import com.dicoding.travt.model.FavoriteItem;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private Context context;
    private List<FavoriteItem> favoriteItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FavoriteItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FavoriteAdapter(Context context, List<FavoriteItem> favoriteItems) {
        this.context = context;
        this.favoriteItems = favoriteItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem item = favoriteItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private TextView name;
        private TextView city;
        private TextView rating;
        private TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.favorite_photo);
            name = itemView.findViewById(R.id.favorite_nama_tempat);
            city = itemView.findViewById(R.id.favorite_city);
            rating = itemView.findViewById(R.id.favorite_rating);
            description = itemView.findViewById(R.id.favorite_description);
        }

        public void bind(FavoriteItem item, OnItemClickListener listener) {
            Glide.with(itemView.getContext()).load(item.getPhotoUrl()).into(photo);
            name.setText(item.getNamaTempat());
            city.setText(item.getCity());
            rating.setText(String.valueOf(item.getRating()));
            description.setText(item.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
