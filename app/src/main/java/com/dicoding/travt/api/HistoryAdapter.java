package com.dicoding.travt.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dicoding.travt.R;
import com.dicoding.travt.ReviewItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ReviewViewHolder> {

    private final List<ReviewItem> reviews;

    public HistoryAdapter(List<ReviewItem> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCreatedAt;
        private final TextView namaTempat;
        private final TextView tvRatingPlace;
        private final ImageView photo;
        private final TextView namaReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCreatedAt = itemView.findViewById(R.id.text_review_date);
            namaTempat = itemView.findViewById(R.id.text_review_title);
            tvRatingPlace = itemView.findViewById(R.id.rating); // Pastikan ID yang benar
            photo = itemView.findViewById(R.id.photo);
            namaReview = itemView.findViewById(R.id.nama_review); // TextView untuk nama pengguna
        }

        public void bind(ReviewItem review) {
            // Mengatur nilai ke TextViews dan ImageView
            // Misalnya, mengubah format tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            try {
                Date date = sdf.parse(review.getCreatedAt());
                String formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date);
                tvCreatedAt.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                tvCreatedAt.setText(review.getCreatedAt()); // Jika parsing gagal, gunakan string asli
            }

            namaTempat.setText(review.getNamaTempat());
            tvRatingPlace.setText("" + review.getRatingUser());

            // Memuat foto menggunakan Glide atau metode lainnya
            if (review.getPhoto() != null && !review.getPhoto().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(review.getPhoto())
                        .placeholder(R.drawable.logo_travt) // Gambar placeholder
                        .error(R.drawable.logo_travt) // Gambar jika terjadi error
                        .into(photo);
            } else {
                photo.setImageResource(R.drawable.logo_travt); // Gambar default jika URL kosong atau error
            }

            // Menggunakan nama pengguna dari objek ReviewItem
            namaReview.setText(review.getReviewerName() != null ? review.getReviewerName() : "Anonymous");
        }
    }
}

