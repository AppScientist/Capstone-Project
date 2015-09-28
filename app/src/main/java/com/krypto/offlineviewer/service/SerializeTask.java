package com.krypto.offlineviewer.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;

import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.model.Articles.Articles;
import com.krypto.offlineviewer.storage.DataContract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SerializeTask extends IntentService {


    public SerializeTask() {
        super("Serialize");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        List<String> selectedItems = intent.getStringArrayListExtra(Constants.SELECTED_ITEMS);

        int size = selectedItems.size();

        List<Articles> newList = new ArrayList<>(size);
        Cursor cursor = null;
        for (int i = 0; i < size; i++) {

            cursor = getContentResolver().query(DataContract.ArticlesEntry.CONTENT_URI, null, "article_title = ?", new String[]{selectedItems.get(i)}, null);

            cursor.moveToFirst();
            int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
            final String title = cursor.getString(titleIndex);

            int urlIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.URL);
            final String url = cursor.getString(urlIndex);

            int descIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.DESC);
            final String desc = cursor.getString(descIndex);

            int imageIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.IMAGE);
            final String image = cursor.getString(imageIndex);

            Articles articles = new Articles(title, url, desc, image);
            newList.add(articles);

        }

        if (cursor != null)
            cursor.close();

        byte[] listBytes = null;
        try {
            listBytes = Utility.serialize(newList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent1 = new Intent(Constants.BYTE_RECORD);
        intent1.putExtra(Constants.BYTES, listBytes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
    }

}
