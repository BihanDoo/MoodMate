package lk.scu.moodmate;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Model class for Joke API response
 */
class JokeResponse {
    private String setup;
    private String punchline;

    public String getSetup() { return setup; }
    public String getPunchline() { return punchline; }
}

/**
 * Retrofit interface for Joke API
 */
interface JokeApiService {
    @GET("random_joke")
    Call<JokeResponse> getRandomJoke();
}

public class Joke extends AppCompatActivity {

    private TextView tvSetup, tvPunchline;
    private ProgressBar progressBar;
    private MaterialButton btnNewJoke, btnLike, btnDislike;
    private JokeApiService apiService;
    private FirebaseFirestore db;
    private String userEmail;
    private String currentJokeSetup, currentJokePunchline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_joke);

        // Initialize UI components
        tvSetup = findViewById(R.id.tvSetup);
        tvPunchline = findViewById(R.id.tvPunchline);
        progressBar = findViewById(R.id.jokeProgressBar);
        btnNewJoke = findViewById(R.id.btnNewJoke);
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
                .baseUrl("https://official-joke-api.appspot.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(JokeApiService.class);

        btnNewJoke.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            fetchJoke();
        });
        
        btnLike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.setBackgroundColor(getResources().getColor(R.color.joke_button));
            saveInteraction("like");
        });

        btnDislike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.setBackgroundColor(Color.parseColor("#FF5252"));
            btnDislike.setTextColor(Color.parseColor("#FFFFFF"));
            saveInteraction("dislike");
        });

        // Load the first joke
        fetchJoke();
    }

    private void fetchJoke() {
        // Reset and show loading
        tvSetup.setText("Thinking of something funny...");
        tvPunchline.setText("");
        progressBar.setVisibility(View.VISIBLE);
        btnNewJoke.setEnabled(false);
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);

        apiService.getRandomJoke().enqueue(new Callback<JokeResponse>() {
            @Override
            public void onResponse(Call<JokeResponse> call, Response<JokeResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnNewJoke.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    currentJokeSetup = response.body().getSetup();
                    currentJokePunchline = response.body().getPunchline();
                    tvSetup.setText(currentJokeSetup);
                    tvPunchline.setText(currentJokePunchline);
                    btnLike.setEnabled(true);
                    btnDislike.setEnabled(true);
                } else {
                    tvSetup.setText("Oops! My funny bone is broken.");
                    Snackbar.make(findViewById(R.id.main), "Failed to get a joke", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JokeResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnNewJoke.setEnabled(true);
                tvSetup.setText("Check your internet, no jokes today!");
                Snackbar.make(findViewById(R.id.main), "Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveInteraction(String type) {
        if (currentJokeSetup == null || userEmail == null) return;

        Map<String, Object> interaction = new HashMap<>();
        interaction.put("userEmail", userEmail);
        interaction.put("type", type);
        interaction.put("contentType", "joke");
        interaction.put("setup", currentJokeSetup);
        interaction.put("punchline", currentJokePunchline);
        interaction.put("timestamp", System.currentTimeMillis());

        db.collection("interactions")
                .add(interaction)
                .addOnSuccessListener(documentReference -> {
                    View rootView = findViewById(R.id.main);
                    if (rootView != null) {
                        rootView.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        Snackbar.make(rootView, "Joke " + type + "d!", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(type.equals("like") ? 0xFF4CAF50 : 0xFFF44336)
                                .setTextColor(0xFFFFFFFF)
                                .show();
                        btnLike.setBackgroundColor(Color.parseColor("#B1E978"));
                        btnDislike.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        btnDislike.setTextColor(Color.parseColor("#FF5252"));
                    }
                    if (btnLike != null) {
                        btnLike.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    }
                })
                .addOnFailureListener(e -> {
                    View rootView = findViewById(R.id.main);
                    if (rootView != null) {
                        rootView.performHapticFeedback(HapticFeedbackConstants.REJECT);
                        Snackbar.make(rootView, "Failed to save reaction", Snackbar.LENGTH_SHORT).show();
                    }
                    if (btnDislike != null) {
                        btnDislike.performHapticFeedback(HapticFeedbackConstants.REJECT);
                    }
                });
    }
}
