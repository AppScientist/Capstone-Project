package com.krypto.offlineviewer.nearby;


import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.krypto.offlineviewer.Utilities.ConnectionHelper;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.ErrorCheckingCallback;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.model.Articles.Articles;
import com.krypto.offlineviewer.storage.DataContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;


public class NearbyRecieveFragment extends Fragment implements ConnectionHelper.Connection {

    private ConnectionHelper mHelper;

    private MatrixCursor mCursor;
    private ListViewAdapter mAdapter;
    private GoogleApiClient mClient;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Animation mAnimation;
    private Snackbar mSnackbar;
    private View mView;

    @Bind(R.id.listView)
    ListView mListView;
    @Bind(R.id.animation)
    ImageView mAnim;

    @Bind(R.id.empty_state)
    LinearLayout mEmpty;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEdit;

    private List<Articles> mArticlesList = new ArrayList<>();

    public static NearbyRecieveFragment newInstance() {

        Bundle args = new Bundle();

        NearbyRecieveFragment fragment = new NearbyRecieveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyRecieveFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_nearby_recieve, container, false);
        ButterKnife.bind(this, mView);

        mHelper = ConnectionHelper.attach(this);
        mClient = mHelper.buildGoogleApiClient(getActivity());

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mEdit = mPreferences.edit();

        mAdapter = new ListViewAdapter(getContext(), R.layout.recieve_list, null, new String[]{DataContract.ArticlesEntry.TITLE}
                , new int[]{R.id.title}, 0);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmpty);

        if (savedInstanceState != null)
            mArticlesList = (ArrayList<Articles>) savedInstanceState.getSerializable(Constants.CURSOR);


        return mView;
    }


    @Override
    public void onConnected() {

        Nearby.Messages.getPermissionStatus(mClient).setResultCallback(new ErrorCheckingCallback(getActivity()));
        if (mPreferences.getBoolean(Constants.CHECKED, false)) {
            receieveData();
        }

    }


    public void permissionAccepted(boolean permission) {

        mEdit.putBoolean(Constants.PERMISSION, permission).apply();
        mEdit.putBoolean(Constants.CHECKED, true).apply();
        receieveData();
    }

    void receieveData() {
        if (!mPreferences.getBoolean(Constants.PERMISSION, false)) {

            mSnackbar.make(mView, getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                    .setDuration(10000)
                    .setAction(getString(R.string.grant), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ErrorCheckingCallback.setStatus();
                            Nearby.Messages.getPermissionStatus(mClient).setResultCallback(new ErrorCheckingCallback(getActivity()));
                        }
                    }).show();
        } else if (mArticlesList.size() != 0) {
            String[] columns = new String[]{"_id", "article_title"};

            mCursor = new MatrixCursor(columns);
            getActivity().startManagingCursor(mCursor);

            int size = mArticlesList.size();
            for (int i = 0; i < size; i++) {

                Articles articles = mArticlesList.get(i);
                mCursor.addRow(new Object[]{i, articles.getTitle()});

            }
            mAdapter.changeCursor(mCursor);

        } else {
            startAnimation();
            mHelper.subscribe(messageListener);
        }

    }

    MessageListener messageListener = new MessageListener() {
        @Override
        public void onFound(final Message message) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    byte[] bytes = new byte[0];
                    Message message1 = new Message(bytes);
                    mHelper.publish(message1);
                    stopAnimation();
                    unsubscribe();

                }
            });

            byte[] content = message.getContent();
            try {
                List<Articles> articlesList = Utility.deserializeToList(content);

                int size = articlesList.size();
                Vector<ContentValues> vector = new Vector<>(size);

                long identityToken = Binder.clearCallingIdentity();


                String[] columns = new String[]{"_id", "article_title"};

                mCursor = new MatrixCursor(columns);
                getActivity().startManagingCursor(mCursor);

                for (int i = 0; i < size; i++) {

                    final Articles articles = articlesList.get(i);
                    mArticlesList.add(articles);

                    ContentValues values = new ContentValues();
                    values.put(DataContract.ArticlesEntry.TITLE, articles.getTitle());
                    values.put(DataContract.ArticlesEntry.URL, articles.getUrl());
                    values.put(DataContract.ArticlesEntry.DESC, articles.getDesc());
                    values.put(DataContract.ArticlesEntry.IMAGE, articles.getImage());
                    vector.add(values);
                    ContentValues[] cvArray = new ContentValues[vector.size()];
                    getActivity().getContentResolver().bulkInsert(DataContract.ArticlesEntry.CONTENT_URI, vector.toArray(cvArray));

                    mCursor.addRow(new Object[]{i, articles.getTitle()});

                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mAdapter.changeCursor(mCursor);
                    }
                });
                Binder.restoreCallingIdentity(identityToken);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    void startAnimation() {
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.alpha);
        mAnim.setVisibility(View.VISIBLE);
        mAnim.startAnimation(mAnimation);

    }

    public void stopAnimation() {
        mAnim.clearAnimation();
        mAnim.setVisibility(View.GONE);
    }

    void unsubscribe() {
        mHelper.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Constants.CURSOR, (ArrayList<Articles>) mArticlesList);
        super.onSaveInstanceState(outState);
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
        if (mCursor != null)
            mCursor.close();
    }

    class ListViewAdapter extends SimpleCursorAdapter {

        ViewHolder holder;

        public ListViewAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.recieve_list, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            holder = (ViewHolder) view.getTag();

            int titleIndex = cursor.getColumnIndex(DataContract.ArticlesEntry.TITLE);
            String title = cursor.getString(titleIndex);

            holder.mTitle.setText(title);

            ColorGenerator generator = ColorGenerator.MATERIAL;

            String letter = String.valueOf(title.charAt(0));

            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(letter, generator.getColor(title));

            holder.mImage.setImageDrawable(drawable);

            holder.mImage.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.scale));
        }
    }

    static class ViewHolder {
        @Bind(R.id.title)
        TextView mTitle;

        @Bind(R.id.imageText)
        ImageView mImage;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
