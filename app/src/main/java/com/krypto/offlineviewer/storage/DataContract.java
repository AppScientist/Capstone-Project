package com.krypto.offlineviewer.storage;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


public class DataContract {


    public static final String AUTHORITY = "com.krypto.offlineviewer";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String ARTICLES = "articles";

    public static class ArticlesEntry implements BaseColumns {

        public static final String TABLE = "Articles";

        public static final String TITLE = "article_title";

        public static final String URL = "article_url";

        public static final String DESC="article_description";

        public static final String IMAGE="image_url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(ARTICLES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + AUTHORITY + "/" + ARTICLES;

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
