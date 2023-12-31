package com.example.eatja.ui.home;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.eatja.MainActivity;
import com.example.eatja.NewReviewActivity;
import com.example.eatja.R;
import com.example.eatja.databinding.FragmentHomeBinding;
import com.example.eatja.search.DataCallback;
import com.example.eatja.search.SearchLocal;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import okio.AsyncTimeout;

public class HomeFragment extends Fragment implements OnMapReadyCallback, DataCallback {

    private MainActivity mainActivity;
    private FragmentHomeBinding binding;
    private FragmentManager fm;
    private MapFragment mapFragment;
    private NaverMap naverMap;
    private UiSettings uiSettings;
    private LocationOverlay locationOverlay;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private androidx.appcompat.widget.SearchView searchView;
    private Button myEatBtnPressed, myEatBtnNotPressed;
    private Button myFollowBtnPressed, myFollowBtnNotPressed;
    private boolean[] selectedFollows, selectedTags;
    ArrayList<Integer> followList = new ArrayList<>();
    ArrayList<Integer> tagList = new ArrayList<>();
    String[] followArray = new String[4];
    String[] tagArray = {"한식", "양식", "중식", "일식", "카페", "기타"};
    private JSONObject jsonObject;
    private ArrayList<Marker> markerArrayList = new ArrayList<>();  // for search markers
    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";
    private JSONArray myReviewsJsonArray = new JSONArray();
    private ArrayList<Marker> myReviewsMarkerArray = new ArrayList<>(); // for my review markers
    private Dialog reviewDialog;
    private final Integer GET_MYREVIEW_CODE = 1;
    private final Integer GET_FOLLOWEEINFO_CODE = 2;
    private final Integer GET_FOLLOWEE_REVIEW_CODE = 3;
    private final Integer GET_LAST_FOLLOWEE_REVIEW_CODE = 4;
    private JSONArray myFolloweeJsonArray = new JSONArray();    // 내가 팔로우 하는 사람들
    private ArrayList<JSONObject> allReviewsJsonArray = new ArrayList<>();    // 내가 팔로우 하는 사람들의 모든 리뷰!
    private ArrayList<Marker> allReviewsMarkerArray = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
        reviewDialog = new Dialog(mainActivity);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // buttons
        myEatBtnPressed = binding.myEatBtnPressed;
        myEatBtnNotPressed = binding.myEatBtnNotPressed;
        myEatBtnPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myEatBtnPressed.getVisibility() == View.VISIBLE) {
                    myEatBtnPressed.setVisibility(View.INVISIBLE);
                    myEatBtnNotPressed.setVisibility(View.VISIBLE);

                    hideMyReviewMarkers();
                }
            }
        });
        myEatBtnNotPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myEatBtnNotPressed.getVisibility() == View.VISIBLE) {
                    myEatBtnPressed.setVisibility(View.VISIBLE);
                    myEatBtnNotPressed.setVisibility(View.INVISIBLE);

                    // user profile json
                    if (jsonObject == null) {
                        jsonObject = mainActivity.getJsonObject();
                    }
                    // get my review markers
                    getMyReviewMarkers();

                }
            }
        });

        // buttons
        myFollowBtnPressed = binding.followEatBtnPressed;
        myFollowBtnNotPressed = binding.followEatBtnNotPressed;
        myFollowBtnPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myFollowBtnPressed.getVisibility() == View.VISIBLE) {
                    myFollowBtnPressed.setVisibility(View.INVISIBLE);
                    myFollowBtnNotPressed.setVisibility(View.VISIBLE);

                    hideFolloweeReviewMarkers();
                }
            }
        });
        myFollowBtnNotPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myFollowBtnNotPressed.getVisibility() == View.VISIBLE) {
                    myFollowBtnPressed.setVisibility(View.VISIBLE);
                    myFollowBtnNotPressed.setVisibility(View.INVISIBLE);

                    // user profile json
                    if (jsonObject == null) {
                        jsonObject = mainActivity.getJsonObject();
                    }
                    // get followees' review markers
                    getFolloweeReviewMarkers();

                }
            }
        });

        // search bar setting
        searchView = binding.searchView;
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            SearchLocal searchLocal = new SearchLocal();
            @Override
            public boolean onQueryTextSubmit(String query) {

                new Thread(() -> {
                    // search query
                    try {
                        String response = searchLocal.search(query);
                        android.util.Log.i("QUERY", response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray items = jsonObject.getJSONArray("items");
                            for (int i = 0 ; i < items.length() ; i++) {
                                JSONObject item = items.getJSONObject(i);
                                android.util.Log.i("Item!", item.toString());
                            }

                            // Call displaySearchedMarkers on the main thread
                            mainActivity.runOnUiThread(() -> displaySearchedMarkers(items));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // get just single map fragment
        fm = getChildFragmentManager();
        mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        mapFragment.getMapAsync(this);

        return root;
    }

    @Override
    public void onMapReady(@NonNull NaverMap nMap) {
        naverMap = nMap;

        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BICYCLE, false);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_CADASTRAL, false);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, false);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRAFFIC, false);
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, false);

        uiSettings = naverMap.getUiSettings();
        setUiSettings();

        if (!locationSource.isActivated()) { // 권한 거부됨
            naverMap.setLocationTrackingMode(LocationTrackingMode.None);
        }
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                hideSearchedMarkers();
            }
        });

        // 위치 overlay (딱 하나)
        locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

    }

    public void setUiSettings() {
        uiSettings.setScaleBarEnabled(true);
    }

    public void getMyReviewMarkers() {
        RequestThread requestThread = new RequestThread();
        requestThread.start();
    }

    public void getFolloweeReviewMarkers() {
        RequestThreadFollow requestThreadFollow = new RequestThreadFollow();
        requestThreadFollow.start();
    }

    public void getAllMarkersOfFollowees() {
        RequestThreadAll requestThreadAll = new RequestThreadAll();
        requestThreadAll.start();
    }

    public void showMyReviewMarkers() {
        if (myReviewsMarkerArray.size() == myReviewsJsonArray.length()) {
            for (int i = 0 ; i < myReviewsMarkerArray.size() ; i ++) {
                myReviewsMarkerArray.get(i).setMap(naverMap);
            }
        } else {
            for (int i = 0 ; i < myReviewsJsonArray.length() ; i++) {
                JSONObject item = new JSONObject();
                try {
                    item = myReviewsJsonArray.getJSONObject(i);

                    String latLngString = item.getString("locationUrl");
                    // if not correct LatLng format
                    if (!latLngString.contains("LatLng")) {
                        continue;
                    }
                    // Extract the latitude and longitude values from the string
                    String[] latLngParts = latLngString
                            .replace("LatLng{", "")
                            .replace("}", "")
                            .split(",");

                    double latitude = Double.parseDouble(latLngParts[0].replace("latitude=", "").trim());
                    double longitude = Double.parseDouble(latLngParts[1].replace("longitude=", "").trim());

                    // Create a new LatLng object
                    LatLng latLng = new LatLng(latitude, longitude);

                    // name
                    String reviewName = item.getString("reviewName");
                    // writer
                    String writer = item.getString("reviewerName");
                    // description
                    String description = item.getString("description");
                    // category
                    String tag = item.getString("tag");
                    // image
                    String imgUrl = item.getString("imgUrl");

                    Marker marker = new Marker();
                    marker.setPosition(latLng);
                    marker.setIcon(MarkerIcons.BLACK);      // basic icon for now!
                    marker.setIconTintColor(getResources().getColor(R.color.orange_light));
                    marker.setCaptionText(reviewName);
                    marker.setCaptionHaloColor(Color.WHITE);
                    marker.setCaptionTextSize(14);

                    marker.setMap(naverMap);

                    // set click listener
                    marker.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {

                            reviewDialog.setContentView(R.layout.review_dialog_layout);
                            reviewDialog.getWindow()
                                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            reviewDialog.setCancelable(true);
                            reviewDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                            // get the views
                            ImageView imageView = reviewDialog.findViewById(R.id.review_image_view);
                            TextView titleTV = reviewDialog.findViewById(R.id.review_item_title);
                            TextView writerTV = reviewDialog.findViewById(R.id.review_item_writer);
                            TextView desTV = reviewDialog.findViewById(R.id.review_item_description);

                            // set the review data
                            titleTV.setText(reviewName);
                            writerTV.setText(writer);
                            desTV.setText(description);

                            Glide.with(mainActivity)
                                    .load("http://172.10.5.130"+imgUrl)
                                    .into(new CustomTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();

                                            imageView.setImageBitmap(bitmap);
                                            reviewDialog.show();
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    });

                            return true;
                        }
                    });

                    myReviewsMarkerArray.add(marker);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public void hideMyReviewMarkers() {
        for (int i = 0 ; i < myReviewsMarkerArray.size() ; i++) {
            myReviewsMarkerArray.get(i).setMap(null);
        }
    }

    public void showAllReviewMarkers() {
        if (allReviewsMarkerArray.size() == allReviewsJsonArray.size()) {
            for (int i = 0 ; i < allReviewsMarkerArray.size() ; i ++) {
                allReviewsMarkerArray.get(i).setMap(naverMap);
            }
        } else {
            for (int i = 0 ; i < allReviewsJsonArray.size() ; i++) {
                JSONObject item = new JSONObject();
                try {
                    item = allReviewsJsonArray.get(i);

                    String latLngString = item.getString("locationUrl");
                    // if not correct LatLng format
                    if (!latLngString.contains("LatLng")) {
                        continue;
                    }
                    // Extract the latitude and longitude values from the string
                    String[] latLngParts = latLngString
                            .replace("LatLng{", "")
                            .replace("}", "")
                            .split(",");

                    double latitude = Double.parseDouble(latLngParts[0].replace("latitude=", "").trim());
                    double longitude = Double.parseDouble(latLngParts[1].replace("longitude=", "").trim());

                    // Create a new LatLng object
                    LatLng latLng = new LatLng(latitude, longitude);

                    // name
                    String reviewName = item.getString("reviewName");
                    // writer
                    String writer = item.getString("reviewerName");
                    // description
                    String description = item.getString("description");
                    // category
                    String tag = item.getString("tag");
                    // image
                    String imgUrl = item.getString("imgUrl");

                    Marker marker = new Marker();
                    marker.setPosition(latLng);
                    marker.setIcon(MarkerIcons.BLACK);      // basic icon for now!
                    marker.setIconTintColor(getResources().getColor(R.color.green_light));
                    marker.setCaptionText(reviewName);
                    marker.setCaptionHaloColor(Color.WHITE);
                    marker.setCaptionTextSize(14);

                    marker.setMap(naverMap);

                    // set click listener
                    marker.setOnClickListener(new Overlay.OnClickListener() {
                        @Override
                        public boolean onClick(@NonNull Overlay overlay) {

                            reviewDialog.setContentView(R.layout.review_dialog_layout);
                            reviewDialog.getWindow()
                                    .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            reviewDialog.setCancelable(true);
                            reviewDialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                            // get the views
                            ImageView imageView = reviewDialog.findViewById(R.id.review_image_view);
                            TextView titleTV = reviewDialog.findViewById(R.id.review_item_title);
                            TextView writerTV = reviewDialog.findViewById(R.id.review_item_writer);
                            TextView desTV = reviewDialog.findViewById(R.id.review_item_description);

                            // set the review data
                            titleTV.setText(reviewName);
                            writerTV.setText(writer);
                            desTV.setText(description);

                            Glide.with(mainActivity)
                                    .load("http://172.10.5.130"+imgUrl)
                                    .into(new CustomTarget<Drawable>() {
                                        @Override
                                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                            Bitmap bitmap = ((BitmapDrawable)resource).getBitmap();

                                            imageView.setImageBitmap(bitmap);
                                            reviewDialog.show();
                                        }

                                        @Override
                                        public void onLoadCleared(@Nullable Drawable placeholder) {

                                        }
                                    });

                            return true;
                        }
                    });

                    allReviewsMarkerArray.add(marker);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void hideFolloweeReviewMarkers() {
        for (int i = 0 ; i < allReviewsMarkerArray.size() ; i++) {
            allReviewsMarkerArray.get(i).setMap(null);
        }
    }

    @Override
    public void onDataFetched(String responseData, Integer code) {
        if (code == GET_MYREVIEW_CODE) {
            // Update your UI with the fetched data
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update your UI elements here
                    // put them in array
                    JSONObject responseJson = null;
                    try {
                        responseJson = new JSONObject(responseData);
                        myReviewsJsonArray = responseJson.getJSONArray("reviews");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // show
                    showMyReviewMarkers();
                }
            });
        } else if (code == GET_FOLLOWEEINFO_CODE) {
            // Update your UI with the fetched data
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update your UI elements here
                    // put them in array
                    JSONObject responseJson = null;
                    try {
                        responseJson = new JSONObject(responseData);
                        myFolloweeJsonArray = responseJson.getJSONArray("followees");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    // show
                    getAllMarkersOfFollowees();
                }
            });
        } else if (code == GET_FOLLOWEE_REVIEW_CODE) {
            // Update your UI with the fetched data
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update your UI elements here
                    // put them in array
                    JSONObject responseJson = null;
                    JSONArray reviewsJsonArray = null;
                    try {
                        responseJson = new JSONObject(responseData);
                        reviewsJsonArray = responseJson.getJSONArray("reviews");

                        for (int i = 0 ; i < reviewsJsonArray.length() ; i ++) {
                            allReviewsJsonArray.add(reviewsJsonArray.getJSONObject(i));
                            android.util.Log.e("REVIEWS-ADDED", reviewsJsonArray.toString());
                        }
                        android.util.Log.e("REVIEWS-ADDED", "All reviews of this followee are added");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (code == GET_LAST_FOLLOWEE_REVIEW_CODE) {
            // Update your UI with the fetched data
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Update your UI elements here
                    // put them in array
                    JSONObject responseJson = null;
                    JSONArray reviewsJsonArray = null;
                    try {
                        responseJson = new JSONObject(responseData);
                        reviewsJsonArray = responseJson.getJSONArray("reviews");

                        for (int i = 0 ; i < reviewsJsonArray.length() ; i ++) {
                            allReviewsJsonArray.add(reviewsJsonArray.getJSONObject(i));
                            android.util.Log.e("REVIEWS-ADDED-LAST", reviewsJsonArray.toString());
                        }
                        android.util.Log.e("REVIEWS-ADDED-LAST", "All reviews of LAST followee are added");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    System.out.println(allReviewsJsonArray);
                    showAllReviewMarkers();
                }
            });
        }
    }

    @Override
    public void onDataFetchError(String errorMessage) {

        android.util.Log.e("DataFetchError", errorMessage);

    }

    class RequestThread extends Thread { // DB를 불러올 때도 앱이 동작할 수 있게 하기 위해 Thread 생성
        @Override
        public void run() { // 이 쓰레드에서 실행 될 메인 코드
            try {
                // user id
                String userId = "";
                JSONObject userIdJson = new JSONObject();
                try {
                    userId = jsonObject.getString("userId");
                    userIdJson.put("userId", userId);
                    android.util.Log.e("GET-MYREVIEW", "userIdJson: "+userIdJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                URL url = new URL(serverUrl + "/my-review?userId="+userId); // 입력받은 웹서버 URL 저장
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // DB에 연결
                if(conn != null){ // 만약 연결이 되었을 경우
                    android.util.Log.e("GET-MYREVIEW", "got connection");
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
//                    conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        br.close();

                        String responseData = response.toString();
                        System.out.println(responseData);

//                        // put them in array
//                        JSONObject responseJson = new JSONObject(responseData);
//                        myReviewsJsonArray = responseJson.getJSONArray("reviews");

                        onDataFetched(responseData, GET_MYREVIEW_CODE);

                    } else {
                        onDataFetchError(""+resCode);
                        android.util.Log.e("GET-MYREVIEW", "resCode: "+resCode);
                    }

                    conn.disconnect();
                }
            } catch (Exception e) { //예외 처리
                android.util.Log.e("ERROR", e.toString());
                onDataFetchError(e.getMessage());
                e.printStackTrace(); // printStackTrace() : 에러 메세지의 발생 근원지를 찾아서 단계별로 에러를 출력
            }
        }
    }

    class RequestThreadFollow extends Thread { // DB를 불러올 때도 앱이 동작할 수 있게 하기 위해 Thread 생성
        @Override
        public void run() { // 이 쓰레드에서 실행 될 메인 코드
            try {
                // user id
                String userId = "";
                JSONObject userIdJson = new JSONObject();
                try {
                    userId = jsonObject.getString("userId");
                    userIdJson.put("userId", userId);
                    android.util.Log.e("GET-FOLLOWEE", "userIdJson: "+userIdJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                URL url = new URL(serverUrl + "/followee-info?userId="+userId); // 입력받은 웹서버 URL 저장
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // DB에 연결
                if(conn != null){ // 만약 연결이 되었을 경우
                    android.util.Log.e("GET-FOLLOWEE", "got connection");
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("GET"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
//                    conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

                    int resCode = conn.getResponseCode();
                    if (resCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        br.close();

                        String responseData = response.toString();
                        System.out.println(responseData);

//                        // put them in array
//                        JSONObject responseJson = new JSONObject(responseData);
//                        myReviewsJsonArray = responseJson.getJSONArray("reviews");

                        onDataFetched(responseData, GET_FOLLOWEEINFO_CODE);

                    } else {
                        onDataFetchError(""+resCode);
                        android.util.Log.e("GET-FOLLOWEE", "resCode: "+resCode);
                    }

                    conn.disconnect();
                }
            } catch (Exception e) { //예외 처리
                android.util.Log.e("ERROR", e.toString());
                onDataFetchError(e.getMessage());
                e.printStackTrace(); // printStackTrace() : 에러 메세지의 발생 근원지를 찾아서 단계별로 에러를 출력
            }
        }
    }

    class RequestThreadAll extends Thread { // DB를 불러올 때도 앱이 동작할 수 있게 하기 위해 Thread 생성
        @Override
        public void run() { // 이 쓰레드에서 실행 될 메인 코드
            for (int i = 0; i < myFolloweeJsonArray.length(); i++) {
                try {
                    // user id
                    String userId = ""; // that followee's id
                    JSONObject userJson = new JSONObject();
                    try {
                        userJson = myFolloweeJsonArray.getJSONObject(i);
                        userId = userJson.getString("userId");
                        android.util.Log.e("GET-FOLLOWEE-REVIEWS", "userId: " + userId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    URL url = new URL(serverUrl + "/my-review?userId=" + userId); // 입력받은 웹서버 URL 저장
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // DB에 연결
                    if (conn != null) { // 만약 연결이 되었을 경우
                        android.util.Log.e("GET-FOLLOWEE-REVIEWS", "got connection");
                        conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                        conn.setRequestMethod("GET"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
//                    conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

                        int resCode = conn.getResponseCode();
                        if (resCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                            StringBuilder response = new StringBuilder();
                            String responseLine;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            br.close();

                            String responseData = response.toString();
                            System.out.println(responseData);

                            if (i != myFolloweeJsonArray.length()-1) {
                                onDataFetched(responseData, GET_FOLLOWEE_REVIEW_CODE);
                            } else {
                                // last followee!
                                onDataFetched(responseData, GET_LAST_FOLLOWEE_REVIEW_CODE);
                            }
                        } else {
                            onDataFetchError("" + resCode);
                            android.util.Log.e("GET-FOLLOWEE-REVIEWS", "resCode: " + resCode);
                        }

                        conn.disconnect();
                    }
                } catch (Exception e) { //예외 처리
                    android.util.Log.e("ERROR", e.toString());
                    onDataFetchError(e.getMessage());
                    e.printStackTrace(); // printStackTrace() : 에러 메세지의 발생 근원지를 찾아서 단계별로 에러를 출력
                }
            }

//            // done with all followees
//            System.out.println(allReviewsJsonArray);
//            android.util.Log.e("ALL REVIEWS", "ALL HERE!!");
        }
    }

    public void hideSearchedMarkers() {
        for (int i = 0 ; i < markerArrayList.size() ; i++) {
            markerArrayList.get(i).setMap(null);
        }
    }

    public void displaySearchedMarkers(JSONArray array) {

        ArrayList<LatLng> latLngArrayList = new ArrayList<>();
        if (!markerArrayList.isEmpty()) {
            for (int i = 0 ; i < markerArrayList.size() ; i++) {
                markerArrayList.get(i).setMap(null);
            }
            markerArrayList = new ArrayList<>();
        }
        for (int i = 0 ; i < array.length() ; i ++) {
            try {
                JSONObject item = array.getJSONObject(i);

                Integer mapx = item.getInt("mapx");
                Integer mapy = item.getInt("mapy");
                Tm128 tmPosition = new Tm128(mapx, mapy);
                LatLng latLng = tmPosition.toLatLng();
                latLngArrayList.add(latLng);

                String _title = item.getString("title");
                String title = _title.replaceAll("<b>|</b>", "");
                String link = item.getString("link");
                String des = item.getString("description");
                String address = item.getString("roadAddress");

                Marker marker = new Marker();
                marker.setPosition(latLng);
                marker.setIcon(MarkerIcons.BLACK);
                marker.setIconTintColor(Color.BLACK);
                marker.setCaptionText(title);
                marker.setCaptionHaloColor(Color.WHITE);
                marker.setCaptionTextSize(14);

                marker.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        // Create the bottom sheet dialog
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);

                        // Find and set the detailed information in the bottom sheet
                        // TextView tvTitle = bottomSheetDialog.findViewById(R.id.tvTitle);
                        // TextView tvDescription = bottomSheetDialog.findViewById(R.id.tvDescription);
                        // Set the title and description based on your marker's data
                        // tvTitle.setText("Marker Title");

                        TextView titleTV = bottomSheetDialog.findViewById(R.id.titleTV);
                        TextView addressTV = bottomSheetDialog.findViewById(R.id.addressTV);
                        Button addBtn = bottomSheetDialog.findViewById(R.id.addBtn);
                        // Set the title and description based on your marker's data
                        titleTV.setText(title);
                        addressTV.setText(address);

                        // button click
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                jsonObject = mainActivity.getJsonObject();
                                Intent i = new Intent(mainActivity, NewReviewActivity.class);
                                i.putExtra("item", item.toString());
                                i.putExtra("json", jsonObject.toString());
                                mainActivity.startActivity(i);
                            }
                        });


                        // Show the bottom sheet dialog
                        bottomSheetDialog.show();

                        return true;
                    }
                });

                markerArrayList.add(marker);

                marker.setMap(naverMap);

            } catch (JSONException e) {
                android.util.Log.e("JSONError", "displaySearchedMarkers error");
                e.printStackTrace();
            }
        }

        if (latLngArrayList.size() == 0) {
            Toast.makeText(mainActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(latLngArrayList)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdate.fitBounds(bounds, 250);
            naverMap.moveCamera(cameraUpdate);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}