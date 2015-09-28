package com.krypto.offlineviewer.reading_list;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.krypto.offlineviewer.R;
import com.krypto.offlineviewer.Utilities.Utility;

import butterknife.Bind;
import butterknife.ButterKnife;


public class WebPageViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.marker)
    ImageView marker;

    @Bind(R.id.articleTitle)
    TextView mTitle;

    @Bind(R.id.articleUrl)
    TextView mUrl;

    @Bind(R.id.swipeable_content)
    RelativeLayout mLayout;

    @Bind(R.id.articledesc)
    TextView mDesciption;

    @Bind(R.id.card)
    CardView mCardView;

    public WebPageViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mTitle.setTypeface(Utility.getFont(view.getContext(), view.getContext().getString(R.string.roboto_light)));
        mUrl.setTypeface(Utility.getFont(view.getContext(), view.getContext().getString(R.string.roboto_bold)));
        mDesciption.setTypeface(Utility.getFont(view.getContext(), view.getContext().getString(R.string.roboto_regular)));
    }
}

