package lk.scu.moodmate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Model class for ZenQuotes API response
 */
class QuoteResponse {
    @SerializedName("q")
    private String quote;
    @SerializedName("a")
    private String author;

    public String getQuote() { return quote; }
    public String getAuthor() { return author; }
}

/**
 * Retrofit interface for ZenQuotes API
 */
interface QuoteApiService {
    @GET("random")
    Call<List<QuoteResponse>> getRandomQuote();
}

public class QuotesActivity extends AppCompatActivity {

    private TextView tvQuoteText, tvQuoteAuthor;
    private ProgressBar progressBar;
    private MaterialButton btnNewQuote, btnLike, btnDislike;
    private QuoteApiService apiService;
    private FirebaseFirestore db;
    private String userEmail;
    private String currentQuoteText, currentQuoteAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quotes);

        // Initialize UI components
        tvQuoteText = findViewById(R.id.tvQuoteText);
        tvQuoteAuthor = findViewById(R.id.tvQuoteAuthor);
        progressBar = findViewById(R.id.quoteProgressBar);
        btnNewQuote = findViewById(R.id.btnNewQuote);
        btnLike = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);

        // Initialize Firestore and Session
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail", "anonymous");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://zenquotes.io/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(QuoteApiService.class);

        btnNewQuote.setOnClickListener(v -> fetchQuote());
        
        btnLike.setOnClickListener(v -> saveInteraction("like"));
        btnDislike.setOnClickListener(v -> saveInteraction("dislike"));

        // Load the first quote
        fetchQuote();
    }

    private void fetchQuote() {
        // Reset and show loading
        tvQuoteText.setText("Fetching wisdom...");
        tvQuoteAuthor.setText("");
        progressBar.setVisibility(View.VISIBLE);
        btnNewQuote.setEnabled(false);
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);

        apiService.getRandomQuote().enqueue(new Callback<List<QuoteResponse>>() {
            @Override
            public void onResponse(Call<List<QuoteResponse>> call, Response<List<QuoteResponse>> response) {
                progressBar.setVisibility(View.GONE);
                btnNewQuote.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    QuoteResponse quoteObj = response.body().get(0);
                    currentQuoteText = quoteObj.getQuote();
                    currentQuoteAuthor = quoteObj.getAuthor();
                    
                    tvQuoteText.setText("\"" + currentQuoteText + "\"");
                    tvQuoteAuthor.setText("- " + currentQuoteAuthor);
                    
                    btnLike.setEnabled(true);
                    btnDislike.setEnabled(true);
                } else {
                    tvQuoteText.setText("The silence is the best answer.");
                    Toast.makeText(QuotesActivity.this, "Failed to get a quote", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<QuoteResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnNewQuote.setEnabled(true);
                tvQuoteText.setText("Check your internet, wisdom is offline!");
                Toast.makeText(QuotesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveInteraction(String type) {
        if (currentQuoteText == null || userEmail == null) return;

        Map<String, Object> interaction = new HashMap<>();
        interaction.put("userEmail", userEmail);
        interaction.put("type", type);
        interaction.put("contentType", "quote");
        interaction.put("quoteText", currentQuoteText);
        interaction.put("quoteAuthor", currentQuoteAuthor);
        interaction.put("timestamp", System.currentTimeMillis());

        db.collection("interactions")
                .add(interaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(QuotesActivity.this, "Quote " + type + "d!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(QuotesActivity.this, "Failed to save reaction", Toast.LENGTH_SHORT).show();
                });
    }
}
