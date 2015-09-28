package com.krypto.offlineviewer.app_intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.krypto.offlineviewer.reading_list.MainActivity;
import com.krypto.offlineviewer.R;

public class IntroActivity extends AppIntro {


    @Override
    public void init(Bundle bundle) {

        String title1 = getString(R.string.add_articles);
        String description1 = getString(R.string.start_adding_articles);

        addSlide(AppIntroFragment.newInstance(title1, description1, R.drawable.intro1, ContextCompat.getColor(this,R.color.md_red_500)));

        String description2 = getString(R.string.click_on_share);
        addSlide(AppIntroFragment.newInstance(title1, description2, R.drawable.intro2, ContextCompat.getColor(this,R.color.md_green_500)));

        String description3 = getString(R.string.select_add_offline);

        addSlide(AppIntroFragment.newInstance(title1, description3, R.drawable.intro3, ContextCompat.getColor(this,R.color.accent)));

        String description4 = getString(R.string.open_first_time);
        addSlide(AppIntroFragment.newInstance(title1, description4, R.drawable.intro4, ContextCompat.getColor(this,R.color.primary)));

        setSeparatorColor(ContextCompat.getColor(this,android.R.color.white));
    }

    @Override
    public void onSkipPressed() {

        startActivity();
    }

    @Override
    public void onDonePressed() {

        startActivity();
    }

    void startActivity() {

        Intent activityIntent = new Intent(this, MainActivity.class);
        startActivity(activityIntent);
    }
}
