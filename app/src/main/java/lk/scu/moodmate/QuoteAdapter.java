package lk.scu.moodmate;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> {

    ArrayList<QuoteModel> list;

    public QuoteAdapter(ArrayList<QuoteModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quote_item,
                        parent,
                        false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        QuoteModel model = list.get(position);

        holder.quote.setText(model.getQuote());

        holder.author.setText(model.getAuthor());

        holder.deleteBtn.setOnClickListener(v -> {

            FirebaseFirestore.getInstance()
                    .collection("favorite_quotes")
                    .document(model.getId())
                    .delete();

            list.remove(position);

            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView quote, author;

        Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            quote = itemView.findViewById(R.id.itemQuote);

            author = itemView.findViewById(R.id.itemAuthor);

            deleteBtn =
                    itemView.findViewById(R.id.deleteBtn);
        }
    }
}

