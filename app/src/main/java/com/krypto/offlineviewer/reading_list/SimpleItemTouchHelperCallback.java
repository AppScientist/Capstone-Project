package com.krypto.offlineviewer.reading_list;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.storage.DataContract;


public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback implements MainActivityFragment.ViewDestroyed {


    private Paint mPaint;
    private Bitmap fullBitmap, resizedBitmap;
    private MainActivityFragment mFragment;
    private Snackbar mSnackbar;
    private boolean delete;

    public SimpleItemTouchHelperCallback(MainActivityFragment fragment) {

        mFragment = fragment;
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(mFragment.getContext(), R.color.accent));
        fullBitmap = BitmapFactory.decodeResource(mFragment.getActivity().getResources(), R.drawable.ic_delete_white_24dp);
        resizedBitmap = Bitmap.createScaledBitmap(fullBitmap, 75, 75, false);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

        final Cursor cursor = mFragment.getActivity().getContentResolver().query(DataContract.ArticlesEntry.CONTENT_URI, null, null, null, null);
        cursor.moveToPosition(viewHolder.getAdapterPosition());

        int idIndex = cursor.getColumnIndex(DataContract.ArticlesEntry._ID);
        final long id = cursor.getLong(idIndex);

        int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
        final String title = cursor.getString(titleIndex);

        int urlIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.URL);
        final String url = cursor.getString(urlIndex);

        int descIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.DESC);
        final String desc = cursor.getString(descIndex);

        int imageIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.IMAGE);
        final String image = cursor.getString(imageIndex);

        mFragment.getActivity().getContentResolver().delete(DataContract.ArticlesEntry.CONTENT_URI, "article_title = ?", new String[]{title});
        mFragment.updateAdapter(viewHolder.getAdapterPosition());

        delete = true;

        mSnackbar = Snackbar.make(viewHolder.itemView, mFragment.getString(R.string.item_deleted), Snackbar.LENGTH_SHORT);
        mSnackbar.setAction(mFragment.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues values = new ContentValues();
                values.put(DataContract.ArticlesEntry._ID, id);
                values.put(DataContract.ArticlesEntry.TITLE, title);
                values.put(DataContract.ArticlesEntry.URL, url);
                values.put(DataContract.ArticlesEntry.DESC, desc);
                values.put(DataContract.ArticlesEntry.IMAGE, image);

                delete = false;

                mFragment.getActivity().getContentResolver().insert(DataContract.ArticlesEntry.CONTENT_URI, values);
            }
        }).setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (delete)
                    Utility.deleteCache(mFragment.getContext(), url);
            }
        }).show();

        cursor.close();
    }


    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

            View itemView = viewHolder.itemView;

            float height = (float) itemView.getTop() + (itemView.getHeight() / 2) - (resizedBitmap.getHeight() / 2);

            if (dX > 0) {

                float width = (float) itemView.getLeft() + (resizedBitmap.getWidth() / 2);
                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), mPaint);
                c.drawBitmap(resizedBitmap, width, height, null);

            } else if (dX < 0) {

                float width = (float) itemView.getRight() - (resizedBitmap.getWidth() / 2);
                float bitmapWidth = resizedBitmap.getWidth();
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(),
                        (float) itemView.getBottom(), mPaint);
                c.drawBitmap(resizedBitmap, width - bitmapWidth, height, null);

            }
            fullBitmap.recycle();

        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }


    @Override
    public void activityDestroyed() {

        if (mSnackbar != null)
            mSnackbar.dismiss();
    }
}
