package com.example.eatja.search;

public interface DataCallback {
    void onDataFetched(String data, Integer code);
    void onDataFetchError(String errorMessage);
}
