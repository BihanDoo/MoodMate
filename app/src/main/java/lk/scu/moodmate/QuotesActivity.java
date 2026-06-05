package com.example.brainbreak;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import com.google.firebase.firestore.FirebaseFirestore;

import o```java
private void saveQuoteToMongoDB() {

    String url =
            "YOUR_MONGODB_DATA_API_URL";

    RequestQueue queue =
            Volley.newRequestQueue(this);

    JSONObject data = new JSONObject();

    try {

        data.put("collection", "favorite_quotes");
        data.put("database", "BrainBreakDB");
        data.put("dataSource", "Cluster0");

        JSONObject document =
                new JSONObject();

        document.put("quote", currentQuote);
        document.put("author", currentAuthor);

        data.put("document", document);

    } catch (JSONException e) {
        e.printStackTrace();
    }

    JsonObjectRequest request =
            new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    data,

                    response -> {

                        Toast.makeText(this,
                                "Quote Saved",
                                Toast.LENGTH_SHORT).show();

                    },

                    error -> {

                        Toast.makeText(this,
                                "Save Failed",
                                Toast.LENGTH_SHORT).show();

                    }) {

                @Override
                public Map<String, String> getHeaders() {

                    Map<String, String> headers =
                            new HashMap<>();

                    headers.put("Content-Type",
                            "application/json");

                    headers.put("api-key",
                            "YOUR_API_KEY");

                    return headers;
                }
            };

    queue.add(request);
}

