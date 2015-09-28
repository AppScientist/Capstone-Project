package com.krypto.offlineviewer.Utilities;

import android.content.IntentSender;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.krypto.offlineviewer.R;


public class ErrorCheckingCallback implements ResultCallback<Status> {

    private static boolean sResolvingNearbyPermissionError = false;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private FragmentActivity mActivity;

    public ErrorCheckingCallback(FragmentActivity activity) {
        mActivity = activity;

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {

        } else {
            if (status.hasResolution()) {
                if (status.getStatusCode() == NearbyMessagesStatusCodes.APP_NOT_OPTED_IN) {
                    if (!sResolvingNearbyPermissionError) {
                        sResolvingNearbyPermissionError = true;
                        try {
                            status.startResolutionForResult(mActivity,
                                    REQUEST_RESOLVE_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (status.getStatusCode() == NearbyMessagesStatusCodes.NETWORK_ERROR) {
                    Toast.makeText(mActivity,
                            mActivity.getString(R.string.no_internet_connectivity),
                            Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(mActivity, mActivity.getString(R.string.unsuccesfull), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void setStatus(){
        sResolvingNearbyPermissionError = false;
    }
}

