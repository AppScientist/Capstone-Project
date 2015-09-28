package com.krypto.offlineviewer.reading_list;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.search.SearchFragment;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.app_intro.IntroActivity;
import com.krypto.offlineviewer.nearby.NearbyMessagesActivity;
import com.krypto.offlineviewer.service.WebpageService;
import com.krypto.offlineviewer.webview.WebViewActivityFragment;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, WebViewActivityFragment.Update {


    private SearchView mView;


    @Bind(R.id.app_bar)
    Toolbar mToolbar;

    @Bind(R.id.toolbarTitle)
    TextView mTitle;

    @Bind(R.id.fragment)
    FrameLayout mFrameLayout;

    @BindBool(R.bool.Land)
    boolean isLand;

    @Nullable
    @Bind(R.id.empty_view)
    View mEmptyView;

    @Bind(R.id.marker_progress)
    ProgressBar mBar;

    @Bind(R.id.adView)
    AdView mAd;

    @BindBool(R.bool.Tablet)
    boolean isTablet;

    private BroadcastReceiver mReceiver;
    private String url;
    private String mSaveQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = preferences.edit();
        boolean first = preferences.getBoolean(Constants.FIRST, true);
        if (first) {
            Intent activityIntent = new Intent(this, IntroActivity.class);
            startActivity(activityIntent);
            edit.putBoolean(Constants.FIRST, false).apply();
            finish();
        }
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {

            url = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        setSupportActionBar(mToolbar);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, MainActivityFragment.newInstance()).commit();
            if (url != null) {
                Intent serviceIntent = new Intent(this, WebpageService.class);
                serviceIntent.putExtra(Constants.URL, url);
                startService(serviceIntent);
                mBar.setVisibility(View.VISIBLE);

            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mTitle.setText(getString(R.string.offline_reader));
        mTitle.setTypeface(Utility.getFont(this, getString(R.string.roboto_medium)));

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mFrameLayout.getLayoutParams();
        if ((!isTablet && isLand) || (isTablet && !isLand)) {

            params.leftMargin = getResources().getDimensionPixelSize(R.dimen.card_margin);
            params.rightMargin = getResources().getDimensionPixelSize(R.dimen.card_margin);
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.VISIBLE);
        } else {
            if (mEmptyView != null)
                mEmptyView.setVisibility(View.GONE);

        }

        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAd.loadAd(adRequest);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mBar.setVisibility(View.GONE);
                MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
                fragment.moveRecyclerView();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Constants.COMPLETED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(Constants.QUERY, mSaveQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            mSaveQuery = savedInstanceState.getString(Constants.QUERY);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        mSaveQuery = query;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, SearchFragment.newInstance(query)).commit();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        mSaveQuery = newText;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, SearchFragment.newInstance(newText)).commit();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.search_view);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mView = (SearchView) MenuItemCompat.getActionView(item);
        mView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mView.setIconified(true);
        mView.setOnQueryTextListener(this);
        if (mSaveQuery != null)
            mView.setQuery(mSaveQuery, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nearby) {
            Intent intent = new Intent(this, NearbyMessagesActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        WebViewActivityFragment fragment = (WebViewActivityFragment) getSupportFragmentManager().findFragmentById(R.id.webView_fragment);
        if (fragment != null && fragment.canGoBack()) {
            fragment.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void updateUrl(String url) {

        if (!(isTablet && !isLand))
            mTitle.setText(url);
    }
}
