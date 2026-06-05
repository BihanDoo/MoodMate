package lk.scu.moodmate;

package com.example.brainbreak;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SavedQuotesActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    ArrayList<QuoteModel> list;

    QuoteAdapter adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_quotes);

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        list = new ArrayList<>();

        adapter = new QuoteAdapter(list);

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadQuotes();
    }

    private void loadQuotes() {

        db.collection("favorite_quotes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    queryDocumentSnapshots.forEach(document -> {

                        QuoteModel model =
                                new QuoteModel(
                                        document.getId(),
                                        document.getString("quote"),
                                        document.getString("author")
                                );

                        list.add(model);

                    });

                    adapter.notifyDataSetChanged();

                });
    }
}

