package com.example.eatja.ui.dashboard;

import static java.sql.DriverManager.println;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";
    private String server = "http://172.10.5.130:80";
    private RecyclerView recyclerView;
    ArrayList<Integer> tagList = new ArrayList<>();
    String[] tagArray = new String[4];

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
        // dummy for now
        tagArray[0] = "한식";
        tagArray[1] = "양식";
        tagArray[2] = "일식";
        tagArray[3] = "중식";
        selectedTags = new boolean[tagArray.length];
        recFilterTagTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

                // set title
                builder.setTitle("태그를 선택하세요");

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(tagArray, selectedTags, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            tagList.add(i);
                            // Sort array list
                            Collections.sort(tagList);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            tagList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < tagList.size(); j++) {
                            // concat array value
                            stringBuilder.append(tagArray[tagList.get(j)]);
                            // check condition
                            if (j != tagList.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                            System.out.println(tagList.get(j));
                            RequestTagReview requestTagReview = new RequestTagReview(tagList.get(j).toString());
                            requestTagReview.start();
                        }
                        System.out.println(stringBuilder.toString());
                        // set text on textView

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedTags.length; j++) {
                            // remove all selection
                            selectedTags[j] = false;
                            // clear language list
                            tagList.clear();
                            // clear text view value
                            recFilterTagTV.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });


    }

    class RequestTagReview extends Thread {
        private String tagId;
        public RequestTagReview(String tagId){
            this.tagId = tagId;
        }
        @Override
        public void run() {
            try {
                URL url = new URL(serverUrl + "/tag-review");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn != null) {
                    android.util.Log.e("CHECK", "got connection");
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("tagId", tagId);

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