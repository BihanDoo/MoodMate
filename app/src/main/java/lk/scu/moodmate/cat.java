package lk.scu.moodmate;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private MaterialButton btnNewCat, btnLike, btnDislike;
    private CatApiService apiService;
    private FirebaseFirestore db;
    private String userEmail;
    private String currentImageUrl;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cat);

        // Bind UI components
        ivCat = findViewById(R.id.ivCat);
        progressBar = findViewById(R.id.progressBar);
        btnNewCat = findViewById(R.id.btnNewCat);
        btnLike = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);

        ivCat.setImageDrawable(getResources().getDrawable(R.drawable.background_17));

        // Initialize Firestore and Session
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail", "anonymous");

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

        btnNewCat.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            fetchCatImage();
        });

        btnLike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.setBackgroundColor(getResources().getColor(R.color.cat_button));
            saveInteraction("like");
        });

        btnDislike.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            v.setBackgroundColor(Color.parseColor("#FF5252"));
            btnDislike.setTextColor(Color.parseColor("#FFFFFF"));
            saveInteraction("dislike");
        });

        // Load initial image
        fetchCatImage();
    }

    private void fetchCatImage() {
        progressBar.setVisibility(View.VISIBLE);
        btnNewCat.setEnabled(false);
        btnLike.setEnabled(false);
        btnDislike.setEnabled(false);

        apiService.getRandomCat().enqueue(new Callback<List<CatImage>>() {
            @Override
            public void onResponse(Call<List<CatImage>> call, Response<List<CatImage>> response) {
                progressBar.setVisibility(View.VISIBLE);
                btnNewCat.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentImageUrl = response.body().get(0).getUrl();
                    
                    // Preload the image to improve smoothness
                    Glide.with(cat.this)
                            .load(currentImageUrl)
                            .placeholder(R.drawable.background_17)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .thumbnail(Glide.with(cat.this).load(currentImageUrl).sizeMultiplier(0.1f))
                            .into(ivCat);

                    btnLike.setEnabled(true);
                    btnDislike.setEnabled(true);

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                    }, 1500);
                } else {
                    Snackbar.make(findViewById(R.id.main), "Failed to load cat image", Snackbar.LENGTH_SHORT).show();
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                    }, 1000);
                }
            }

            @Override
            public void onFailure(Call<List<CatImage>> call, Throwable t) {
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                }, 1000);
                btnNewCat.setEnabled(true);
                Snackbar.make(findViewById(R.id.main), "Network Error: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void saveInteraction(String type) {
        if (currentImageUrl == null || userEmail == null) return;

        Map<String, Object> interaction = new HashMap<>();
        interaction.put("userEmail", userEmail);
        interaction.put("type", type);
        interaction.put("contentType", "cat");
        interaction.put("imageUrl", currentImageUrl);
        interaction.put("timestamp", System.currentTimeMillis());

        db.collection("interactions")
                .add(interaction)
                .addOnSuccessListener(documentReference -> {
                    // Success feedback: Haptic + Snackbar
                    View rootView = findViewById(R.id.main);

                    if (rootView != null) {
                        rootView.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        Snackbar.make(rootView, "Cat image " + type + "d!", Snackbar.LENGTH_SHORT)
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
