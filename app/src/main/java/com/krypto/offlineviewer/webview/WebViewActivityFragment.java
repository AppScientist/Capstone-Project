package com.krypto.offlineviewer.webview;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.konifar.fab_transformation.FabTransformation;
import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Constants;
import com.krypto.offlineviewer.Utilities.Utility;
import com.krypto.offlineviewer.reading_list.MainActivity;
import com.krypto.offlineviewer.service.DownloadService;
import com.krypto.offlineviewer.service.DownloadTextService;
import com.krypto.offlineviewer.storage.DataContract;

import org.jsoup.Jsoup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WebViewActivityFragment extends Fragment implements TextToSpeech.OnInitListener {

    @Bind(R.id.webView)
    WebView mWebView;

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout mSwipe;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    @Bind(R.id.card)
    CardView mCardView;

    @Bind(R.id.share)
    TextView mShare;

    @Bind(R.id.tts)
    TextView mTts;

    @Bind(R.id.delete)
    TextView mDelete;

    @Bind(R.id.header)
    TextView mHeader;


    private String mLink, mTitle;
    private Update mUpdate;
    private TextToSpeech mTextToSpeech;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mReceiver2;
    private static final int NOTIFICATIONID = 001;
    private static final int MY_DATA_CHECK_CODE = 1;
    private List<String> mStrings = new ArrayList<>();
    private int i = 0;
    private boolean mHtmlDownloading;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mTtsInitalized;

    public static WebViewActivityFragment newInstance() {

        return new WebViewActivityFragment();
    }

    public static WebViewActivityFragment newInstancewithArgs(String title, String url) {

        Bundle args = new Bundle();
        args.putString(Constants.TITLE, title);
        args.putString(Constants.URL, url);
        WebViewActivityFragment fragment = new WebViewActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public WebViewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        ButterKnife.bind(this, view);
        mUpdate = (Update) getContext();

        mSwipe.setEnabled(false);
        mSwipe.setColorSchemeResources(R.color.accent, R.color.primary, R.color.background);


        mLink = getActivity().getIntent().getStringExtra(Constants.URL);
        mTitle = getActivity().getIntent().getStringExtra(Constants.TITLE);

        if (getArguments() != null) {
            mLink = getArguments().getString(Constants.URL);
            mTitle = getArguments().getString(Constants.TITLE);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                if (getContext() != null) {
                    boolean exists = Utility.imageFileExists(getContext(), mLink, url);
                    if (exists) {
                        InputStream stream = Utility.getImagesFromCache(getContext(), mLink, url);
                        return new WebResourceResponse("images/*", "", stream);
                    }
                }
                return null;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                if (getContext() != null) {
                    boolean exists = Utility.imageFileExists(getContext(), mLink, request.getUrl().toString());
                    if (exists) {
                        String url = request.getUrl().toString();
                        InputStream stream = Utility.getImagesFromCache(getContext(), mLink, url);
                        return new WebResourceResponse("images/*", "", stream);
                    }
                }
                return null;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Snackbar.make(view, getString(R.string.webpage_not_downloaded), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Snackbar.make(view, getString(R.string.webpage_not_downloaded), Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                if (getContext() != null) {
                    mUpdate.updateUrl(url);
                }
                if (mSwipe != null)
                    mSwipe.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mSwipe != null && !mHtmlDownloading)
                    mSwipe.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (mTextToSpeech != null) {
                    mTextToSpeech.stop();
                    mTextToSpeech.shutdown();
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptEnabled(true);

        if (!Utility.htmlFileExists(getContext(), mLink)) {

            if (Utility.textFileExists(getContext(), mLink)) {
                downloadHtml();
                String content = Utility.getTextFromCache(getContext(), mLink);
                mWebView.loadDataWithBaseURL(mLink, content, "text/html", "", mLink);
                Toast.makeText(getContext(), getString(R.string.webpage_displaying_content), Toast.LENGTH_LONG).show();
            } else {


                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipe.setRefreshing(true);
                    }
                });

                Intent serviceIntent2 = new Intent(getContext(), DownloadTextService.class);
                serviceIntent2.putExtra(Constants.URL, mLink);
                getActivity().startService(serviceIntent2);
            }

        } else {
            mHtmlDownloading = false;
            String content = Utility.getHtmlFromCache(getContext(), mLink);
            mWebView.loadDataWithBaseURL(mLink, content, "text/html", "", mLink);
        }

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        return view;
    }

    void downloadHtml() {
        Intent serviceIntent = new Intent(getContext(), DownloadService.class);
        serviceIntent.putExtra(Constants.URL, mLink);
        getActivity().startService(serviceIntent);

        mHtmlDownloading = true;
        Notification.Builder builder = new Notification.Builder(getContext())
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.downloading_webpage))
                .setContentText(getString(R.string.downloadging_in_progress));

        Intent resultIntent = new Intent(getContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        builder.setProgress(0, 0, true);

        NotificationManager mNotifyMgr =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIFICATIONID, builder.build());

    }

    public boolean canGoBack() {
        return mWebView != null && mWebView.canGoBack();
    }

    public void goBack() {
        if (this.mWebView != null) {
            this.mWebView.goBack();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTextToSpeech = new TextToSpeech(getContext(), this);
                mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        ttsClick();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), getString(R.string.tts_not_initialized), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                toSpeakText();

            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    void toSpeakText() {

        if (Utility.textFileExists(getContext(), mLink)) {
            int index = 0;
            String content = Utility.getTextFromCache(getContext(), mLink);
            String toSpeak = Jsoup.parse(content).body().text();
            //Text to speech feature can only read 4000 characters at a time.
            while (index < toSpeak.length()) {
                mStrings.add(toSpeak.substring(index, Math.min(index + 3999, toSpeak.length())));
                index += 4000;
            }
        }
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            mTtsInitalized = true;
            mTextToSpeech.setLanguage(Locale.getDefault());
        }
    }

    @OnClick(R.id.fab)
    public void click() {

        FabTransformation.with(mFab).transformTo(mCardView);
    }

    @OnClick(R.id.share)
    void shareClick() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mTitle + " \n" + mLink);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
        FabTransformation.with(mFab).transformFrom(mCardView);
    }

    @OnClick(R.id.tts)
    void ttsClick() {

        FabTransformation.with(mFab).transformFrom(mCardView);
        int size = mStrings.size();
        if (size != 0 && i < size && mTtsInitalized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mTextToSpeech.speak(mStrings.get(i), TextToSpeech.QUEUE_FLUSH, null, String.valueOf(i));
            else
                mTextToSpeech.speak(mStrings.get(i), TextToSpeech.QUEUE_FLUSH, null);
            i++;
        } else {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getString(R.string.tts_not_initialized), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    @OnClick(R.id.delete)
    void deleteClick() {

        getActivity().getContentResolver().delete(DataContract.ArticlesEntry.CONTENT_URI, "article_title = ?", new String[]{mTitle});

        Utility.deleteCache(getContext(), mLink);

        if (!getActivity().isTaskRoot()) {
            getActivity().onBackPressed();
        } else {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @OnClick(R.id.header)
    void headerClick() {
        FabTransformation.with(mFab).transformFrom(mCardView);
    }


    @Override
    public void onResume() {
        super.onResume();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mHtmlDownloading = false;
                String content = Utility.getHtmlFromCache(getContext(), mLink);
                mWebView.loadDataWithBaseURL(mLink, content, "text/html", "", mLink);

                NotificationManager mNotifyMgr =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(NOTIFICATIONID);

            }
        };

        mReceiver2 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                downloadHtml();
                toSpeakText();
                String content = Utility.getTextFromCache(getContext(), mLink);
                mWebView.loadDataWithBaseURL(mLink, content, "text/html", "", mLink);
                Toast.makeText(getContext(), getString(R.string.webpage_displaying_content), Toast.LENGTH_LONG).show();
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, new IntentFilter(Constants.DOWNLOADED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver2, new IntentFilter(Constants.DOWNLOADED_TEXT));

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver2);
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public interface Update {

        void updateUrl(String url);
    }

    public String getUrl() {

        return mLink;
    }

}
