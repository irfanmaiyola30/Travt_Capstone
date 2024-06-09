package com.dicoding.travt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dicoding.travt.R;
import com.dicoding.travt.model.Favorite;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    private List<Favorite> favoriteList = new ArrayList<>();

    public void setFavoriteList(List<Favorite> favoriteList) {
        if (favoriteList != null) {
            this.favoriteList = favoriteList;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite favorite = favoriteList.get(position);
        if (favorite != null) {
            holder.namaTempatTextView.setText(favorite.getName());
            holder.cityTextView.setText(favorite.getCity());
            holder.ratingTextView.setText(String.valueOf(favorite.getRating()));
            holder.descriptionTextView.setText(favorite.getDescription());
            String photoUrl = favorite.getPhoto();
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Picasso.get().load(photoUrl)
                        .placeholder(R.drawable.awal_photo)
                        .error(R.drawable.baseline_person_outline_24)
                        .into(holder.photoImageView);
            } else {
                holder.photoImageView.setImageResource(R.drawable.baseline_person_outline_24); // Ganti dengan placeholder yang sesuai
            }
        }
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {

        ImageView photoImageView;
        TextView namaTempatTextView, cityTextView, ratingTextView, descriptionTextView;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.favorite_photo);
            namaTempatTextView = itemView.findViewById(R.id.favorite_nama_tempat);
            cityTextView = itemView.findViewById(R.id.favorite_city);
            ratingTextView = itemView.findViewById(R.id.favorite_rating);
            descriptionTextView = itemView.findViewById(R.id.favorite_description);
        }
    }
}
