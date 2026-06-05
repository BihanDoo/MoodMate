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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QuotesActivity extends AppCompatActivity {

    TextView quoteText, authorText;

    Button newQuoteBtn, saveBtn, viewSavedBtn;

    String currentQuote = "";
    String currentAuthor = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes);

        quoteText = findViewById(R.id.quoteText);
        authorText = findViewById(R.id.authorText);

        newQuoteBtn = findViewById(R.id.newQuoteBtn);
        saveBtn = findViewById(R.id.saveBtn);
        viewSavedBtn = findViewById(R.id.viewSavedBtn);

        loadQuote();

        newQuoteBtn.setOnClickListener(v -> loadQuote());

        saveBtn.setOnClickListener(v -> saveQuoteToMongoDB());

        viewSavedBtn.setOnClickListener(v -> {

            Intent intent =
                    new Intent(QuotesActivity.this,
                            SavedQuotesActivity.class);

            startActivity(intent);

        });
    }

    private void loadQuote() {

        String url =
                "https://zenquotes.io/api/random";

        RequestQueue queue =
                Volley.newRequestQueue(this);

        JsonArrayRequest request =
                new JsonArrayRequest(
                        Request.Method.GET,
                        url,
                        null,

                        response -> {

                            try {

                                currentQuote =
                                        response.getJSONObject(0)
                                                .getString("q");

                                currentAuthor =
                                        response.getJSONObject(0)
                                                .getString("a");

                                quoteText.setText(currentQuote);

                                authorText.setText("- " + currentAuthor);

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }

                        },

                        error -> Toast.makeText(this,
                                "Failed To Load Quote",
                                Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void saveQuoteToMongoDB() {

        String url =
                "https://YOUR_DATA_API_URL/action/insertOne";

        RequestQueue queue =
                Volley.newRequestQueue(this);

        JSONObject data = new JSONObject();

        try {

            data.put("collection",
                    "favorite_quotes");

            data.put("database",
                    "BrainBreakDB");

            data.put("dataSource",
                    "Cluster0");

            JSONObject document =
                    new JSONObject();

            document.put("quote",
                    currentQuote);

            document.put("author",
                    currentAuthor);

            data.put("document",
                    document);

        } catch (JSONException e) {

            e.printStackTrace();
        }

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        data,

                        response -> Toast.makeText(this,
                                "Quote Saved",
                                Toast.LENGTH_SHORT).show(),

                        error -> Toast.makeText(this,
                                "Save Failed",
                                Toast.LENGTH_SHORT).show()) {

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
}


