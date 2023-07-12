package com.example.eatja.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eatja.MainActivity;
import com.example.eatja.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
   private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";

   private String currentUserId;
   private List<String> userId;
   private List<String> userNameView;
   private List<String> userProfileView;
   private List<String> userFollowerView;
   private List<String> userFolloweeView;


   public SearchResultsAdapter(List<String> userId, List<String> userNameView, List<String> userProfileView, List<String> userFollowerView, List<String> userFolloweeView, String currentUserId) {
      this.userId = userId;
      this.userNameView = userNameView;
      this.userProfileView = userProfileView;
      this.userFollowerView = userFollowerView;
      this.userFolloweeView = userFolloweeView;
      this.currentUserId = currentUserId;
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

      String user = userId.get(position);

      String name = userNameView.get(position);
      holder.nameView.setText(name);

      String img = userProfileView.get(position);
      Glide.with(holder.profileView.getContext()).load(img).into(holder.profileView);

      String follower = userFollowerView.get(position);
      holder.followerView.setText("팔로워 " +follower);

      String followee =userFolloweeView.get(position);
      holder.followeeView.setText("팔로잉 "+ followee);

      holder.btnFollow.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            System.out.println(currentUserId);
            System.out.println(user);
            RequestFollow requestFollow = new RequestFollow(currentUserId, user);
            requestFollow.start();

            Toast.makeText(v.getContext(), "팔로우 성공 ", Toast.LENGTH_SHORT).show();
         }
      });

      holder.btnUnfollow.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            RequestUnFollow requestUnFollow = new RequestUnFollow(currentUserId, user);
            requestUnFollow.start();

            Toast.makeText(v.getContext(), "언팔로우 성공 " , Toast.LENGTH_SHORT).show();
         }
      });
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

      Button btnFollow;
      Button btnUnfollow;

      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         nameView = itemView.findViewById(R.id.user_name);
         profileView = itemView.findViewById(R.id.user_profile_image);
         followerView = itemView.findViewById(R.id.user_follower);
         followeeView = itemView.findViewById(R.id.user_followee);
         btnFollow = itemView.findViewById(R.id.btn_follow);
         btnUnfollow = itemView.findViewById(R.id.btn_unfollow);
      }
   }


   class RequestFollow extends Thread {
      private String followerId;
      private String followeeId;

      public RequestFollow(String followerId, String followeeId) {
         this.followerId = followerId;
         this.followeeId = followeeId;
      }

      @Override
      public void run() {
         try {
            URL url = new URL(serverUrl + "/follow");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if(conn != null) {
               android.util.Log.e("CHECK", "got connection");
               conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
               conn.setRequestMethod("POST"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
               conn.setRequestProperty("Content-Type", "application/json");
               conn.setRequestProperty("Accept", "application/json");
               conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
               conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

               JSONObject jsonObject = new JSONObject();
               jsonObject.put("followerId", followerId);
               jsonObject.put("followeeId", followeeId);
               System.out.println(jsonObject.toString());

               try(OutputStream os = conn.getOutputStream()) {
                  byte[] input = jsonObject.toString().getBytes("utf-8");
                  os.write(input, 0, input.length);
               }
               // read response
               try(BufferedReader br = new BufferedReader(
                       new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                  StringBuilder response = new StringBuilder();
                  String responseLine = null;
                  while ((responseLine = br.readLine()) != null) {
                     response.append(responseLine.trim());
                  }
                  System.out.println(response.toString());
               }

            }

         } catch (Exception e) {
            android.util.Log.e("ERROR", e.toString());
            e.printStackTrace();
         }
      }
   }

   class RequestUnFollow extends Thread {
      private String followerId;
      private String followeeId;

      public RequestUnFollow(String followerId, String followeeId) {
         this.followerId = followerId;
         this.followeeId = followeeId;
      }

      @Override
      public void run() {
         try {
            URL url = new URL(serverUrl + "/follow?followerId="+followerId+"&followeeId="+followeeId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if(conn != null) {
               android.util.Log.e("CHECK", "got connection");
               conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
               conn.setRequestMethod("DELETE"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
               conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
               //conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

               int resCode = conn.getResponseCode();
               if(resCode == HttpURLConnection.HTTP_OK){
                  try(BufferedReader br = new BufferedReader(
                          new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                     StringBuilder response = new StringBuilder();
                     String responseLine = null;
                     while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                     }
                     System.out.println(response.toString());
                  }
               }

            }

         } catch (Exception e) {
            android.util.Log.e("ERROR", e.toString());
            e.printStackTrace();
         }
      }
   }
}