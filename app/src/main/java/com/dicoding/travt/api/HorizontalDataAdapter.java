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

public class HorizontalDataAdapter extends RecyclerView.Adapter<HorizontalDataAdapter.ViewHolder> {

    private List<ApiCaller.Data> itemList;
    private Context context;
    private OnItemClickListener mListener; // Listener for item click events

    public HorizontalDataAdapter(Context context, List<ApiCaller.Data> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApiCaller.Data data = itemList.get(position);
        holder.titleTextView.setText(data.name);
        holder.ratingTextView.setText(String.valueOf(data.rating));
        holder.locationTextView.setText(data.city); // Mengubah data.city sesuai kebutuhan
        // Load image using Picasso library
        Picasso.get().load(data.photo).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, ratingTextView, locationTextView;
        ImageView imageView, starImageView, locationLink;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationLink = itemView.findViewById(R.id.bottom_navigation);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            ratingTextView = itemView.findViewById(R.id.rating_text_view);
            locationTextView = itemView.findViewById(R.id.location_text_view);
            imageView = itemView.findViewById(R.id.image_view);
            starImageView = itemView.findViewById(R.id.star_image_view);

            // Set OnClickListener untuk imageView
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Panggil onItemClick dari mListener dan kirimkan data yang sesuai
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mListener != null) {
                        mListener.onItemClick(itemList.get(position));
                    }
                }
            });
        }
    }

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(ApiCaller.Data item);
    }

    // Method to set the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
