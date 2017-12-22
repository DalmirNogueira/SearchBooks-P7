package xyz.dnglabs.searchbooks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {
    private QueryUtils() {
    }
    public static List<Book> fetchBookData(String json) {
        String authors = "";
        List<Book> books = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(json);
            if (jsonResponse.getInt("totalItems") == 0) {
                return books;
            }
            JSONArray jsonArray = jsonResponse.getJSONArray("items");

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject bookObject = jsonArray.getJSONObject(i);
                JSONObject bookInfo = bookObject.getJSONObject("volumeInfo");
                String title = "";
                if(bookInfo.has("title")){
                    title = bookInfo.getString("title");
                }
                if(bookInfo.has("authors")){
                    JSONArray authorsArray = bookInfo.getJSONArray("authors");
                    if (authorsArray.length() == 0) {
                    } else {
                        for (int i2 = 0; i2 < authorsArray.length(); i2++) {
                            if (i2 == 0) {
                                authors = authorsArray.getString(0);
                            } else {
                                authors += " , " + authorsArray.getString(i2);
                            }
                        }
                    }
                }

                Book book = new Book(authors, title);
                books.add(book);
            }
        } catch (JSONException e) {
        }
        return books;
    }
}
