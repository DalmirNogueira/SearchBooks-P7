package xyz.dnglabs.searchbooks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    EditText inputTxt;
    ImageButton searchButton;
    BookAdapter adapter;
    ListView bookListView;
    TextView mEmptyStateTextView;
    static final String ID = "";
    int setRotation = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputTxt = (EditText) findViewById(R.id.search);
        searchButton = (ImageButton) findViewById(R.id.search_button);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        adapter = new BookAdapter(this, -1);
        bookListView = (ListView) findViewById(R.id.list);
        bookListView.setAdapter(adapter);

        searchButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternetConnectionAvailable()){
                    mEmptyStateTextView.setVisibility(View.GONE);
                    bookListView.setVisibility(View.VISIBLE);
                    setRotation = 0;
                    BookSearch searchBooks = new BookSearch();
                    searchBooks.execute();
                } else {
                    bookListView.setVisibility(View.GONE);
                    setRotation = 1;
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });
        if (savedInstanceState != null && setRotation == 0) {
            mEmptyStateTextView.setVisibility(View.GONE);
            bookListView.setVisibility(View.VISIBLE);
            Book[] books = (Book[]) savedInstanceState.getParcelableArray(ID);
            adapter.addAll(books);
        }
    }
    private boolean isInternetConnectionAvailable(){
        ConnectivityManager netInfo = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netconnected = netInfo.getActiveNetworkInfo();
        if (netconnected != null && netconnected.isConnected()) {
            return true;
        }
            return false;
    }
    private void setList(List<Book> books){
        if (books != null && books.isEmpty()){
            bookListView.setVisibility(View.GONE);
            setRotation = 2;
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setText(R.string.no_books);
        } else {
            mEmptyStateTextView.setVisibility(View.GONE);
            bookListView.setVisibility(View.VISIBLE);
            setRotation = 0;
        }
        adapter.clear();
        adapter.addAll(books);
    }

    private String getUrlForHttpRequest() {//change place
        final String baseUrl = "https://www.googleapis.com/books/v1/volumes?q=";
        String url = baseUrl + inputTxt.getText().toString();
        return url;
    }

    private class BookSearch extends AsyncTask<URL, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(URL... urls) {
            URL url = null;
            try {
                url = new URL(getUrlForHttpRequest());
            } catch (MalformedURLException e) {
            }
            String jsonResponse = "";

            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
            }
            List<Book> books;
            if (jsonResponse == null) {
                books = null;
            }else{
                books =  QueryUtils.fetchBookData(jsonResponse);
            }

            return books;
        }
        @Override
        protected void onPostExecute(List<Book> books) {
            if (books == null) {
                mEmptyStateTextView.setText(R.string.no_books);
                return;
            }else {
                setList(books);
            }
        }
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            if (url == null){
                return jsonResponse;
            }
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200){
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {

                }
            } catch (IOException e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Book[] books = new Book[adapter.getCount()];
        for (int i = 0; i < books.length; i++) {
            books[i] = adapter.getItem(i);
        }
        outState.putParcelableArray(ID, (Parcelable[]) books);
    }
}