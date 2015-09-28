package com.krypto.offlineviewer.search;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.reading_list.WebPageAdapter;
import com.krypto.offlineviewer.storage.DataContract;
import com.krypto.offlineviewer.webview.WebViewActivity;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SearchFragment extends Fragment implements WebPageAdapter.OnItemClickListener, WebPageAdapter.OnItemLongClickListener {

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.empty_state)
    LinearLayout mLinearLayout;

    private WebPageAdapter mAdapter;
    Cursor mCursor;

    public static SearchFragment newInstance(String searchItem) {

        Bundle args = new Bundle();
        args.putString(Constants.SEARCH, searchItem);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);

        String text = null;
        if (getArguments() != null) {
            text = getArguments().getString(Constants.SEARCH);
        }

        mCursor = getActivity().getContentResolver().query(DataContract.ArticlesEntry.CONTENT_URI, null
                , DataContract.ArticlesEntry.TABLE + "." + DataContract.ArticlesEntry.TITLE + " LIKE ? OR "
                + DataContract.ArticlesEntry.TABLE + "." + DataContract.ArticlesEntry.URL + " LIKE ?"
                , new String[]{"%" + text + "%", "%" + text + "%"}
                , null);
        if (mCursor.getCount() == 0) {
            mLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mLinearLayout.setVisibility(View.GONE);
        }
        mAdapter = new WebPageAdapter(mCursor, getContext(), this);

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }


    @Override
    public void onItemClicked(int position) {

        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        int urlIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.URL);
        String url = cursor.getString(urlIndex);
        Intent serviceIntent = new Intent(getContext(), WebViewActivity.class);
        serviceIntent.putExtra(Constants.URL, url);
        getActivity().startActivity(serviceIntent);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLinearLayout.setVisibility(View.GONE);
        mCursor.close();
        ButterKnife.unbind(this);

    }
}
