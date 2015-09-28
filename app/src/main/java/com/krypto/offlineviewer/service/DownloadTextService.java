package com.krypto.offlineviewer.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.krypto.offlineviewer.BuildConfig;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.model.Articles.ArticleContent;

import java.io.IOException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


public class DownloadTextService extends IntentService {


    public DownloadTextService() {
        super("DownloadTextService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String url = intent.getStringExtra(Constants.URL);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.readability))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UrlInterfaces.APIService service = retrofit.create(UrlInterfaces.APIService.class);
        Call<ArticleContent> call = service.loadRepo(url, BuildConfig.READABILITY_KEY);

        try {
            Response<ArticleContent> response = call.execute();
            ArticleContent articles = response.body();
            Utility.storeTextToCache(this, url, articles);

            Intent intent1= new Intent(Constants.DOWNLOADED_TEXT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
