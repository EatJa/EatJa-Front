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
import com.example.eatja.search.DataCallback;
import com.example.eatja.ui.home.HomeFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPageFragment extends Fragment implements DataCallback {

    private FragmentMypageBinding binding;
    private MainActivity mainActivity;
    private JSONObject jsonObject;  // profile json
    private TextView userNameTV, followerCountTV, followeeCountTV;
    private CircleImageView profileIV;
    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";
    private String followerCount, followeeCount;

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
        followerCountTV = binding.followersCountTextView;
        followeeCountTV = binding.followeesCountTextView;

        // set profile
        setProfile();

        // get follower/following
        getFollowData();

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

    public void getFollowData() {
        RequestThread requestThread = new RequestThread();
        requestThread.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDataFetched(String responseData) {

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update your UI elements here
                // put them in array
                JSONObject responseJson = null;
                JSONObject userJson = null;
                try {
                    responseJson = new JSONObject(responseData);
                    userJson = responseJson.getJSONObject("user");
                    Integer followerCountInt = userJson.getInt("followerCount");
                    Integer followeeCountInt = userJson.getInt("followeeCount");
                    followerCount = followerCountInt.toString();
                    followeeCount = followeeCountInt.toString();

                    // set count
                    followerCountTV.setText(followerCount);
                    followeeCountTV.setText(followeeCount);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
                    userId = jsonObject.getString("userId");    // userid string
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                URL url = new URL(serverUrl + "/my-page?userId="+userId); // 입력받은 웹서버 URL 저장
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // DB에 연결
                if(conn != null){ // 만약 연결이 되었을 경우
                    android.util.Log.e("GET-MYPAGE", "got connection");
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

                        // on data fetched (async)
                        onDataFetched(responseData);

                    } else {
                        onDataFetchError(""+resCode);
                        android.util.Log.e("GET-MYPAGE", "resCode: "+resCode);
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
}