package com.example.eatja.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatja.R;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {
   private List<String> data;

   public DashboardAdapter(List<String> data) {
      this.data = data;
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
      String item = data.get(position);
      // Set the values to the views
      holder.titleTextView.setText(item);
      // Set other values as needed
   }


   @Override
   public int getItemCount() {
      return data.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {
      ImageView imageView;
      TextView titleTextView;
      TextView locationTextView;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         imageView = itemView.findViewById(R.id.recommend_item_image_view);
         titleTextView = itemView.findViewById(R.id.recommend_item_title);
         locationTextView = itemView.findViewById(R.id.recommend_item_location);
      }
   }

}
