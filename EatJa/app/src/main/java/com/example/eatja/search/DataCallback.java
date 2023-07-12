package com.example.eatja.search;

public interface DataCallback {
    void onDataFetched(String data);
    void onDataFetchError(String errorMessage);
}
