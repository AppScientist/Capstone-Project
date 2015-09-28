package com.krypto.offlineviewer.nearby;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.krypto.offlineviewer.Utilities.ConnectionHelper;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;

import butterknife.Bind;
import butterknife.BindBool;
import butterknife.ButterKnife;


public class NearbyFragment extends Fragment implements View.OnClickListener, ConnectionHelper.Connection {


    @Bind(R.id.receive)
    Button mRecieve;

    @Bind(R.id.send)
    Button mSend;

    @Bind(R.id.thumbnail1)
    ImageView mImageView;

    @BindBool(R.bool.Land)
    boolean isLand;

    @Bind(R.id.linear_nearby)
    LinearLayout mLinearLayout;

    @Bind(R.id.scrollView)
    ScrollView mScrollView;

    @BindBool(R.bool.Tablet)
    boolean isTablet;


    private ConnectionHelper mHelper;
    private boolean isConnected;
    private View mView;
    private Snackbar mSnackbar;

    private FragmentStatus mStatus;

    public static NearbyFragment newInstance() {

        Bundle args = new Bundle();

        NearbyFragment fragment = new NearbyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, mView);
        mStatus=(FragmentStatus) getContext();
        mHelper = ConnectionHelper.attach(this);
        mHelper.buildGoogleApiClient(getActivity());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.nearby_new, options);
        mImageView.setImageBitmap(bitmap);

        if (isTablet && !isLand) {
            mImageView.setAdjustViewBounds(true);
            mImageView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mImageView.requestLayout();
        }
        mRecieve.setOnClickListener(this);
        mSend.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onConnected() {

        isConnected = true;
    }

    @Override
    public void onClick(View v) {

        if (isConnected) {
            if (v.getId() == R.id.send) {

                mStatus.fragmentName(Constants.SEND);
                if (!(isTablet && isLand)) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nearbyFragment, NearbySendFragment.newInstance()).commit();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nearbyActionFragment, NearbySendFragment.newInstance()).commit();
                }

            } else {

                mStatus.fragmentName(Constants.RECEIVE);
                if (!(isTablet && isLand)) {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nearbyFragment, NearbyRecieveFragment.newInstance()).commit();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nearbyActionFragment, NearbyRecieveFragment.newInstance()).commit();
                }

            }
        } else
            mSnackbar.make(mView, getString(R.string.unable_to_connect_google_services), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSnackbar != null)
            mSnackbar.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public interface FragmentStatus{

        void fragmentName(String name);
    }
}
