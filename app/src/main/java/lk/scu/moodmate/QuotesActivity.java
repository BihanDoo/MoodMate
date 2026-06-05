package lk.scu.moodmate;

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

    RequestQueue queue;

    // REPLACE WITH YOUR REAL MONGODB DATA API URL
    private final String mongoURL =
            "https://YOUR_APP_ID.mongodb-api.com/app/data-xxxxx/endpoint/data/v1/action/insertOne";

    // REPLACE WITH YOUR REAL API KEY
    private final String apiKey =
            "YOUR_API_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quotes);

        quoteText = findViewById(R.id.quoteText);
        authorText = findViewById(R.id.authorText);

        newQuoteBtn = findViewById(R.id.newQuoteBtn);
        saveBtn = findViewById(R.id.saveBtn);
        viewSavedBtn = findViewById(R.id.viewSavedBtn);

        queue = Volley.newRequestQueue(this);

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

        JsonArrayRequest request =
                new JsonArrayRequest(
                        Request.Method.GET,
                        url,
                        null,

                        response -> {

                            try {

                                JSONObject quoteObject =
                                        response.getJSONObject(0);

                                currentQuote =
                                        quoteObject.getString("q");

                                currentAuthor =
                                        quoteObject.getString("a");

                                quoteText.setText(currentQuote);

                                authorText.setText("- " + currentAuthor);

                            } catch (JSONException e) {

                                Toast.makeText(
                                        this,
                                        "JSON Error",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        },

                        error -> Toast.makeText(
                                this,
                                "Failed To Load Quote",
                                Toast.LENGTH_SHORT
                        ).show());

        queue.add(request);
    }

    private void saveQuoteToMongoDB() {

        if(currentQuote.isEmpty()) {

            Toast.makeText(
                    this,
                    "No Quote To Save",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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
                        mongoURL,
                        data,

                        response -> Toast.makeText(
                                this,
                                "Quote Saved Successfully",
                                Toast.LENGTH_SHORT
                        ).show(),

                        error -> Toast.makeText(
                                this,
                                "Failed To Save Quote",
                                Toast.LENGTH_SHORT
                        ).show()) {

                    @Override
                    public Map<String, String> getHeaders() {

                        Map<String, String> headers =
                                new HashMap<>();

                        headers.put(
                                "Content-Type",
                                "application/json"
                        );

                        headers.put(
                                "api-key",
                                apiKey
                        );

                        return headers;
                    }
                };

        queue.add(request);
    }
}