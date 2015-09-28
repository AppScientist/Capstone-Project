package com.krypto.offlineviewer.nearby;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.krypto.offlineviewer.Utilities.ConnectionHelper;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.ErrorCheckingCallback;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.service.SerializeTask;
import com.krypto.offlineviewer.storage.DataContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NearbySendFragment extends Fragment implements ConnectionHelper.Connection, LoaderManager.LoaderCallbacks<Cursor> {

    private ListViewAdapter mAdapter;
    private ConnectionHelper mHelper;
    private BroadcastReceiver mReceiver;

    private boolean isConnected;
    private View mView;
    private Animation mAnimation;

    private GoogleApiClient mClient;

    private Snackbar mSnackbar;
    private static final int LOADER = 1;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEdit;

    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Bind(R.id.listView)
    ListView mListView;

    @Bind(R.id.animation)
    ImageView mAnim;

    public static NearbySendFragment newInstance() {

        Bundle args = new Bundle();
        NearbySendFragment fragment = new NearbySendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbySendFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_nearby_send, container, false);


        ButterKnife.bind(this, mView);

        mHelper = ConnectionHelper.attach(this);
        mClient = mHelper.buildGoogleApiClient(getActivity());


        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEdit = mPreferences.edit();

        mAdapter = new ListViewAdapter(getActivity(), R.layout.send_items, null, new String[]{DataContract.ArticlesEntry.TITLE}
                , new int[]{R.id.title}, 0);


        mListView.setAdapter(mAdapter);

        return mView;
    }


    @Override
    public void onConnected() {

        isConnected = true;
        Nearby.Messages.getPermissionStatus(mClient).setResultCallback(new ErrorCheckingCallback(getActivity()));
    }

    public void permissionAccepted(boolean permission) {

        mEdit.putBoolean(Constants.PERMISSION, permission).apply();
        mEdit.putBoolean(Constants.CHECKED, true).apply();
    }

    void startAnimation() {
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alpha);
        mAnim.setVisibility(View.VISIBLE);
        mAnim.startAnimation(mAnimation);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getContext()
                , DataContract.ArticlesEntry.CONTENT_URI
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

        mAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getSupportLoaderManager().restartLoader(LOADER, null, this);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                byte[] data = intent.getByteArrayExtra(Constants.BYTES);
                Message message = new Message(data);
                mHelper.publish(message);
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(Constants.BYTE_RECORD));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportLoaderManager().initLoader(LOADER, null, this);
        if (savedInstanceState != null) {
            List<Integer> selectedItems = savedInstanceState.getIntegerArrayList(Constants.SELECTED_ITEMS);
            if (selectedItems != null && selectedItems.size() > 0) {

                for (int i = 0; i < selectedItems.size(); i++) {
                    int position = selectedItems.get(i);
                    mAdapter.toggleSelection(position);
                }
            }
        }
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
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        if (mSnackbar != null)
            mSnackbar.dismiss();

    }

    void stopAnimation() {
        mAnim.clearAnimation();
        mAnim.setVisibility(View.GONE);
    }

    void unsubscribe() {
        mHelper.unsubscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAnimation != null && !mAnimation.hasEnded()) {
            stopAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

    }

    MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopAnimation();
                    unsubscribe();
                }
            });
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nearby_messages, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.send_msg) {

            if (isConnected && mPreferences.getBoolean(Constants.PERMISSION, false)) {
                mHelper.subscribe(mMessageListener);
                List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
                if (!selectedItemPositions.isEmpty()) {
                    startAnimation();
                    Cursor cursor = mAdapter.getCursor();
                    int size = selectedItemPositions.size();
                    List<String> titles = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {

                        cursor.moveToPosition(selectedItemPositions.get(i));

                        int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
                        String title = cursor.getString(titleIndex);
                        titles.add(title);
                    }
                    Intent serviceIntent = new Intent(getContext(), SerializeTask.class);
                    serviceIntent.putStringArrayListExtra(Constants.SELECTED_ITEMS, (ArrayList<String>) titles);
                    getActivity().startService(serviceIntent);
                    mAdapter.clearSelection();
                } else {

                    mSnackbar.make(mView, getString(R.string.select_items_to_send), Snackbar.LENGTH_SHORT).show();
                }
            } else {

                mSnackbar.make(mView, getString(R.string.error_grant_permission), Snackbar.LENGTH_LONG)
                        .setDuration(10000)
                        .setAction(getString(R.string.grant), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ErrorCheckingCallback.setStatus();
                                Nearby.Messages.getPermissionStatus(mClient).setResultCallback(new ErrorCheckingCallback(getActivity()));
                            }
                        }).show();
            }

            return true;
        } else if (id == R.id.select_all) {
            mAdapter.selectAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class ListViewAdapter extends SimpleCursorAdapter implements View.OnClickListener {


        Viewholder holder;
        private SparseBooleanArray selectedItems = new SparseBooleanArray();
        boolean select_all;

        public ListViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            View view = LayoutInflater.from(context).inflate(R.layout.send_items, parent, false);
            holder = new Viewholder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            holder = (Viewholder) view.getTag();

            int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
            String title = cursor.getString(titleIndex);

            holder.mTitle.setText(title);

            int pos = cursor.getPosition();

            holder.mLinearLayout.setTag(pos);

            holder.mLinearLayout.setOnClickListener(this);
            if (selectedItems.get(pos, false)) {
                holder.mCheckBox.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_circle_green));
            } else {
                holder.mCheckBox.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_circle_grey));
            }


        }

        @Override
        public void onClick(View v) {

            ImageView imageView = (ImageView) v.findViewById(R.id.checkbox);
            int pos = (int) v.getTag();
            if (selectedItems.get(pos, false)) {
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_circle_grey));
                selectedItems.put(pos, false);
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_circle_green));
                selectedItems.put(pos, true);
            }
        }

        public List<Integer> getSelectedItems() {

            List<Integer> items = new ArrayList<>(selectedItems.size());
            for (int i = 0; i < selectedItems.size(); i++) {
                boolean selected = selectedItems.valueAt(i);
                if (selected)
                    items.add(selectedItems.keyAt(i));
            }
            return items;
        }

        public void clearSelection() {
            selectedItems.clear();
            notifyDataSetChanged();
        }

        public int getSelectedItemCount() {

            List<Integer> items = new ArrayList<>(selectedItems.size());
            for (int i = 0; i < selectedItems.size(); i++) {
                boolean selected = selectedItems.valueAt(i);
                if (selected)
                    items.add(selectedItems.keyAt(i));
            }

            return items.size();
        }

        public void selectAll() {

            int size = mCursor.getCount();
            if (!select_all) {
                for (int i = 0; i < size; i++) {
                    selectedItems.put(i, true);
                    select_all = true;
                }
            } else {
                for (int i = 0; i < size; i++) {
                    selectedItems.put(i, false);
                    select_all = false;
                }
            }
            notifyDataSetChanged();
        }

        public void toggleSelection(int pos) {
            if (selectedItems.get(pos, false)) {
                selectedItems.delete(pos);
            } else {
                selectedItems.put(pos, true);
            }
            notifyDataSetChanged();
        }

    }


    static class Viewholder {

        @Bind(R.id.checkbox)
        ImageView mCheckBox;

        @Bind(R.id.title)
        TextView mTitle;

        @Bind(R.id.linear)
        LinearLayout mLinearLayout;

        public Viewholder(View v) {

            ButterKnife.bind(this, v);
            mTitle.setTypeface(Utility.getFont(v.getContext(), v.getContext().getString(R.string.roboto_regular)));
        }

    }


}
