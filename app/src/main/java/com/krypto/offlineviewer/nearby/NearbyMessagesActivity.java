package com.krypto.offlineviewer.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;

public class NearbyMessagesActivity extends AppCompatActivity implements NearbyFragment.FragmentStatus {

    @Bind(R.id.app_bar)
    Toolbar mToolbar;

    @Bind(R.id.toolbarTitle)
    TextView mTitle;

    @BindBool(R.bool.Tablet)
    boolean isTablet;


    @BindBool(R.bool.Land)
    boolean isLand;

    private NearbySendFragment mSendFragment;
    private NearbyRecieveFragment mRecieveFragment;
    private String mName;

    private static final int REQUEST_RESOLVE_ERROR = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_messages);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.nearbyFragment, NearbyFragment.newInstance()).commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitle.setText(getString(R.string.share_content));
        mTitle.setTypeface(Utility.getFont(this, getString(R.string.roboto_medium)));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (!(isTablet && isLand)) {
                if (mName.equals(Constants.SEND))
                    mSendFragment = (NearbySendFragment) getSupportFragmentManager().findFragmentById(R.id.nearbyFragment);
                else
                    mRecieveFragment = (NearbyRecieveFragment) getSupportFragmentManager().findFragmentById(R.id.nearbyFragment);
            } else {
                if (mName.equals(Constants.SEND))
                    mSendFragment = (NearbySendFragment) getSupportFragmentManager().findFragmentById(R.id.nearbyActionFragment);
                else
                    mRecieveFragment = (NearbyRecieveFragment) getSupportFragmentManager().findFragmentById(R.id.nearbyActionFragment);
            }
        } catch (ClassCastException c) {
            c.printStackTrace();
        } finally {

            if (requestCode == REQUEST_RESOLVE_ERROR) {
                if (resultCode == Activity.RESULT_OK) {
                    if (mSendFragment != null) {

                        mSendFragment.permissionAccepted(true);
                    } else if (mRecieveFragment != null) {

                        mRecieveFragment.permissionAccepted(true);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    if (mSendFragment != null) {

                        mSendFragment.permissionAccepted(false);
                    } else if (mRecieveFragment != null) {

                        mRecieveFragment.permissionAccepted(false);
                    }
                } else {
                    Toast.makeText(this, getString(R.string.resolve_failure, resultCode),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void fragmentName(String name) {

        mName = name;
    }
}
