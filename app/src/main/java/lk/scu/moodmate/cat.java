package lk.scu.moodmate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Model class for Cat API response
 */
class CatImage {
    private String url;
    public String getUrl() { return url; }
}

/**
 * Retrofit interface for Cat API
 */
interface CatApiService {
    @GET("v1/images/search")
    Call<List<CatImage>> getRandomCat();
}

public class cat extends AppCompatActivity {

    private ImageView ivCat;
    private ProgressBar progressBar;
    private MaterialButton btnNewCat;
    private CatApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat);

        // Bind UI components
        ivCat = findViewById(R.id.ivCat);
        progressBar = findViewById(R.id.progressBar);
        btnNewCat = findViewById(R.id.btnNewCat);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.thecatapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CatApiService.class);

        btnNewCat.setOnClickListener(v -> fetchCatImage());

        // Load initial image
        fetchCatImage();
    }

    private void fetchCatImage() {
        progressBar.setVisibility(View.VISIBLE);
        btnNewCat.setEnabled(false);

        apiService.getRandomCat().enqueue(new Callback<List<CatImage>>() {
            @Override
            public void onResponse(Call<List<CatImage>> call, Response<List<CatImage>> response) {
                progressBar.setVisibility(View.GONE);
                btnNewCat.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String imageUrl = response.body().get(0).getUrl();
                    Glide.with(cat.this)
                            .load(imageUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivCat);
                } else {
                    Toast.makeText(cat.this, "Failed to load cat image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CatImage>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnNewCat.setEnabled(true);
                Toast.makeText(cat.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
