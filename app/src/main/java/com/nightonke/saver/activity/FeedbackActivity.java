package com.nightonke.saver.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.nightonke.saver.R;
import com.nightonke.saver.adapter.HelpFragmentAdapter;
import com.nightonke.saver.fragment.HelpFeedbackFragment;
import com.nightonke.saver.util.CoCoinUtil;

public class FeedbackActivity extends AppCompatActivity implements HelpFeedbackFragment.OnTextChangeListener {

    private ViewPager mViewPager;

    private Toolbar toolbar;

    private HelpFragmentAdapter adapter = null;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_help);

        mViewPager = (ViewPager) findViewById(R.id.materialViewPager);

//        mViewPager.getPagerTitleStrip().setTypeface(CoCoinUtil.getInstance().typefaceLatoLight, Typeface.NORMAL);
//        mViewPager.getPagerTitleStrip().setAllCaps(false);
//        mViewPager.getPagerTitleStrip().setUnderlineColor(Color.parseColor("#00000000"));
//        mViewPager.getPagerTitleStrip().setIndicatorColor(Color.parseColor("#00000000"));
//        mViewPager.getPagerTitleStrip().setUnderlineHeight(0);
//        mViewPager.getPagerTitleStrip().setIndicatorHeight(0);

        setTitle("");

//        toolbar = mViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);

//            final ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
//                actionBar.setDisplayShowHomeEnabled(true);
//                actionBar.setDisplayShowTitleEnabled(true);
//                actionBar.setDisplayUseLogoEnabled(false);
//                actionBar.setHomeButtonEnabled(true);
//            }
        }

//        View logo = findViewById(R.id.logo_white);
//        if (logo != null) {
//            logo.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mViewPager.notifyHeaderChanged();
//                }
//            });
//        }

//        adapter = new HelpFragmentAdapter(getSupportFragmentManager(), 1);
//        mViewPager.getViewPager().setAdapter(adapter);
//        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());
//
//        mViewPager.getPagerTitleStrip().invalidate();
//        mViewPager.getViewPager().setOffscreenPageLimit(2);
//
//        mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
//            @Override
//            public HeaderDesign getHeaderDesign(int page) {
//                return HeaderDesign.fromColorAndDrawable(
//                        ContextCompat.getColor(CoCoinApplication.getAppContext(), R.color.my_blue),
//                        ContextCompat.getDrawable(
//                                CoCoinApplication.getAppContext(), R.drawable.cocoin_blue_bg));
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        MaterialViewPagerHelper.unregister(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private String feedbackText = "";
    private boolean exceed;
    @Override
    public void onTextChange(String text, boolean exceed) {
        feedbackText = text;
        this.exceed = exceed;
    }
}
