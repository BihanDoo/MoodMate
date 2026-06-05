package lk.scu.moodmate;
public class QuoteModel {

    String id;
    String quote;
    String author;

    public QuoteModel() {
    }

    public QuoteModel(String id,
                      String quote,
                      String author) {

        this.id = id;
        this.quote = quote;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }
}

