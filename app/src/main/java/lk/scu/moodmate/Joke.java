package lk.scu.moodmate;

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
    private MaterialButton btnNewJoke;
    private JokeApiService apiService;

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

        btnNewJoke.setOnClickListener(v -> fetchJoke());

        // Load the first joke
        fetchJoke();
    }

    private void fetchJoke() {
        // Reset and show loading
        tvSetup.setText("Thinking of something funny...");
        tvPunchline.setText("");
        progressBar.setVisibility(View.VISIBLE);
        btnNewJoke.setEnabled(false);

        apiService.getRandomJoke().enqueue(new Callback<JokeResponse>() {
            @Override
            public void onResponse(Call<JokeResponse> call, Response<JokeResponse> response) {
                progressBar.setVisibility(View.GONE);
                btnNewJoke.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    tvSetup.setText(response.body().getSetup());
                    tvPunchline.setText(response.body().getPunchline());
                } else {
                    tvSetup.setText("Oops! My funny bone is broken.");
                    Toast.makeText(Joke.this, "Failed to get a joke", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JokeResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnNewJoke.setEnabled(true);
                tvSetup.setText("Check your internet, no jokes today!");
                Toast.makeText(Joke.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
