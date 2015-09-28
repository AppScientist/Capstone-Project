package com.krypto.offlineviewer.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;


public class ConnectionHelper extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String FRAG_TAG = ConnectionHelper.class.getCanonicalName();
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private static boolean sResolvingError = false;

    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(3 * 60).build();

    private Message mMessage;
    private MessageListener mMessageListener;

    public interface Connection {
        void onConnected();
    }

    public static <ParentFrag extends Fragment & Connection> ConnectionHelper attach(ParentFrag parent) {
        return attach(parent.getChildFragmentManager());
    }


    private static ConnectionHelper attach(FragmentManager fragmentManager) {
        ConnectionHelper frag = (ConnectionHelper) fragmentManager.findFragmentByTag(FRAG_TAG);
        if (frag == null) {
            frag = new ConnectionHelper();
            fragmentManager.beginTransaction().add(frag, FRAG_TAG).commit();
        }
        return frag;
    }

    private Connection getParent() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof Connection) {
            return (Connection) parentFragment;
        } else {
            FragmentActivity activity = getActivity();
            if (activity instanceof Connection) {
                return (Connection) activity;
            }
        }
        return null;
    }

    public synchronized GoogleApiClient buildGoogleApiClient(Context c) {
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Nearby.MESSAGES_API)
                .build();

        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {

        getParent().onConnected();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (sResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                sResolvingError = true;
                connectionResult.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
            sResolvingError = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            if (mMessage != null)
                Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ErrorCheckingCallback(getActivity()));

            if (mMessageListener != null)
                Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                        .setResultCallback(new ErrorCheckingCallback(getActivity()));

            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            sResolvingError = false;
            if (resultCode == AppCompatActivity.RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else {
                Toast.makeText(getContext(), "Unable to resolve error with code " + resultCode,
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void subscribe(MessageListener listener) {

        mMessageListener = listener;
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .build();
        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ErrorCheckingCallback(getActivity()));
    }

    public void publish(Message message) {
        mMessage = message;
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .build();
        Nearby.Messages.publish(mGoogleApiClient, mMessage, options)
                .setResultCallback(new ErrorCheckingCallback(getActivity()));

    }

    public void unpublish() {

        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            if (mMessage != null)
                Nearby.Messages.unpublish(mGoogleApiClient, mMessage)
                        .setResultCallback(new ErrorCheckingCallback(getActivity()));
        }
    }

    public void unsubscribe() {

        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            if (mMessageListener != null)
                Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener)
                        .setResultCallback(new ErrorCheckingCallback(getActivity()));
        }

    }


    private void showErrorDialog(int errorCode) {

        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "errordialog");
    }


    public static class ErrorDialogFragment extends DialogFragment {

        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    getActivity(), REQUEST_RESOLVE_ERROR);

        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            sResolvingError = false;
        }


    }
}