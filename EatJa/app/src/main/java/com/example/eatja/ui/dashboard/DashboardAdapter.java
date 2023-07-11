package com.example.eatja.ui.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.eatja.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
   private List<String> titleView;
   private List<String> reviewerView;
   private List<String> imgView;
   private List<String> descriptionView;


   public DashboardAdapter(List<String> titleView, List<String> reviewerView, List<String> imgView , List<String> description) {
      this.titleView = titleView;
      this.reviewerView = reviewerView;
      this.imgView = imgView;
      this.descriptionView = description;
   }

   // ViewHolder and other necessary methods

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_card_list, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      String title = titleView.get(position);
      holder.titleTextView.setText(title);

      String reviewer = reviewerView.get(position);
      holder.reviewerTextView.setText(reviewer);

      String img = imgView.get(position);
      Glide.with(holder.imageView.getContext()).load(img).into(holder.imageView);

      String description = descriptionView.get(position);
      holder.descriptionTextView.setText(description);
   }


   @Override
   public int getItemCount() {
      return titleView.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {
      ImageView imageView;
      TextView titleTextView;
      TextView descriptionTextView;
      TextView reviewerTextView;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         imageView = itemView.findViewById(R.id.recommend_item_image_view);
         titleTextView = itemView.findViewById(R.id.recommend_item_title);
         descriptionTextView = itemView.findViewById(R.id.recommend_item_description);
         reviewerTextView = itemView.findViewById(R.id.recommend_item_writer);
      }
   }

}
