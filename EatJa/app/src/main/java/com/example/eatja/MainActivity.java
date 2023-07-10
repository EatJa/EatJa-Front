package com.example.eatja;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eatja.databinding.ActivityMainBinding;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NaverIdLoginSDK naverIdLoginSDK;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_mypage)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // login instance 초기화
        naverIdLoginSDK = NaverIdLoginSDK.INSTANCE;
        naverIdLoginSDK.showDevelopersLog(true);
        naverIdLoginSDK.initialize(
                this,
                getString(R.string.naver_client_id),
                getString(R.string.naver_client_secret),
                getString(R.string.app_name)
        );

        OAuthLoginCallback oauthLoginCallback = new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                android.util.Log.i("LOG", naverIdLoginSDK.getAccessToken());
                android.util.Log.i("LOG", naverIdLoginSDK.getRefreshToken());
                android.util.Log.i("LOG", ""+ naverIdLoginSDK.getExpiresAt());
                android.util.Log.i("LOG", naverIdLoginSDK.getTokenType());
                android.util.Log.i("LOG", naverIdLoginSDK.getState().toString());
            }

            @Override
            public void onFailure(int httpStatus, String message) {
                String errorCode = naverIdLoginSDK.getLastErrorCode().getCode();
                String errorDescription = naverIdLoginSDK.getLastErrorDescription();
                Toast.makeText(context, "errorCode: " + errorCode + ", errorDesc: " + errorDescription, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int errorCode, String message) {
                onFailure(errorCode, message);
            }
        };

        naverIdLoginSDK.authenticate(context, oauthLoginCallback);

    }

//    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
//
//    }

}