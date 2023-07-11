package com.example.eatja.ui.mypage;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.eatja.MainActivity;
import com.example.eatja.databinding.FragmentMypageBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageFragment extends Fragment {

    private FragmentMypageBinding binding;
    private MainActivity mainActivity;
    private JSONObject jsonObject;
    private TextView userNameTV;
    private CircleImageView profileIV;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyPageViewModel myPageViewModel =
                new ViewModelProvider(this).get(MyPageViewModel.class);

        binding = FragmentMypageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // get profile json
        jsonObject = mainActivity.getJsonObject();

        // bind views
        userNameTV = binding.usernameTextView;
        profileIV = binding.userProfileImageView;

        // set profile
        setProfile();

        return root;
    }

    public void setProfile() {
        try {
            String userName = jsonObject.getString("userName");
            userNameTV.setText(userName);
            String profileUrl = jsonObject.getString("profileImg");
            Picasso.get()
                    .load(profileUrl)
                    .into(profileIV);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}