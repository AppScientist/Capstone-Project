package com.krypto.offlineviewer.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.krypto.offlineviewer.BuildConfig;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.model.Articles.ArticleContent;
import com.krypto.offlineviewer.storage.DataContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


public class WebpageService extends IntentService {

    public static final String ACTION_DATA_UPDATED =
            "com.krypto.offlineviewer.ACTION_DATA_UPDATED";

    public WebpageService() {
        super("Webpage Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String mUrl = intent.getStringExtra(Constants.URL);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.readability))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UrlInterfaces.APIService service = retrofit.create(UrlInterfaces.APIService.class);
            Call<ArticleContent> call = service.loadRepo(mUrl, BuildConfig.READABILITY_KEY);

            Response<ArticleContent> response = call.execute();
            ArticleContent articles = response.body();
            String description = articles.getExcerpt();
            String imageUrl = articles.getLeadImageUrl();
            String title = articles.getTitle();

            Utility.storeTextToCache(this, mUrl, articles);

            ContentValues values = new ContentValues();
            values.put(DataContract.ArticlesEntry.TITLE, title);
            values.put(DataContract.ArticlesEntry.URL, mUrl);
            values.put(DataContract.ArticlesEntry.DESC, description);
            values.put(DataContract.ArticlesEntry.IMAGE, imageUrl);

            getContentResolver().insert(DataContract.ArticlesEntry.CONTENT_URI, values);

            Intent intent1 = new Intent(Constants.COMPLETED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

            updateWidgets();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {e.printStackTrace();
                }
            }


        }
    }

    private void updateWidgets() {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(this.getPackageName());
        this.sendBroadcast(dataUpdatedIntent);
    }
}
