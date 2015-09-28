package com.krypto.offlineviewer.reading_list;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.storage.DataContract;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class WebPageAdapter extends RecyclerView.Adapter<WebPageViewHolder> {

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private Cursor mCursor;
    private Context mContext;
    private boolean mSelectAll;
    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;
    private static final int TYPE_WITH_IMAGE = 1;
    private static final int TYPE_WITHOUT_IMAGE = 2;

    public WebPageAdapter(Cursor cursor, Context context, Fragment fragment) {
        mCursor = cursor;
        mContext = context;
        mLongClickListener = (OnItemLongClickListener) fragment;
        mClickListener = (OnItemClickListener) fragment;
    }

    @Override
    public WebPageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType == TYPE_WITH_IMAGE)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_items_with_image, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_items, parent, false);

        return new WebPageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final WebPageViewHolder holder, final int position) {


        holder.itemView.setActivated(mSelectedItems.get(position, false));

        mCursor.moveToPosition(position);

        int titleIndex = mCursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
        String title = mCursor.getString(titleIndex);

        int urlIndex = mCursor.getColumnIndex(DataContract.ArticlesEntry.URL);
        String url = mCursor.getString(urlIndex);

        int descIndex = mCursor.getColumnIndex(DataContract.ArticlesEntry.DESC);
        final String desc = mCursor.getString(descIndex);

        int imageIndex = mCursor.getColumnIndex(DataContract.ArticlesEntry.IMAGE);
        final String image = mCursor.getString(imageIndex);

        try {
            URL url1 = new URL(url);
            url = url1.getAuthority();
        } catch (MalformedURLException m) {
            m.printStackTrace();
        }

        holder.mTitle.setText(Html.fromHtml(title).toString());
        holder.mUrl.setText(url);

        holder.mDesciption.setText(Html.fromHtml(desc).toString());

        ColorGenerator generator = ColorGenerator.MATERIAL;

        String letter = String.valueOf(title.charAt(0));

        TextDrawable drawable1 = TextDrawable.builder()
                .buildRound(letter, generator.getColor(title));


        if (holder.getItemViewType() == TYPE_WITHOUT_IMAGE)
            holder.marker.setImageDrawable(drawable1);
        else

            Glide.with(mContext).load(image).into(holder.marker);


        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mClickListener.onItemClicked(position);
            }
        });

        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mLongClickListener.onItemLongClicked(position);
                return true;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (mCursor != null) {

            mCursor.moveToPosition(position);
            int imageIndex = mCursor.getColumnIndex(DataContract.ArticlesEntry.IMAGE);
            final String image = mCursor.getString(imageIndex);

            return image != null ? TYPE_WITH_IMAGE : TYPE_WITHOUT_IMAGE;

        } else
            return super.getItemViewType(position);
    }

    public void remove(int position) {

        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void toggleSelection(int pos) {
        if (mSelectedItems.get(pos, false)) {
            mSelectedItems.delete(pos);
        } else {
            mSelectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {

        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            boolean selected = mSelectedItems.valueAt(i);
            if (selected)
                items.add(mSelectedItems.keyAt(i));
        }

        return items.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            boolean selected = mSelectedItems.valueAt(i);
            if (selected)
                items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }

    public void selectAll() {


        int size = mCursor.getCount();
        if (!mSelectAll) {
            for (int i = 0; i < size; i++) {
                mSelectedItems.put(i, true);
                mSelectAll = true;
            }
        } else {
            for (int i = 0; i < size; i++) {
                mSelectedItems.put(i, false);
                mSelectAll = false;
            }
        }
        notifyDataSetChanged();

    }

    public Cursor getCursor() {

        return mCursor;
    }

    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClicked(int position);
    }
}
