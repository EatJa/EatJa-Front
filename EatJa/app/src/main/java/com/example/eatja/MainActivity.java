package com.example.eatja;

import static android.Manifest.permission.READ_MEDIA_IMAGES;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NaverIdLoginSDK naverIdLoginSDK;
    private Context context;
    private ActivityResultLauncher<Intent> launcher;
    private String accessToken;
    private Handler handler = new Handler();

    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";
    private String userId, userName, profileImg;
    private JSONObject jsonObject;

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
                    userId = response.getProfile().getId();
                    userName = response.getProfile().getName();
                    profileImg = response.getProfile().getProfileImage();

                    String profile = response.getProfile().toString();
                    android.util.Log.i("PROFILE", profile);
                    android.util.Log.i("PROFILE", "id: " + userId + "\ntoken: " + accessToken);
                    Toast.makeText(MainActivity.this, "네이버 아이디 로그인 성공!", Toast.LENGTH_SHORT).show();

                    jsonObject = new JSONObject();
                    try{
                        jsonObject.put("userId", userId);
                        jsonObject.put("userName", userName);
                        jsonObject.put("profileImg", profileImg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    RequestThread thread = new RequestThread(); // Thread 생성
                    thread.start();
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
        // permissions to ask based on sdk version
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(android.Manifest.permission.INTERNET);
        permissions.add(android.Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(READ_MEDIA_IMAGES);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        // below line is use to request
        // permission in the current activity.
        Dexter.withContext(context)
                // below line is use to request the number of
                // permissions which are required in our app.
                .withPermissions(permissions)
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

    class RequestThread extends Thread { // DB를 불러올 때도 앱이 동작할 수 있게 하기 위해 Thread 생성
        @Override
        public void run() { // 이 쓰레드에서 실행 될 메인 코드
            try {
                URL url = new URL(serverUrl + "/sign-in"); // 입력받은 웹서버 URL 저장
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // DB에 연결
                if(conn != null){ // 만약 연결이 되었을 경우
                    android.util.Log.e("CHECK", "got connection");
                    conn.setConnectTimeout(10000); // 10초 동안 기다린 후 응답이 없으면 종료
                    conn.setRequestMethod("POST"); // GET 메소드 : 웹 서버로 부터 리소스를 가져온다.
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true); // 서버에서 온 데이터를 입력받을 수 있는 상태인가? true
                    conn.setDoOutput(true); // 서버에서 온 데이터를 출력할 수 있는 상태인가? true

                    // write
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

//                    int resCode = conn.getResponseCode(); // 응답 코드를 리턴 받는다.
//                    System.out.println(resCode);
//                    if(resCode == HttpURLConnection.HTTP_OK){ // 만약 응답 코드가 200(=OK)일 경우
//                        android.util.Log.e("CHECK", "resCode is OK");
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        // BufferedReader() : 엔터만 경계로 인식하고 받은 데이터를 String 으로 고정, Scanner 에 비해 빠름!
//                        // InputStreamReader() : 지정된 문자 집합 내의 문자로 인코딩
//                        // getInputStream() : url 에서 데이터를 읽어옴
//                        String line = null; // 웹에서 가져올 데이터를 저장하기위한 변수
//                        while(true){
//                            line = reader.readLine(); // readLine() : 한 줄을 읽어오는 함수
//                            if(line == null) // 만약 읽어올 줄이 없으면 break
//                                break;
//                            System.out.println(line); // 출력 *80번째 줄의 함수*
//                        }
//                        reader.close(); // 입력이 끝남
//                    }
//                    android.util.Log.e("CHECK", "resCode is "+ resCode);
//                    conn.disconnect(); // DB연결 해제
//                    android.util.Log.e("CHECK", "disconnected");
                }
            } catch (Exception e) { //예외 처리
                android.util.Log.e("ERROR", e.toString());
                e.printStackTrace(); // printStackTrace() : 에러 메세지의 발생 근원지를 찾아서 단계별로 에러를 출력
            }
        }
    }

    public void println(final String data){ // final : 변수의 상수화 => 변수 변경 불가
        handler.post(new Runnable() {
            // post() : 핸들러에서 쓰레드로 ()를 보냄
            // Runnable() : 실행 코드가 담긴 객체
            @Override
            public void run() {
                System.out.println(data);
            } // run() : 실행될 코드가 들어있는 메소드
        });
    }

}

//class RequestThread extends Thread {
//    @Override
//
//    public void run(){}
//    public void run(String userId, String userName, String profileImg, String serverUrl) {
//        // api POST
//        JSONObject jsonObject = new JSONObject();
//        try{
//            jsonObject.put("userId", userId);
//            jsonObject.put("userName", userName);
//            jsonObject.put("profileImg", profileImg);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        HttpURLConnection conn = null;
//        try {
//            URL url = new URL(serverUrl + "/sign-in");
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            System.out.println(conn.getResponseCode());
//
//            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
//            os.writeBytes(jsonObject.toString());
//            os.flush();
//            os.close();
//
//            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
//            Log.i("MSG" , conn.getResponseMessage());
//
//            conn.disconnect();
//        } catch (Exception e) {
//            Log.e("EXCEPTION", "연결 오류: " + e);
//        }
//    }
//}