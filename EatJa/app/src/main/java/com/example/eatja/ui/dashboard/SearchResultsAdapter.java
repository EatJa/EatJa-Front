package com.example.eatja.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eatja.R;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

   private List<String> userNameView;
   private List<String> userProfileView;
   private List<String> userFollowerView;
   private List<String> userFolloweeView;


   public SearchResultsAdapter(List<String> userNameView, List<String> userProfileView, List<String> userFollowerView, List<String> userFolloweeView) {
      this.userNameView = userNameView;
      this.userProfileView = userProfileView;
      this.userFollowerView = userFollowerView;
      this.userFolloweeView = userFolloweeView;
   }

   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      // Inflate your search result item layout here
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_result_layout, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      String name = userNameView.get(position);
      holder.nameView.setText(name);

      String img = userProfileView.get(position);
      Glide.with(holder.profileView.getContext()).load(img).into(holder.profileView);

      String follower = userFollowerView.get(position);
      holder.followerView.setText("팔로워 " +follower);

      String folllowee =userFolloweeView.get(position);
      holder.followeeView.setText("팔로잉 "+ folllowee);
   }

   @Override
   public int getItemCount() {
      return userNameView.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {
      TextView nameView;
      ImageView profileView;
      TextView followerView;
      TextView followeeView;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         nameView = itemView.findViewById(R.id.user_name);
         profileView = itemView.findViewById(R.id.user_profile_image);
         followerView = itemView.findViewById(R.id.user_follower);
         followeeView = itemView.findViewById(R.id.user_followee);
      }
   }
}