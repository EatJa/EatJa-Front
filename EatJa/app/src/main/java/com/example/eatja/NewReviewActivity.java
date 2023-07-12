package com.example.eatja;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eatja.ui.home.HomeFragment;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.ByteString;

public class NewReviewActivity extends AppCompatActivity {

    private Context context;
    private TextView titleTV;
    private Button uploadImgBtn, registerBtn;
    private ImageView reviewIV;
    private EditText reviewET;
    private String title;
    private LatLng locationLatLng;
    private String reviewDescription;
    private JSONObject jsonObject;

    private TextView categoryTV;
    private boolean[] selectedTags;
    private Integer selectedTag = -1;
    String[] tagArray = {"한식", "양식", "중식", "일식", "카페", "기타"};
    private Uri selectedImageUri;
    private String serverUrl = "http://172.10.5.130:80/eatja/api/v1";

    // ActivityResultLauncher for image selection
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_review);

        context = this;

        String itemString = getIntent().getStringExtra("item");
        String jsonString = getIntent().getStringExtra("json");

        JSONObject item = new JSONObject();
        Integer mapx, mapy;
        Tm128 tmPosition;
        try {
            item = new JSONObject(itemString);
            String _title = item.getString("title");
            title = _title.replaceAll("<b>|</b>", "");
            mapx = item.getInt("mapx");
            mapy = item.getInt("mapy");
            tmPosition = new Tm128(mapx, mapy);
            locationLatLng = tmPosition.toLatLng();

            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // bind view elements
        titleTV = findViewById(R.id.titleTV);
        reviewIV = findViewById(R.id.reviewIV);
        categoryTV = findViewById(R.id.categoryTV);
        uploadImgBtn = findViewById(R.id.uploadImageBtn);
        registerBtn = findViewById(R.id.registerBtn);
        reviewET = findViewById(R.id.reviewET);

        // set view elements
        titleTV.setText(title);

        selectedTags = new boolean[tagArray.length];
        categoryTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

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
                        categoryTV.setText(tagArray[selectedTag]);
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

        // Initialize the ActivityResultLauncher
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        selectedImageUri = data.getData();

                        try {
                            // Load the selected image into the ImageView
                            InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                            reviewIV.setImageBitmap(selectedImage);
                            try {
                                // Read the rotation information from the image's EXIF metadata
                                ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(selectedImageUri));
                                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                                // Rotate the image bitmap based on the orientation value
                                Matrix matrix = new Matrix();
                                switch (orientation) {
                                    case ExifInterface.ORIENTATION_ROTATE_90:
                                        matrix.postRotate(90);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_180:
                                        matrix.postRotate(180);
                                        break;
                                    case ExifInterface.ORIENTATION_ROTATE_270:
                                        matrix.postRotate(270);
                                        break;
                                    default:
                                        // No rotation needed
                                        break;
                                }

                                // Apply the rotation transformation to the Bitmap
                                Bitmap rotatedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, true);

                                // Set the rotated image to the ImageView
                                reviewIV.setImageBitmap(rotatedImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });

        // upload image button
        uploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // register button
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected image from the ImageView
                Bitmap reviewImage = null;
                try {
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    reviewImage = BitmapFactory.decodeStream(imageStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Convert the Bitmap image to a byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                reviewImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                ByteString imageByteString = ByteString.of(imageBytes);

                if (selectedTag < 0) {
                    Toast.makeText(context, "태그를 선택하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // get the review description content
                    reviewDescription = reviewET.getText().toString();
                    // get user profile from json
                    String userId ="";
                    String userName = "";
                    try {
                        userId = jsonObject.getString("userId");
                        userName = jsonObject.getString("userName");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // Create a request body with multipart/form-data
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("userId", userId)
                            .addFormDataPart("reviewName", title)
                            .addFormDataPart("reviewerName", userName)
                            .addFormDataPart("imgUrl", "upload.jpg",
                                    RequestBody.create(MediaType.parse("image/jpeg"), imageByteString))
                            .addFormDataPart("locationUrl", locationLatLng.toString())
                            .addFormDataPart("tag", selectedTag.toString())
                            .addFormDataPart("description", reviewDescription)
                            .build();

                    // Create a POST request to your API
                    Request request = new Request.Builder()
                            .url(serverUrl+"/my-review")
                            .post(requestBody)
                            .build();

                    // Create an OkHttpClient instance
                    OkHttpClient client = new OkHttpClient();

                    // Send the POST request asynchronously
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            // Handle request failure
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            // Handle request success
                            if (response.isSuccessful()) {
                                // Process the response from your API
                                String responseData = response.body().string();
                                android.util.Log.e("REVIEW", responseData);
                                android.util.Log.e("REVIEW", "review POST sent!");

                                // Show a toast message
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                });

//                                // Return to HomeFragment
//                                Intent intent = new Intent(context, HomeFragment.class);
//                                startActivity(intent);

                                // Finish the NewReviewActivity to prevent going back to it
                                finish();

                            } else {
                                // Handle request failure
                                String errorMessage = response.message();
                                android.util.Log.e("REVIEW", errorMessage);
                                // ... Handle the error message as needed
                            }
                        }
                    });
                }
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

}