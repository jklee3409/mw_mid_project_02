package com.example.photoviewer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText searchEdit;
    private Spinner sortSpinner;
    private static final String API_URL = "http://10.0.2.2:8000/api_root/Post/";
//    private static final String API_URL = "https://jklee3409.pythonanywhere.com/api_root/Post/";
    private static final String TOKEN = "1c5fe424c16b5ffabcaa038d934625e3573cc4a499ffedce3bf7313fcda4b7d8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        searchEdit = findViewById(R.id.searchEdit);
        sortSpinner = findViewById(R.id.sortSpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PostAdapter(this, posts);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> loadPosts());

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"날짜↑", "날짜↓", "제목↑", "제목↓"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: adapter.sort(true, true); break;
                    case 1: adapter.sort(false, true); break;
                    case 2: adapter.sort(true, false); break;
                    case 3: adapter.sort(false, false); break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void onClickDownload(View v) {
        loadPosts();
    }

    public void onClickUpload(View v) {
        startActivity(new Intent(MainActivity.this, UploadActivity.class));
    }

    @SuppressLint("StaticFieldLeak")
    private void loadPosts() {
        new AsyncTask<Void, Void, List<Post>>() {
            @Override
            protected void onPreExecute() {
                swipeRefreshLayout.setRefreshing(true);
            }
            @Override
            protected List<Post> doInBackground(Void... voids) {
                List<Post> result = new ArrayList<>();
                try {
                    URL url = new URL(API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Token " + TOKEN);
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    int code = conn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONArray array = new JSONArray(sb.toString());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String title = obj.optString("title");
                            String text = obj.optString("text");
                            String image = obj.optString("image");
                            String created = obj.optString("created_date");
                            result.add(new Post(title, text, image, created));
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return result;
            }
            @Override
            protected void onPostExecute(List<Post> res) {
                swipeRefreshLayout.setRefreshing(false);
                if (res.isEmpty()) {
                    Toast.makeText(MainActivity.this, "게시물이 없습니다.", Toast.LENGTH_SHORT).show();
                }
                posts.clear();
                posts.addAll(res);
                adapter.filter(searchEdit.getText().toString());
            }
        }.execute();
    }
}
