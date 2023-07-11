package com.example.eatja;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.eatja.databinding.ActivityMainBinding;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NaverIdLoginSDK naverIdLoginSDK;
    private Context context;
    private ActivityResultLauncher<Intent> launcher;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // activity result launcher to solve deprecation
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the result here
                }
            });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_mypage)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // runtime permission
        requestPermissions();

        // get login
        getNaverLogin();
    }

    private void getNaverLogin() {
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

                accessToken = naverIdLoginSDK.getAccessToken();
                new NidOAuthLogin().callProfileApi(profileCallback);
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

            // profile callback
            private NidProfileCallback<NidProfileResponse> profileCallback = new NidProfileCallback<NidProfileResponse>() {
                @Override
                public void onSuccess(NidProfileResponse response) {
                    String userId = response.getProfile().getId();
                    String profile = response.getProfile().toString();
                    android.util.Log.i("PROFILE", profile);
                    android.util.Log.i("PROFILE", "id: " + userId + "\ntoken: " + accessToken);
                    Toast.makeText(MainActivity.this, "네이버 아이디 로그인 성공!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i, @NonNull String s) {
                    String errorCode = naverIdLoginSDK.getLastErrorCode().getCode();
                    String errorDescription = naverIdLoginSDK.getLastErrorDescription();
                    Toast.makeText(MainActivity.this, "errorCode: " + i + "\n"
                            + "errorDescription: " + s, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(int i, @NonNull String s) {
                    onFailure(i, s);
                }
            };
        };

        naverIdLoginSDK.authenticate(context, oauthLoginCallback);

    }

    private void requestPermissions() {
        // below line is use to request
        // permission in the current activity.
        Dexter.withContext(context)
                // below line is use to request the number of
                // permissions which are required in our app.
                .withPermissions(android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                // after adding permissions we are
                // calling and with listener method.
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        // this method is called when all permissions are granted
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            // do your work now
                            Toast.makeText(context, "All permissions are granted.", Toast.LENGTH_SHORT).show();
                        }
                        // check for permanent denial of any permission
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permanently,
                            // we will show user a dialog message.
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        // this method is called when user grants some
                        // permission and denies some of them.
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
                    // this method is use to handle error
                    // in runtime permissions
                    @Override
                    public void onError(DexterError error) {
                        // we are displaying a toast message for error message.
                        Toast.makeText(context, "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                // below line is use to run the permissions
                // on same thread and to check the permissions
                .onSameThread().check();
    }

    private void showSettingsDialog() {
        // dialog message for when a permission is permanently denied

        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Need Permissions");

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called on click on positive
                // button and on clicking shit button we
                // are redirecting our user from our app to the
                // settings page of our app.
                dialog.cancel();
                // below is the intent from which we
                // are redirecting our user.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
//                startActivityForResult(intent, 101);
                launcher.launch(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // this method is called when
                // user click on negative button.
                dialog.cancel();
            }
        });
        // below line is used
        // to display our dialog
        builder.show();
    }

}