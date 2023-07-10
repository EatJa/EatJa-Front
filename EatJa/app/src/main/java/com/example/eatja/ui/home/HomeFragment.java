package com.example.eatja.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.eatja.MainActivity;
import com.example.eatja.R;
import com.example.eatja.databinding.FragmentHomeBinding;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Collections;

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

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}