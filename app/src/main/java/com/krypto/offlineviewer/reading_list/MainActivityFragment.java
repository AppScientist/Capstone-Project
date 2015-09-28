package com.krypto.offlineviewer.reading_list;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.storage.DataContract;
import com.krypto.offlineviewer.webview.WebViewActivity;
import com.krypto.offlineviewer.webview.WebViewActivityFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ActionMode.Callback, WebPageAdapter.OnItemClickListener, WebPageAdapter.OnItemLongClickListener {

    private WebPageAdapter mAdapter;
    private ActionMode mActionMode;
    private ViewDestroyed mViewDestroyed;
    private static final int LOADER = 0;
    private List<Integer> mCheckedItems = new ArrayList<>();

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Bind(R.id.empty_state)
    LinearLayout mLinearLayout;

    @BindBool(R.bool.Tablet)
    boolean isTablet;

    @BindBool(R.bool.Land)
    boolean isLand;


    public MainActivityFragment() {
    }

    public interface ViewDestroyed {

        void activityDestroyed();
    }

    public static MainActivityFragment newInstance() {

        Bundle args = new Bundle();
        MainActivityFragment fragment = new MainActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, rootView);


        mAdapter = new WebPageAdapter(null, getContext(), this);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mViewDestroyed = (SimpleItemTouchHelperCallback) callback;
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.setAdapter(mAdapter);


        mLinearLayout.setVisibility(View.VISIBLE);

        mAdapter.registerAdapterDataObserver(observer);

        return rootView;
    }

    @Override
    public void onItemClicked(int position) {


        if (mActionMode != null) {
            myToggleSelection(position);
        } else {

            Cursor cursor = mAdapter.getCursor();
            cursor.moveToPosition(position);
            int urlIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.URL);
            String url = cursor.getString(urlIndex);
            int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
            String title = cursor.getString(titleIndex);
            if (!(isTablet && isLand)) {
                Intent serviceIntent = new Intent(getContext(), WebViewActivity.class);
                serviceIntent.putExtra(Constants.URL, url);
                serviceIntent.putExtra(Constants.TITLE, title);
                getActivity().startActivity(serviceIntent);
            } else {

                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.webView_fragment, WebViewActivityFragment.newInstancewithArgs(title, url)).commit();
            }
        }

    }

    @Override
    public boolean onItemLongClicked(int position) {

        if (mActionMode != null) {
            return false;
        }

        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(MainActivityFragment.this);

        myToggleSelection(position);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mAdapter != null && mAdapter.getSelectedItemCount() > 0) {
            List<Integer> selectedItems = mAdapter.getSelectedItems();
            outState.putIntegerArrayList(Constants.SELECTED_ITEMS, (ArrayList<Integer>) selectedItems);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER, null, this);
        if (savedInstanceState != null) {
            mCheckedItems = savedInstanceState.getIntegerArrayList(Constants.SELECTED_ITEMS);
            if (mCheckedItems != null && mCheckedItems.size() > 0) {
                mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);

                for (int i = 0; i < mCheckedItems.size(); i++) {
                    int position = mCheckedItems.get(i);
                    mAdapter.toggleSelection(position);
                }

                mActionMode.setTitle(getString(R.string.items_selected, mAdapter.getSelectedItemCount()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(LOADER, null, this);

    }


    public void updateAdapter(int pos) {
        mAdapter.remove(pos);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                DataContract.ArticlesEntry.CONTENT_URI
                , null
                , null
                , null
                , null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {


    }

    RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();

            if (mAdapter.getItemCount() == 0) {
                mLinearLayout.setVisibility(View.VISIBLE);
            } else {
                mLinearLayout.setVisibility(View.GONE);
            }
        }
    };

    public void moveRecyclerView() {

        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
    }


    private void myToggleSelection(int idx) {
        mAdapter.toggleSelection(idx);
        if (mAdapter.getSelectedItemCount() != 0) {
            String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
            mActionMode.setTitle(title);
        } else {
            mActionMode.finish();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        if (item.getItemId() == R.id.menu_delete) {
            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            int currPos;

            Cursor cursor = mAdapter.getCursor();
            int size = selectedItemPositions.size() - 1;

            List<String> urlList = new ArrayList<>(selectedItemPositions.size());
            for (int i = size; i >= 0; i--) {
                currPos = selectedItemPositions.get(i);

                cursor.moveToPosition(currPos);

                int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
                String title = cursor.getString(titleIndex);

                int urlIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.URL);
                final String url = cursor.getString(urlIndex);

                urlList.add(url);
                getActivity().getContentResolver().delete(DataContract.ArticlesEntry.CONTENT_URI, "article_title = ?", new String[]{title});
                updateAdapter(currPos);

                Utility.deleteCache(getContext(), url);
                WebViewActivityFragment fragment = (WebViewActivityFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.webView_fragment);
                if (fragment != null) {

                    String displayedUrl = fragment.getUrl();
                    int capacity = urlList.size();
                    for (int j = 0; j < capacity; j++) {

                        if (displayedUrl.equals(urlList.get(j))) {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                            break;
                        }
                    }

                }
            }

            mode.finish();
            return true;

        } else if (item.getItemId() == R.id.select_all) {
            mAdapter.selectAll();
            String title = getString(R.string.selected_count, mAdapter.getSelectedItemCount());
            mActionMode.setTitle(title);

            return true;
        }
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {

        mActionMode = null;
        mAdapter.clearSelections();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewDestroyed.activityDestroyed();
        ButterKnife.unbind(this);
        mAdapter.unregisterAdapterDataObserver(observer);

    }
}
