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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class NewReviewActivity extends AppCompatActivity {

    private Context context;
    private TextView titleTV;
    private Button uploadImgBtn, registerBtn;
    private ImageView reviewIV;
    private EditText reviewET;
    private String title;

    private TextView categoryTV;
    private boolean[] selectedTags;
    private Integer selectedTag = 0;
    ArrayList<Integer> tagList = new ArrayList<>();
    String[] tagArray = {"한식", "양식", "중식", "일식", "카페", "기타"};
    private Uri selectedImageUri;
    private String imagePath;

    // ActivityResultLauncher for image selection
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_review);

        context = this;

        String itemString = getIntent().getStringExtra("item");
        JSONObject item = new JSONObject();
        try {
            item = new JSONObject(itemString);
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

        // get item data
        try {
            String _title = item.getString("title");
            title = _title.replaceAll("<b>|</b>", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                            reviewIV.setImageBitmap(selectedImage);
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

            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

}