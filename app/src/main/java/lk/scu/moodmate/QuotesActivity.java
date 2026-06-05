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

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class QuotesActivity extends AppCompatActivity {

    TextView quoteText, authorText;

    Button newQuoteBtn, saveBtn, viewSavedBtn;

    FirebaseFirestore db;

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

        db = FirebaseFirestore.getInstance();

        loadQuote();

        newQuoteBtn.setOnClickListener(v -> loadQuote());

        saveBtn.setOnClickListener(v -> saveQuote());

        viewSavedBtn.setOnClickListener(v -> {

            Intent intent =
                    new Intent(QuotesActivity.this,
                            SavedQuotesActivity.class);

            startActivity(intent);

        });
    }

    private void loadQuote() {

        String url = "https://zenquotes.io/api/random";

        RequestQueue queue = Volley.newRequestQueue(this);

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
                                "Failed to load quote",
                                Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void saveQuote() {

        Map<String, Object> quote =
                new HashMap<>();

        quote.put("quote", currentQuote);
        quote.put("author", currentAuthor);

        db.collection("favorite_quotes")
                .add(quote)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(this,
                            "Quote Saved",
                            Toast.LENGTH_SHORT).show();

                })

                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Save Failed",
                            Toast.LENGTH_SHORT).show();

                });
    }
}

