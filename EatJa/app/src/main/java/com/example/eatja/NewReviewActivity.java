package com.example.eatja;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

public class NewReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_review);

        String itemString = getIntent().getStringExtra("item");
        JSONObject item = new JSONObject();
        try {
            item = new JSONObject(itemString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}