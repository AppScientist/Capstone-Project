package com.krypto.offlineviewer.widgets;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.krypto.offlineviewer.reading_list.MainActivity;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.storage.DataContract;

import java.net.MalformedURLException;
import java.net.URL;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(DataContract.ArticlesEntry.CONTENT_URI,
                        null, null, null, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {


                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                data.moveToPosition(position);
                int titleIndex = data.getColumnIndex(DataContract.ArticlesEntry.TITLE);
                final String title = data.getString(titleIndex);

                int urlIndex = data.getColumnIndex(DataContract.ArticlesEntry.URL);
                String url = data.getString(urlIndex);

                String newUrl = null;
                try {
                    URL link = new URL(url);
                    newUrl = link.getAuthority();
                } catch (MalformedURLException m) {
                    m.printStackTrace();
                }
                views.setTextViewText(R.id.articleTitle, title);
                views.setTextViewText(R.id.articleUrl, newUrl);

                Intent fillInIntent = new Intent(DetailWidgetRemoteViewsService.this, MainActivity.class);
                views.setOnClickFillInIntent(R.id.swipeable_content, fillInIntent);

                return views;
            }


            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
