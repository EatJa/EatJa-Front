package com.example.eatja.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.eatja.MainActivity;
import com.example.eatja.NewReviewActivity;
import com.example.eatja.R;
import com.example.eatja.databinding.FragmentHomeBinding;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

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
    private TextView filterFollowTV, filterTagTV;
    private boolean[] selectedFollows, selectedTags;
    ArrayList<Integer> followList = new ArrayList<>();
    ArrayList<Integer> tagList = new ArrayList<>();
    String[] followArray = new String[4];
    String[] tagArray = new String[4];

    private ArrayList<Marker> markerArrayList = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // text view
        filterFollowTV = binding.filterFollowTV;
        // dummy for now
        followArray[0] = "Kim";
        followArray[1] = "Park";
        followArray[2] = "Choi";
        followArray[3] = "Ryu";
        selectedFollows = new boolean[followArray.length];
        filterFollowTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

                // set title
                builder.setTitle("팔로우를 선택하세요");

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(followArray, selectedFollows, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            followList.add(i);
                            // Sort array list
                            Collections.sort(followList);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            followList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < followList.size(); j++) {
                            // concat array value
                            stringBuilder.append(followArray[followList.get(j)]);
                            // check condition
                            if (j != followList.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        filterFollowTV.setText(stringBuilder.toString());
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
                        for (int j = 0; j < selectedFollows.length; j++) {
                            // remove all selection
                            selectedFollows[j] = false;
                            // clear language list
                            followList.clear();
                            // clear text view value
                            filterFollowTV.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });

        filterTagTV = binding.filterTagTV;
        // dummy for now
        tagArray[0] = "한식";
        tagArray[1] = "양식";
        tagArray[2] = "일식";
        tagArray[3] = "중식";
        selectedTags = new boolean[tagArray.length];
        filterTagTV.setOnClickListener(new View.OnClickListener() {
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
                        }
                        // set text on textView
                        filterTagTV.setText(stringBuilder.toString());
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
                            filterTagTV.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });

        // buttons
        myEatBtnPressed = binding.myEatBtnPressed;
        myEatBtnNotPressed = binding.myEatBtnNotPressed;
        myEatBtnPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myEatBtnPressed.getVisibility() == View.VISIBLE) {
                    myEatBtnPressed.setVisibility(View.INVISIBLE);
                    myEatBtnNotPressed.setVisibility(View.VISIBLE);
                }
            }
        });
        myEatBtnNotPressed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myEatBtnNotPressed.getVisibility() == View.VISIBLE) {
                    myEatBtnPressed.setVisibility(View.VISIBLE);
                    myEatBtnNotPressed.setVisibility(View.INVISIBLE);
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

        locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);
    }

    public void setUiSettings() {
        uiSettings.setScaleBarEnabled(true);
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
                        TextView titleTV = bottomSheetDialog.findViewById(R.id.titleTV);
                        TextView addressTV = bottomSheetDialog.findViewById(R.id.addressTV);
                        ImageView imageIV = bottomSheetDialog.findViewById(R.id.imageIV);
                        Button addBtn = bottomSheetDialog.findViewById(R.id.addBtn);
                        // Set the title and description based on your marker's data
                        titleTV.setText(title);
                        addressTV.setText(address);

                        // button click
                        addBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(mainActivity, NewReviewActivity.class);
                                i.putExtra("item", item.toString());
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