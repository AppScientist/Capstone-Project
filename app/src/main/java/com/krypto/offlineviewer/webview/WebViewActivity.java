package com.krypto.offlineviewer.webview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity implements WebViewActivityFragment.Update {


    @Bind(R.id.app_bar)
    Toolbar mToolbar;

    @Bind(R.id.urlTitle)
    TextView mUrlTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.webView_fragment, WebViewActivityFragment.newInstance()).commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
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

        mUrlTitle.setText(url);
        mUrlTitle.setTypeface(Utility.getFont(this, getString(R.string.roboto_bold)));
    }

}
