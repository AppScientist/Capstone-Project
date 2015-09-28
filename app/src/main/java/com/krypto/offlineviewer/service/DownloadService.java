package com.krypto.offlineviewer.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DownloadService extends IntentService {


    public DownloadService() {
        super("Download Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String mUrl = intent.getStringExtra(Constants.URL);
        boolean redirect = false;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

           URL mLink = new URL(mUrl);

            urlConnection = (HttpURLConnection) mLink.openConnection();
            urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            urlConnection.addRequestProperty("User-Agent", "Mozilla");
            urlConnection.addRequestProperty("Referer", "google.com");
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();


            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status ==    HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                    redirect = true;
            }

            if (redirect) {

                mUrl = urlConnection.getHeaderField("Location");

                urlConnection = (HttpURLConnection) new URL(mUrl).openConnection();
                urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                urlConnection.addRequestProperty("User-Agent", "Mozilla");
                urlConnection.addRequestProperty("Referer", "google.com");
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

            }

            InputStream inputStream = urlConnection.getInputStream();

            StringBuilder buffer = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }


            String webpageContent = buffer.toString();

            Utility.storeHtmlToCache(this, mUrl, webpageContent);

            Document doc = Jsoup.parse(webpageContent);
            Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
            List<String> imageList = new ArrayList<>(10);
            for (Element image : images) {

                imageList.add(image.attr("src"));

            }

            int size = imageList.size();

            for (int i = 0; i < size; i++) {

                String imagePath = imageList.get(i);
                if (imagePath.contains("http") || imagePath.contains("https")) {
                    urlConnection = (HttpURLConnection) new URL(imagePath).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    InputStream imageStream = urlConnection.getInputStream();

                    Utility.storeImagesToCache(DownloadService.this, mUrl, imagePath, imageStream);
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            Intent intent1= new Intent(Constants.DOWNLOADED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        }
    }
}
