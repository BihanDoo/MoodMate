package lk.scu.moodmate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Liked_favourites extends AppCompatActivity {

    private static final String TAG = "Liked_favourites";
    private RecyclerView rvInteractions;
    private ProgressBar pbLoading;
    private TextView tvNoData;
    private ImageButton btnBack;
    private InteractionAdapter adapter;
    private List<Interaction> interactionList;
    private FirebaseFirestore db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_liked_favourites);

        rvInteractions = findViewById(R.id.rvInteractions);
        pbLoading = findViewById(R.id.pbLoading);
        tvNoData = findViewById(R.id.tvNoData);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail", "");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        interactionList = new ArrayList<>();
        adapter = new InteractionAdapter(this, interactionList);
        rvInteractions.setLayoutManager(new LinearLayoutManager(this));
        rvInteractions.setAdapter(adapter);

        fetchInteractions();
    }

    private void fetchInteractions() {
        if (userEmail.isEmpty()) {
            tvNoData.setText("Please login to see interactions");
            tvNoData.setVisibility(View.VISIBLE);
            return;
        }

        pbLoading.setVisibility(View.VISIBLE);
        db.collection("interactions")
                .whereEqualTo("userEmail", userEmail)
                // Removed .orderBy("timestamp", Query.Direction.DESCENDING) to avoid index requirement
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pbLoading.setVisibility(View.GONE);
                    interactionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Interaction item = doc.toObject(Interaction.class);
                        interactionList.add(item);
                    }

                    // Sort locally by timestamp descending
                    Collections.sort(interactionList, (a, b) -> Long.compare(b.timestamp, a.timestamp));
                    
                    if (interactionList.isEmpty()) {
                        tvNoData.setVisibility(View.VISIBLE);
                    } else {
                        tvNoData.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    pbLoading.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching interactions: ", e);
                });
    }

    // Data Model Class
    public static class Interaction {
        public String contentType;
        public String type; // like or dislike
        public String setup; // for joke
        public String punchline; // for joke
        public String imageUrl; // for cat
        public String quoteText; // for quote
        public String quoteAuthor; // for quote
        public long timestamp;

        public Interaction() {} // Required for Firestore
    }

    // Adapter Class
    private static class InteractionAdapter extends RecyclerView.Adapter<InteractionAdapter.ViewHolder> {
        private List<Interaction> items;
        private Context context;

        public InteractionAdapter(Context context, List<Interaction> items) {
            this.context = context;
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interaction, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Interaction item = items.get(position);
            
            holder.tvType.setText(item.contentType != null ? item.contentType.toUpperCase() : "UNKNOWN");
            
            if ("joke".equals(item.contentType)) {
                holder.tvContent.setVisibility(View.VISIBLE);
                holder.ivCatThumb.setVisibility(View.GONE);
                holder.tvContent.setText(item.setup + "\n\n" + item.punchline);
            } else if ("cat".equals(item.contentType)) {
                holder.tvContent.setVisibility(View.GONE);
                holder.ivCatThumb.setVisibility(View.VISIBLE);
                Glide.with(context).load(item.imageUrl).placeholder(R.drawable.ic_launcher_background).into(holder.ivCatThumb);
            } else if ("quote".equals(item.contentType)) {
                holder.tvContent.setVisibility(View.VISIBLE);
                holder.ivCatThumb.setVisibility(View.GONE);
                holder.tvContent.setText("\"" + item.quoteText + "\"\n- " + item.quoteAuthor);
            }

            if (item.type != null) {
                holder.tvReaction.setText(item.type.toUpperCase());
                holder.tvReaction.setTextColor(item.type.equals("like") ? 0xFF4CAF50 : 0xFFF44336);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvContent, tvReaction;
            ImageView ivCatThumb;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvType);
                tvContent = itemView.findViewById(R.id.tvContent);
                tvReaction = itemView.findViewById(R.id.tvReaction);
                ivCatThumb = itemView.findViewById(R.id.ivCatThumb);
            }
        }
    }
}
