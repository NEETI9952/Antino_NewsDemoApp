package com.example.antino_newsdemoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.antino_newsdemoapp.Models.Article;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.text.ParseException;

public class CurrentPage extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {


    private ImageView imageView;
    private TextView appBarTitle, appBarSubtitle, date, time, title;
    private Boolean isHideTolBarView = false;
    private FrameLayout dateBehavior;
    private LinearLayout titleAppbar;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private String currentUrl, currentImage, currentTitle, currentDate, currentSource, currentAuthor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_page);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");

        appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(CurrentPage.this);
        dateBehavior = findViewById(R.id.date_behavior);
        titleAppbar = findViewById(R.id.title_appbar);
        imageView = findViewById(R.id.backdrop);
        appBarTitle=findViewById(R.id.title_on_appbar);
        appBarSubtitle= findViewById(R.id.subtitle_on_appbar);
        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        title = findViewById(R.id.title);

        Intent intent=getIntent();
        currentUrl=intent.getStringExtra("url");
        currentImage=intent.getStringExtra("img");
        currentTitle=intent.getStringExtra("title");
        currentAuthor=intent.getStringExtra("author");
        currentSource=intent.getStringExtra("source");
        currentDate=intent.getStringExtra("date");

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(Utils.getRandomDrawbleColor());

        Glide.with(this).load(currentImage).apply(requestOptions).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);

        appBarTitle.setText(currentSource);
        appBarSubtitle.setText(currentUrl);
        try {

             date.setText(Utils.DateFormat(currentDate));
        }catch(Exception e){}
        title.setText(currentTitle);

        String author = null;

        if(currentAuthor != null || currentAuthor != ""){
            currentAuthor=" \u2022 "+ currentAuthor;
        }else{
            author= "";
        }
        try {
            time.setText(currentSource + author +" \u2022 "+Utils.DateToTimeFormat(currentDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        initWebView(currentUrl);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset)/ (float) maxScroll;


        if(percentage == 1f  && isHideTolBarView){
            dateBehavior.setVisibility(View.GONE);
            titleAppbar.setVisibility(View.VISIBLE);
            isHideTolBarView = !isHideTolBarView;
        }else if (percentage < 1f && isHideTolBarView){
            dateBehavior.setVisibility(View.VISIBLE);
            titleAppbar.setVisibility(View.GONE);
            isHideTolBarView = !isHideTolBarView;
        }
    }
    private void initWebView(String url){
        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);




    }

}