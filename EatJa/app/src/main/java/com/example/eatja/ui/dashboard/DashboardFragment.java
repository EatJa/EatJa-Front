package com.example.eatja.ui.dashboard;

import static java.sql.DriverManager.println;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatja.MainActivity;
import com.example.eatja.R;
import com.example.eatja.databinding.FragmentDashboardBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private MainActivity mainActivity;
    private TextView recFilterTagTV;
    private boolean[] selectedTags;

    private JSONObject currentUser;

    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";
    private String server = "http://172.10.5.130:80";
    private RecyclerView recyclerView;
    private SearchView searchView;
    private LinearLayout searchResultsLayout;
    private RecyclerView searchResultsRecyclerView;
    ArrayList<Integer> tagList = new ArrayList<>();
    String[] tagArray = new String[6];
    private Integer selectedTag = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.bind(view);

        // Set up the RecyclerView
        recyclerView = binding.recSV;
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        RequestAllReview requestAllReview = new RequestAllReview();
        requestAllReview.start();

        // tag filter text view
        recFilterTagTV = binding.recFilterTagTV;

        tagArray[0] = "한식";
        tagArray[1] = "양식";
        tagArray[2] = "중식";
        tagArray[3] = "일식";
        tagArray[4] = "카페";
        tagArray[5] = "기타";
        selectedTags = new boolean[tagArray.length];

        searchResultsLayout = view.findViewById(R.id.searchResultsLayout);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

        searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
        recFilterTagTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

                // set title
                builder.setTitle("태그를 선택하세요");

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setSingleChoiceItems(tagArray, selectedTag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTag = which;
                    }
                });

                builder.setPositiveButton("완료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // set text on textView
                        recFilterTagTV.setText(tagArray[selectedTag]);

                        RequestTagReview requestTagReview = new RequestTagReview(selectedTag.toString());
                        requestTagReview.start();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        selectedTag = -1;
                        dialogInterface.dismiss();
                    }
                });
                // show dialog
                builder.show();
            }
        });



    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // If the query is empty, hide the search results popup window
            hideSearchResults();
        } else {
            RequestSearchUser requestSearchUser = new RequestSearchUser(query);
            requestSearchUser.start();
        }
    }


    private void showSearchResults() {
        searchResultsLayout.setVisibility(View.VISIBLE);
    }

    private void hideSearchResults() {
        searchResultsLayout.setVisibility(View.GONE);
    }

    class RequestSearchUser extends Thread {
        private String userName;

        public RequestSearchUser(String userName) {
            this.userName = userName;
        }

        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl + "/search-user?userName="+userName);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    android.util.Log.e("CHECK", "got connection");
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    //conn.setDoOutput(true);
                    System.out.println(userName);


                    int resCode = conn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {

                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            System.out.println(response.toString());

                            JSONObject jsonObject = new JSONObject(response.toString());
                            String user = jsonObject.getString("user");
                            JSONObject ubJson = new JSONObject(user);

                            List<String> userIds = new ArrayList<>();
                            List<String> userNames = new ArrayList<>();
                            List<String> profileImgs = new ArrayList<>();
                            List<String> followerCounts = new ArrayList<>();
                            List<String> followeeCounts = new ArrayList<>();

                            String userId = ubJson.getString("userId");
                            String userName = ubJson.getString("userName");
                            String profileImg = ubJson.getString("profileImg");
                            String followerCount = ubJson.getString("followerCount");
                            String followeeCount = ubJson.getString("followeeCount");

                            userIds.add(userId);
                            userNames.add(userName);
                            profileImgs.add(profileImg);
                            followerCounts.add(followerCount);
                            followeeCounts.add(followeeCount);

                            currentUser = mainActivity.getJsonObject();
                            String currentUserId = currentUser.getString("userId");

                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the UI to display search results
                                    if (userNames != null && userNames.size() > 0) {
                                        // Display the search results popup window
                                        showSearchResults();
                                        // Update the adapter with search results
                                        SearchResultsAdapter adapter = new SearchResultsAdapter(userIds, userNames, profileImgs, followerCounts, followeeCounts, currentUserId);
                                        searchResultsRecyclerView.setAdapter(adapter);
                                    } else {
                                        // Hide the search results popup window if no results found
                                        hideSearchResults();
                                    }
                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("ERROR", e.toString());
                e.printStackTrace();
            }
        }
    }

    class RequestTagReview extends Thread {
        private String tagId;
        public RequestTagReview(String tagId){
            this.tagId = tagId;
        }
        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl + "/tag-review?tagId="+tagId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    android.util.Log.e("CHECK", "got connection");
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    //conn.setDoOutput(true);
                    System.out.println(tagId);


                    int resCode = conn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {

                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            System.out.println(response.toString());

                            JSONObject jsonObject = new JSONObject(response.toString());
                            String reviews = jsonObject.getString("reviews");
                            JSONArray jsonArray = new JSONArray(reviews);
                            List<String> reviewTitles = new ArrayList<>();
                            List<String> reviewerNames = new ArrayList<>();
                            List<String> imgUrls = new ArrayList<>();
                            List<String> descriptions = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject subJsonObject = jsonArray.getJSONObject(i);

                                String reviewName = subJsonObject.getString("reviewName");
                                String reviewerName = subJsonObject.getString("reviewerName");
                                String imgUrl = subJsonObject.getString("imgUrl");
                                String description = subJsonObject.getString("description");

                                reviewTitles.add(reviewName);
                                reviewerNames.add(reviewerName);
                                imgUrls.add(server + imgUrl);
                                System.out.println(server + imgUrl);
                                descriptions.add(description);
                            }

                            DashboardAdapter adapter = new DashboardAdapter(reviewTitles, reviewerNames, imgUrls, descriptions);
                            recyclerView.setAdapter(adapter);
                        }
                    }

                }
            } catch (Exception e) {
                android.util.Log.e("ERROR", e.toString());
                e.printStackTrace();
            }
        }
    }

    class RequestAllReview extends Thread {
        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl + "/all-review");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn != null) {
                    android.util.Log.e("CHECK", "got connection");
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    //conn.setDoOutput(true);

                    int resCode = conn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader br = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }

                            JSONObject jsonObject = new JSONObject(response.toString());
                            String reviews = jsonObject.getString("reviews");
                            JSONArray jsonArray = new JSONArray(reviews);
                            List<String> reviewTitles = new ArrayList<>();
                            List<String> reviewerNames = new ArrayList<>();
                            List<String> imgUrls = new ArrayList<>();
                            List<String> descriptions = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject subJsonObject = jsonArray.getJSONObject(i);

                                String reviewName = subJsonObject.getString("reviewName");
                                String reviewerName = subJsonObject.getString("reviewerName");
                                String imgUrl = subJsonObject.getString("imgUrl");
                                String description = subJsonObject.getString("description");

                                reviewTitles.add(reviewName);
                                reviewerNames.add(reviewerName);
                                imgUrls.add(server+imgUrl);
                                System.out.println(server+imgUrl);
                                descriptions.add(description);
                            }

                            // Set the dummy data to the adapter
                            DashboardAdapter adapter = new DashboardAdapter(reviewTitles, reviewerNames, imgUrls, descriptions);
                            recyclerView.setAdapter(adapter);

                        }
                    }
                }

            } catch (Exception e) {
                android.util.Log.e("ERROR", e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}