package com.example.eatja.search;

import com.example.eatja.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SearchLocal {
    final String baseUrlLocal = "https://openapi.naver.com/v1/search/local.json?query=";
    final String baseUrlImg = "https://openapi.naver.com/v1/search/image.json?query=";

    public String search(String _url) {
        HttpURLConnection con = null;
        String result = "";

        try {
            URL urlLocal = new URL(baseUrlLocal + _url + "&display=5");
            con = (HttpURLConnection) urlLocal.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", "WzwSWaFJOPxVXGiNcRRt");
            con.setRequestProperty("X-Naver-Client-Secret", "IV0tDMUUOp");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                result = readBody(con.getInputStream());
            } else {
                result = readBody(con.getErrorStream());
            }
        } catch (Exception e) {
            android.util.Log.e("EXCEPTION", "연결 오류: " + e);
        } finally {
            con.disconnect();
        }

        return result;
    }

    public String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try(BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답 읽기 실패", e);
        }
    }

}
