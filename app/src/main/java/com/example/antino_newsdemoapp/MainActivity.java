package com.example.antino_newsdemoapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.antino_newsdemoapp.Api.ApiClient;
import com.example.antino_newsdemoapp.Api.ApiInterface;
import com.example.antino_newsdemoapp.Models.Article;
import com.example.antino_newsdemoapp.Models.News;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
//import android.app.SearchManager;
//import android.widget.SearchView;
//import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static DocumentReference newsApiKeyRef=db.collection("Antino").document("NewsApiKey");

    public static String API_KEY = "34c3bea4d49c4122bab34ea3b62a57f6";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private SwipeRefreshLayout swipeRefreshLayout;
    private String mkeyword = "";
    private ChipGroup chipGroup;
    private Boolean chipSelected = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getApiKeyFromFirestore();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(MainActivity.this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        chipGroup = findViewById(R.id.chip_group);

        onLoadingSwipeRefresh(mkeyword);

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {

                Chip chip = chipGroup.findViewById(i);

                if (chip != null) {
                    chipSelected = true;
                    mkeyword = chip.getText().toString();
                    loadJson(mkeyword);
                } else {
                    mkeyword = "";
                    chipSelected = false;
                    loadJson(mkeyword);
                }

            }
        });

    }

    private void getApiKeyFromFirestore() {
        newsApiKeyRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                API_KEY=documentSnapshot.get("ApiKey").toString();
            }
        });
    }

    public void loadJson(final String keyword) {
        swipeRefreshLayout.setRefreshing(false);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String country = Utils.getCountry();
        String language = Utils.getLanguage();

        Call<News> call;

        if (keyword.length() > 0) {
            call = apiInterface.getNewsSearch(keyword, language, "publishedAt", API_KEY);
        } else {
            call = apiInterface.getNews(country, API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null) {

                    if (!articles.isEmpty()) {
                        articles.clear();
                    }
                    articles = response.body().getArticle();

                    adapter = new Adapter(MainActivity.this, articles);
                    recyclerView.setAdapter(adapter);
                    initListener();
                    adapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);

                } else {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.actionSearch).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.actionSearch);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News");
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipGroup.clearCheck();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2) {
                    chipGroup.setVisibility(View.GONE);
//
                    mkeyword = query;
                    onLoadingSwipeRefresh(mkeyword);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mkeyword = newText;

                loadJson(mkeyword);
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false, false);
        return true;
    }

    @Override
    public void onRefresh() {
        if (mkeyword.length() > 2 && mkeyword == "") {
            loadJson(mkeyword);
        } else {
            loadJson(mkeyword);
        }
    }

    private void onLoadingSwipeRefresh(final String keyword) {
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadJson(keyword);
                    }
                }
        );
    }

    private void initListener() {
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {

                Intent intent = new Intent(MainActivity.this, CurrentPage.class);
                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                startActivity(intent);
            }
        });


    }
}